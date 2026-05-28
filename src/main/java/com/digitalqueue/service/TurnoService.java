package com.digitalqueue.service;

import com.digitalqueue.dto.CrearTurnoResponse;
import com.digitalqueue.dto.TurnoAdminResponse;
import com.digitalqueue.dto.TurnoEstadoResponse;
import com.digitalqueue.exception.OperacionInvalidaException;
import com.digitalqueue.exception.RecursoNoEncontradoException;
import com.digitalqueue.model.Fila;
import com.digitalqueue.model.PuntoAcceso;
import com.digitalqueue.model.Turno;
import com.digitalqueue.model.enums.EstadoFila;
import com.digitalqueue.model.enums.EstadoTurno;
import com.digitalqueue.repository.PuntoAccesoRepository;
import com.digitalqueue.repository.TurnoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TurnoService {

    private final TurnoRepository turnoRepository;
    private final PuntoAccesoRepository puntoAccesoRepository;

    private static final List<EstadoTurno> ESTADOS_EN_ESPERA = List.of(
            EstadoTurno.ESPERANDO,
            EstadoTurno.PROXIMO
    );

    @Transactional
    public CrearTurnoResponse crearTurnoAnonimo(String codigoPublico) {
        PuntoAcceso puntoAcceso = puntoAccesoRepository
                .findByCodigoPublicoAndActivoTrue(codigoPublico)
                .orElseThrow(() -> new RecursoNoEncontradoException("Punto de acceso no encontrado o inactivo"));

        Fila fila = puntoAcceso.getFila();

        if (fila.getEstado() != EstadoFila.ABIERTA) {
            throw new OperacionInvalidaException("La fila no está abierta");
        }

        Integer proximoNumero = obtenerProximoNumeroTurno(fila.getId());

        Turno turno = Turno.builder()
                .fila(fila)
                .numeroTurno(proximoNumero)
                .tokenPublico(UUID.randomUUID().toString())
                .estado(EstadoTurno.ESPERANDO)
                .expiresAt(LocalDateTime.now().plusHours(2))
                .cliente(null)
                .build();

        Turno turnoGuardado = turnoRepository.save(turno);

        Long personasAdelante = calcularPersonasAdelante(turnoGuardado);
        Integer tiempoEstimado = calcularTiempoEstimado(turnoGuardado, personasAdelante);

        return new CrearTurnoResponse(
                turnoGuardado.getId(),
                turnoGuardado.getNumeroTurno(),
                turnoGuardado.getTokenPublico(),
                turnoGuardado.getEstado(),
                personasAdelante,
                tiempoEstimado
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

        turno.setEstado(EstadoTurno.LLAMADO);
        turno.setCalledAt(LocalDateTime.now());

        Turno turnoGuardado = turnoRepository.save(turno);

        return mapToTurnoEstadoResponse(turnoGuardado);
    }

    @Transactional
    public TurnoEstadoResponse finalizarTurno(Long turnoId) {
        Turno turno = turnoRepository.findById(turnoId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Turno no encontrado"));

        turno.setEstado(EstadoTurno.FINALIZADO);
        turno.setCompletedAt(LocalDateTime.now());

        Turno turnoGuardado = turnoRepository.save(turno);

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

        if (turno.getEstado() == EstadoTurno.FINALIZADO ||
                turno.getEstado() == EstadoTurno.CANCELADO ||
                turno.getEstado() == EstadoTurno.NO_PRESENTADO ||
                turno.getEstado() == EstadoTurno.EXPIRADO) {
            throw new OperacionInvalidaException("El turno ya no se puede cancelar");
        }

        turno.setEstado(EstadoTurno.CANCELADO);
        turno.setCompletedAt(LocalDateTime.now());

        Turno turnoGuardado = turnoRepository.save(turno);

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

    private Integer calcularTiempoEstimado(Turno turno, Long personasAdelante) {
        return Math.toIntExact(
                personasAdelante * turno.getFila().getTiempoPromedioAtencionMinutos()
        );
    }

    private TurnoEstadoResponse mapToTurnoEstadoResponse(Turno turno) {
        Long personasAdelante = calcularPersonasAdelante(turno);
        Integer tiempoEstimado = calcularTiempoEstimado(turno, personasAdelante);

        return new TurnoEstadoResponse(
                turno.getId(),
                turno.getFila().getId(),
                turno.getNumeroTurno(),
                turno.getEstado(),
                personasAdelante,
                tiempoEstimado
        );
    }

    private TurnoAdminResponse mapToTurnoAdminResponse(Turno turno) {
        return new TurnoAdminResponse(
                turno.getId(),
                turno.getNumeroTurno(),
                turno.getEstado(),
                turno.getCreatedAt(),
                turno.getCalledAt(),
                turno.getCompletedAt()
        );
    }
}