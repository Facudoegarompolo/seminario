package com.digitalqueue.service;

import com.digitalqueue.dto.CrearTurnoRequest;
import com.digitalqueue.dto.CrearTurnoResponse;
import com.digitalqueue.dto.EstimacionEspera;
import com.digitalqueue.dto.TurnoAdminResponse;
import com.digitalqueue.dto.TurnoEstadoResponse;
import com.digitalqueue.exception.OperacionInvalidaException;
import com.digitalqueue.exception.RecursoNoEncontradoException;
import com.digitalqueue.model.Fila;
import com.digitalqueue.model.Local;
import com.digitalqueue.model.PuntoAcceso;
import com.digitalqueue.model.Turno;
import com.digitalqueue.model.enums.EstadoFila;
import com.digitalqueue.model.enums.EstadoTurno;
import com.digitalqueue.model.enums.QueueStatus;
import com.digitalqueue.model.enums.TipoCliente;
import com.digitalqueue.model.enums.TipoOperacionLocal;
import com.digitalqueue.model.enums.TipoNotificacion;
import com.digitalqueue.repository.LocalRepository;
import com.digitalqueue.repository.PuntoAccesoRepository;
import com.digitalqueue.repository.TurnoRepository;
import com.digitalqueue.service.metrics.MetricasFilaService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TurnoService {

    private final TurnoRepository turnoRepository;
    private final PuntoAccesoRepository puntoAccesoRepository;
    private final LocalRepository localRepository;
    private final EstimacionEsperaService estimacionEsperaService;
    private final MetricasFilaService metricasFilaService;
    private final PushNotificationService pushNotificationService;

    private static final List<EstadoTurno> ESTADOS_EN_ESPERA = List.of(
            EstadoTurno.ESPERANDO,
            EstadoTurno.PROXIMO
    );
    private static final String NOMBRE_CLIENTE_ANONIMO = "Cliente anónimo";

    @Transactional
    public CrearTurnoResponse crearTurnoAnonimo(String codigoPublico, CrearTurnoRequest request) {
        PuntoAcceso puntoAcceso = puntoAccesoRepository
                .findByCodigoPublicoAndActivoTrue(codigoPublico)
                .orElseThrow(() -> new RecursoNoEncontradoException("Punto de acceso no encontrado o inactivo"));

        Fila fila = puntoAcceso.getFila();

        if (fila.getEstado() != EstadoFila.ABIERTA) {
            throw new OperacionInvalidaException("La fila no está abierta");
        }

        int cantidadIntegrantes = obtenerCantidadIntegrantes(request);
        String nombreCliente = obtenerNombreCliente(request);
        Long personasAdelante = turnoRepository.countByFilaIdAndEstadoIn(fila.getId(), ESTADOS_EN_ESPERA);
        EstimacionEspera estimacion = estimacionEsperaService.calcularEstimacion(fila, personasAdelante, cantidadIntegrantes);

        Integer proximoNumero = obtenerProximoNumeroTurno(fila.getId());
        EstadoTurno estadoInicial = estimacion.getQueueStatus() == QueueStatus.SIN_ESPERA
                ? EstadoTurno.LLAMADO
                : EstadoTurno.ESPERANDO;

        LocalDateTime ahora = LocalDateTime.now();

        Turno turno = Turno.builder()
                .fila(fila)
                .numeroTurno(proximoNumero)
                .tokenPublico(UUID.randomUUID().toString())
                .estado(estadoInicial)
                .calledAt(estadoInicial == EstadoTurno.LLAMADO ? ahora : null)
                .expiresAt(ahora.plusHours(2))
                .cliente(null)
                .nombreCliente(nombreCliente)
                .tipoCliente(TipoCliente.ANONIMO)
                .cantidadIntegrantes(cantidadIntegrantes)
                .posicionInicial(Math.toIntExact(personasAdelante + 1))
                .personasAdelanteAlAnotarse(personasAdelante)
                .queueStatusAlAnotarse(estimacion.getQueueStatus())
                .tiempoEstimadoInformadoMinutos(estimacion.getTiempoEstimadoMinutos())
                .tiempoEstimadoMinimoMinutos(estimacion.getTiempoEstimadoMinimoMinutos())
                .tiempoEstimadoMaximoMinutos(estimacion.getTiempoEstimadoMaximoMinutos())
                .tiempoRealEsperaMinutos(estadoInicial == EstadoTurno.LLAMADO ? 0 : null)
                .errorPrediccionMinutos(estadoInicial == EstadoTurno.LLAMADO ? 0 : null)
                .build();

        if (estadoInicial == EstadoTurno.LLAMADO && esConsumoEnLocal(fila.getLocal())) {
            sumarPersonasActuales(fila.getLocal(), cantidadIntegrantes);
        }

        Turno turnoGuardado = turnoRepository.save(turno);
        metricasFilaService.registrarInscripcion(turnoGuardado);
        if (estadoInicial == EstadoTurno.LLAMADO) {
            metricasFilaService.registrarLlamado(turnoGuardado);
        }

        return new CrearTurnoResponse(
                turnoGuardado.getId(),
                turnoGuardado.getNumeroTurno(),
                turnoGuardado.getTokenPublico(),
                turnoGuardado.getEstado(),
                turnoGuardado.getNombreCliente(),
                turnoGuardado.getTipoCliente(),
                estimacion.getQueueStatus(),
                personasAdelante,
                estimacion.getTiempoEstimadoMinutos(),
                estimacion.getTiempoEstimadoMinimoMinutos(),
                estimacion.getTiempoEstimadoMaximoMinutos()
        );
    }

    public TurnoEstadoResponse obtenerEstadoTurno(String tokenPublico) {
        Turno turno = turnoRepository.findByTokenPublico(tokenPublico)
                .orElseThrow(() -> new RecursoNoEncontradoException("Turno no encontrado"));

        return mapToTurnoEstadoResponse(turno);
    }

    public List<TurnoAdminResponse> obtenerTurnosDeFila(Long filaId) {
        return turnoRepository.findByFilaIdOrderByCreatedAtAsc(filaId)
                .stream()
                .map(this::mapToTurnoAdminResponse)
                .toList();
    }

    @Transactional
    public TurnoEstadoResponse llamarSiguiente(Long filaId) {
        Turno turno = turnoRepository
                .findFirstByFilaIdAndEstadoInOrderByCreatedAtAsc(filaId, ESTADOS_EN_ESPERA)
                .orElseThrow(() -> new OperacionInvalidaException("No hay turnos esperando"));

        LocalDateTime ahora = LocalDateTime.now();
        turno.setEstado(EstadoTurno.LLAMADO);
        turno.setCalledAt(ahora);
        actualizarMetricasDeEspera(turno, ahora);
        if (esConsumoEnLocal(turno.getFila().getLocal())) {
            sumarPersonasActuales(turno.getFila().getLocal(), turno.getCantidadIntegrantes());
        }

        Turno turnoGuardado = turnoRepository.save(turno);
        metricasFilaService.registrarLlamado(turnoGuardado);
        pushNotificationService.registrarNotificacionPendiente(turnoGuardado, TipoNotificacion.TURNO_LLAMADO);

        return mapToTurnoEstadoResponse(turnoGuardado);
    }

    @Transactional
    public TurnoEstadoResponse finalizarTurno(Long turnoId) {
        Turno turno = turnoRepository.findById(turnoId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Turno no encontrado"));

        turno.setEstado(EstadoTurno.FINALIZADO);
        turno.setCompletedAt(LocalDateTime.now());
        if (esConsumoEnLocal(turno.getFila().getLocal())) {
            restarPersonasActuales(turno.getFila().getLocal(), turno.getCantidadIntegrantes());
        }

        Turno turnoGuardado = turnoRepository.save(turno);
        pushNotificationService.registrarNotificacionPendiente(turnoGuardado, TipoNotificacion.NO_PRESENTADO);

        return mapToTurnoEstadoResponse(turnoGuardado);
    }

    @Transactional
    public TurnoEstadoResponse marcarNoPresentado(Long turnoId) {
        Turno turno = turnoRepository.findById(turnoId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Turno no encontrado"));

        turno.setEstado(EstadoTurno.NO_PRESENTADO);
        turno.setCompletedAt(LocalDateTime.now());

        Turno turnoGuardado = turnoRepository.save(turno);

        return mapToTurnoEstadoResponse(turnoGuardado);
    }

    @Transactional
    public TurnoEstadoResponse cancelarTurno(String tokenPublico) {
        Turno turno = turnoRepository.findByTokenPublico(tokenPublico)
                .orElseThrow(() -> new RecursoNoEncontradoException("Turno no encontrado"));

        if (turno.getEstado() != EstadoTurno.ESPERANDO && turno.getEstado() != EstadoTurno.PROXIMO) {
            throw new OperacionInvalidaException("Solo se pueden cancelar turnos que siguen en la fila");
        }

        turno.setEstado(EstadoTurno.CANCELADO);
        turno.setCompletedAt(LocalDateTime.now());

        Turno turnoGuardado = turnoRepository.save(turno);
        pushNotificationService.registrarNotificacionPendiente(turnoGuardado, TipoNotificacion.TURNO_CANCELADO);

        return mapToTurnoEstadoResponse(turnoGuardado);
    }

    private Integer obtenerProximoNumeroTurno(Long filaId) {
        return turnoRepository.findTopByFilaIdOrderByNumeroTurnoDesc(filaId)
                .map(turno -> turno.getNumeroTurno() + 1)
                .orElse(1);
    }

    private Long calcularPersonasAdelante(Turno turno) {
        if (turno.getEstado() != EstadoTurno.ESPERANDO &&
                turno.getEstado() != EstadoTurno.PROXIMO) {
            return 0L;
        }

        return turnoRepository.countByFilaIdAndEstadoInAndCreatedAtBefore(
                turno.getFila().getId(),
                ESTADOS_EN_ESPERA,
                turno.getCreatedAt()
        );
    }

    private TurnoEstadoResponse mapToTurnoEstadoResponse(Turno turno) {
        Long personasAdelante = calcularPersonasAdelante(turno);
        EstimacionEspera estimacion;

        if (turno.getEstado() == EstadoTurno.ESPERANDO || turno.getEstado() == EstadoTurno.PROXIMO) {
            estimacion = estimacionEsperaService.calcularEstimacion(
                    turno.getFila(),
                    personasAdelante,
                    turno.getCantidadIntegrantes()
            );
        } else {
            estimacion = new EstimacionEspera(turno.getFila().getQueueStatus(), 0, 0, 0, 0.0);
        }

        return new TurnoEstadoResponse(
                turno.getId(),
                turno.getFila().getId(),
                turno.getNumeroTurno(),
                turno.getEstado(),
                obtenerNombreCliente(turno),
                obtenerTipoCliente(turno),
                estimacion.getQueueStatus(),
                personasAdelante,
                estimacion.getTiempoEstimadoMinutos(),
                estimacion.getTiempoEstimadoMinimoMinutos(),
                estimacion.getTiempoEstimadoMaximoMinutos()
        );
    }

    private TurnoAdminResponse mapToTurnoAdminResponse(Turno turno) {
        return new TurnoAdminResponse(
                turno.getId(),
                turno.getNumeroTurno(),
                turno.getEstado(),
                turno.getQueueStatusAlAnotarse(),
                turno.getCantidadIntegrantes(),
                obtenerNombreCliente(turno),
                obtenerTipoCliente(turno),
                turno.getPersonasAdelanteAlAnotarse(),
                turno.getTiempoEstimadoInformadoMinutos(),
                turno.getTiempoRealEsperaMinutos(),
                turno.getErrorPrediccionMinutos(),
                turno.getCreatedAt(),
                turno.getCalledAt(),
                turno.getCompletedAt()
        );
    }

    private int obtenerCantidadIntegrantes(CrearTurnoRequest request) {
        if (request == null || request.getCantidadIntegrantes() == null) {
            return 1;
        }
        return request.getCantidadIntegrantes();
    }

    private String obtenerNombreCliente(CrearTurnoRequest request) {
        if (request == null || request.getNombreCliente() == null || request.getNombreCliente().isBlank()) {
            return NOMBRE_CLIENTE_ANONIMO;
        }
        return request.getNombreCliente().trim();
    }

    private String obtenerNombreCliente(Turno turno) {
        if (turno.getNombreCliente() == null || turno.getNombreCliente().isBlank()) {
            return NOMBRE_CLIENTE_ANONIMO;
        }
        return turno.getNombreCliente();
    }

    private TipoCliente obtenerTipoCliente(Turno turno) {
        return turno.getTipoCliente() == null ? TipoCliente.ANONIMO : turno.getTipoCliente();
    }

    private void actualizarMetricasDeEspera(Turno turno, LocalDateTime horaLlamado) {
        if (turno.getCreatedAt() == null) {
            return;
        }

        int tiempoReal = Math.toIntExact(Duration.between(turno.getCreatedAt(), horaLlamado).toMinutes());
        int estimadoInformado = turno.getTiempoEstimadoInformadoMinutos() == null ? 0 : turno.getTiempoEstimadoInformadoMinutos();

        turno.setTiempoRealEsperaMinutos(tiempoReal);
        turno.setErrorPrediccionMinutos(Math.abs(tiempoReal - estimadoInformado));
    }

    private void sumarPersonasActuales(Local local, Integer cantidad) {
        int actuales = local.getPersonasActuales() == null ? 0 : local.getPersonasActuales();
        int integrantes = cantidad == null ? 1 : cantidad;
        local.setPersonasActuales(actuales + integrantes);
        actualizarMomentoLleno(local, LocalDateTime.now());
        localRepository.save(local);
    }

    private void restarPersonasActuales(Local local, Integer cantidad) {
        int actuales = local.getPersonasActuales() == null ? 0 : local.getPersonasActuales();
        int integrantes = cantidad == null ? 1 : cantidad;
        local.setPersonasActuales(Math.max(0, actuales - integrantes));
        actualizarMomentoLleno(local, LocalDateTime.now());
        localRepository.save(local);
    }

    private void actualizarMomentoLleno(Local local, LocalDateTime ahora) {
        int capacidad = obtenerCapacidadEfectiva(local);
        if (capacidad <= 0) {
            return;
        }

        boolean estaLleno = (local.getPersonasActuales() == null ? 0 : local.getPersonasActuales()) >= capacidad;

        if (estaLleno && local.getLlenoDesde() == null) {
            local.setLlenoDesde(ahora);
        } else if (!estaLleno) {
            local.setLlenoDesde(null);
        }
    }

    private int obtenerCapacidadEfectiva(Local local) {
        int capacidadOperativa = local.getCapacidadOperativaActual() == null ? 0 : local.getCapacidadOperativaActual();
        if (capacidadOperativa > 0) {
            return capacidadOperativa;
        }

        return local.getCapacidadMaxima() == null ? 0 : local.getCapacidadMaxima();
    }

    private boolean esConsumoEnLocal(Local local) {
        TipoOperacionLocal tipoOperacion = local.getTipoOperacion() == null
                ? TipoOperacionLocal.ATENCION_RAPIDA
                : local.getTipoOperacion();
        return tipoOperacion == TipoOperacionLocal.CONSUMO_EN_LOCAL;
    }
}
