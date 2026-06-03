package com.digitalqueue.service.metrics;

import com.digitalqueue.model.Turno;
import com.digitalqueue.model.metrics.MetricaInscripcionFilaDia;
import com.digitalqueue.model.metrics.MetricaInscripcionFilaDiaId;
import com.digitalqueue.model.metrics.MetricaLlegadasFranja;
import com.digitalqueue.model.metrics.MetricaLlegadasFranjaId;
import com.digitalqueue.model.metrics.MetricaLlamadoFilaDia;
import com.digitalqueue.repository.metrics.MetricaInscripcionFilaDiaRepository;
import com.digitalqueue.repository.metrics.MetricaLlegadasFranjaRepository;
import com.digitalqueue.repository.metrics.MetricaLlamadoFilaDiaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class MetricasFilaService {

    private final MetricaInscripcionFilaDiaRepository inscripcionRepository;
    private final MetricaLlamadoFilaDiaRepository llamadoRepository;
    private final MetricaLlegadasFranjaRepository llegadasFranjaRepository;

    @Transactional
    public void registrarInscripcion(Turno turno) {
        if (!turnoTieneDatosMinimos(turno) || turno.getCreatedAt() == null) {
            return;
        }

        LocalDateTime createdAt = turno.getCreatedAt();
        LocalDate fecha = createdAt.toLocalDate();
        String diaSemana = obtenerDiaSemana(turno, createdAt);
        Integer franjaHoraria = obtenerFranjaHoraria(turno, createdAt);

        MetricaInscripcionFilaDiaId id = new MetricaInscripcionFilaDiaId(
                turno.getFila().getId(),
                fecha,
                createdAt,
                turno.getId()
        );
        boolean esNuevaInscripcion = !inscripcionRepository.existsById(id);

        inscripcionRepository.save(MetricaInscripcionFilaDia.builder()
                .filaId(turno.getFila().getId())
                .fecha(fecha)
                .createdAt(createdAt)
                .turnoId(turno.getId())
                .cantidadIntegrantes(valorEntero(turno.getCantidadIntegrantes(), 1))
                .nombreCliente(turno.getNombreCliente())
                .tipoCliente(turno.getTipoCliente() == null ? null : turno.getTipoCliente().name())
                .personasAdelante(valorLong(turno.getPersonasAdelanteAlAnotarse(), 0L))
                .queueStatus(turno.getQueueStatusAlAnotarse() == null ? null : turno.getQueueStatusAlAnotarse().name())
                .tiempoEstimadoInformado(turno.getTiempoEstimadoInformadoMinutos())
                .tiempoEstimadoMinimo(turno.getTiempoEstimadoMinimoMinutos())
                .tiempoEstimadoMaximo(turno.getTiempoEstimadoMaximoMinutos())
                .diaSemana(diaSemana)
                .franjaHoraria(franjaHoraria)
                .build());

        if (esNuevaInscripcion) {
            incrementarLlegadasPorFranja(turno.getFila().getId(), diaSemana, franjaHoraria, fecha);
        }
    }

    @Transactional
    public void registrarLlamado(Turno turno) {
        if (!turnoTieneDatosMinimos(turno) || turno.getCalledAt() == null) {
            return;
        }

        LocalDateTime calledAt = turno.getCalledAt();

        llamadoRepository.save(MetricaLlamadoFilaDia.builder()
                .filaId(turno.getFila().getId())
                .fecha(calledAt.toLocalDate())
                .calledAt(calledAt)
                .turnoId(turno.getId())
                .tiempoRealEspera(valorEntero(turno.getTiempoRealEsperaMinutos(), 0))
                .errorPrediccion(valorEntero(turno.getErrorPrediccionMinutos(), 0))
                .build());
    }

    @Transactional(readOnly = true)
    public long contarInscripcionesEntre(Long filaId, LocalDateTime desde, LocalDateTime hasta) {
        if (consultaInvalida(filaId, desde, hasta)) {
            return 0L;
        }

        return inscripcionRepository.countByFilaIdAndCreatedAtGreaterThanEqualAndCreatedAtBefore(
                filaId,
                desde,
                hasta
        );
    }

    @Transactional(readOnly = true)
    public long contarLlamadosEntre(Long filaId, LocalDateTime desde, LocalDateTime hasta) {
        if (consultaInvalida(filaId, desde, hasta)) {
            return 0L;
        }

        return llamadoRepository.countByFilaIdAndCalledAtGreaterThanEqualAndCalledAtBefore(
                filaId,
                desde,
                hasta
        );
    }

    @Transactional(readOnly = true)
    public long contarHistoricoPorDiaYFranja(
            Long filaId,
            LocalDateTime desde,
            LocalDateTime hasta,
            DayOfWeek diaSemana,
            Integer franjaHoraria
    ) {
        if (consultaInvalida(filaId, desde, hasta) || diaSemana == null) {
            return 0L;
        }

        int hora = franjaHoraria == null ? LocalDateTime.now().getHour() : franjaHoraria;

        return inscripcionRepository
                .countByFilaIdAndDiaSemanaAndFranjaHorariaAndCreatedAtGreaterThanEqualAndCreatedAtBefore(
                        filaId,
                        diaSemana.name(),
                        hora,
                        desde,
                        hasta
                );
    }

    @Transactional(readOnly = true)
    public double promedioErrorPrediccionDesde(Long filaId, LocalDateTime desde, LocalDateTime hasta) {
        if (consultaInvalida(filaId, desde, hasta)) {
            return 0.0;
        }

        return llamadoRepository.promedioErrorPrediccionDesde(filaId, desde, hasta);
    }

    private boolean consultaInvalida(Long filaId, LocalDateTime desde, LocalDateTime hasta) {
        return filaId == null || desde == null || hasta == null || !desde.isBefore(hasta);
    }

    private void incrementarLlegadasPorFranja(Long filaId, String diaSemana, Integer franjaHoraria, LocalDate fecha) {
        MetricaLlegadasFranjaId id = new MetricaLlegadasFranjaId(
                filaId,
                diaSemana,
                franjaHoraria,
                fecha
        );

        MetricaLlegadasFranja metrica = llegadasFranjaRepository.findById(id)
                .orElseGet(() -> MetricaLlegadasFranja.builder()
                        .filaId(filaId)
                        .diaSemana(diaSemana)
                        .franjaHoraria(franjaHoraria)
                        .fecha(fecha)
                        .cantidad(0L)
                        .build());

        metrica.setCantidad(valorLong(metrica.getCantidad(), 0L) + 1);
        llegadasFranjaRepository.save(metrica);
    }

    private boolean turnoTieneDatosMinimos(Turno turno) {
        return turno != null
                && turno.getId() != null
                && turno.getFila() != null
                && turno.getFila().getId() != null;
    }

    private String obtenerDiaSemana(Turno turno, LocalDateTime fechaHora) {
        return turno.getDiaSemana() == null ? fechaHora.getDayOfWeek().name() : turno.getDiaSemana().name();
    }

    private Integer obtenerFranjaHoraria(Turno turno, LocalDateTime fechaHora) {
        return turno.getFranjaHoraria() == null ? fechaHora.getHour() : turno.getFranjaHoraria();
    }

    private Integer valorEntero(Integer valor, Integer defaultValue) {
        return Objects.requireNonNullElse(valor, defaultValue);
    }

    private Long valorLong(Long valor, Long defaultValue) {
        return Objects.requireNonNullElse(valor, defaultValue);
    }
}
