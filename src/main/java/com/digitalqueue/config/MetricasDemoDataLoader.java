package com.digitalqueue.config;

import com.digitalqueue.model.Fila;
import com.digitalqueue.model.Turno;
import com.digitalqueue.model.enums.QueueStatus;
import com.digitalqueue.model.enums.TipoCliente;
import com.digitalqueue.repository.FilaRepository;
import com.digitalqueue.service.metrics.MetricasFilaService;
import com.digitalqueue.util.BusinessTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
@Order(2)
public class MetricasDemoDataLoader implements CommandLineRunner {

    private final FilaRepository filaRepository;
    private final MetricasFilaService metricasFilaService;

    @Override
    public void run(String... args) {
        List<Fila> filas = filaRepository.findAll();
        if (filas.isEmpty()) {
            return;
        }

        try {
            Fila fila = filas.getFirst();
            LocalDateTime ahora = BusinessTime.nowStorage().withSecond(0).withNano(0);

            if (contarInscripciones(fila.getId(), ahora.minusMinutes(15), ahora) == 0) {
                insertarMetricasRecientes(fila, ahora);
            }

            if (contarHistoricoMismaFranja(fila.getId(), ahora) == 0) {
                insertarMetricasHistoricas(fila, ahora);
            }
        } catch (RuntimeException ex) {
            log.warn("No se pudieron sembrar metricas demo en SQL: {}", ex.getMessage());
        }
    }

    private void insertarMetricasRecientes(Fila fila, LocalDateTime ahora) {
        insertarInscripcion(fila, ahora.minusMinutes(12), -1L, 1, 0L, 0, 0, 0);
        insertarInscripcion(fila, ahora.minusMinutes(6), -2L, 2, 1L, 3, 3, 4);
        insertarLlamado(fila, ahora.minusMinutes(10), -101L, 3, 1);
        insertarLlamado(fila, ahora.minusMinutes(4), -102L, 4, 1);
    }

    private void insertarMetricasHistoricas(Fila fila, LocalDateTime ahora) {
        for (int semana = 1; semana <= 4; semana++) {
            LocalDateTime base = ahora.minusWeeks(semana).withMinute(5);

            for (int indice = 0; indice < 8; indice++) {
                LocalDateTime createdAt = base.plusMinutes(indice * 6L);
                long turnoId = -1000L - (semana * 100L) - indice;
                long personasAdelante = indice % 4L;
                int estimado = (int) personasAdelante * 3;
                insertarInscripcion(fila, createdAt, turnoId, 1 + (indice % 2), personasAdelante, estimado, estimado, estimado + 2);
            }
        }
    }

    private void insertarInscripcion(
            Fila fila,
            LocalDateTime createdAt,
            Long turnoId,
            Integer cantidadIntegrantes,
            Long personasAdelante,
            Integer tiempoEstimado,
            Integer tiempoMinimo,
            Integer tiempoMaximo
    ) {
        Turno turno = Turno.builder()
                .id(turnoId)
                .fila(fila)
                .createdAt(createdAt)
                .cantidadIntegrantes(cantidadIntegrantes)
                .nombreCliente("Cliente demo")
                .tipoCliente(TipoCliente.ANONIMO)
                .personasAdelanteAlAnotarse(personasAdelante)
                .queueStatusAlAnotarse(QueueStatus.NORMAL)
                .tiempoEstimadoInformadoMinutos(tiempoEstimado)
                .tiempoEstimadoMinimoMinutos(tiempoMinimo)
                .tiempoEstimadoMaximoMinutos(tiempoMaximo)
                .diaSemana(BusinessTime.storageToBusiness(createdAt).getDayOfWeek())
                .franjaHoraria(BusinessTime.storageToBusiness(createdAt).getHour())
                .build();

        metricasFilaService.registrarInscripcion(turno);
    }

    private void insertarLlamado(Fila fila, LocalDateTime calledAt, Long turnoId, Integer tiempoReal, Integer errorPrediccion) {
        Turno turno = Turno.builder()
                .id(turnoId)
                .fila(fila)
                .createdAt(calledAt.minusMinutes(tiempoReal == null ? 0 : tiempoReal))
                .calledAt(calledAt)
                .tiempoRealEsperaMinutos(tiempoReal)
                .errorPrediccionMinutos(errorPrediccion)
                .build();

        metricasFilaService.registrarLlamado(turno);
    }

    private long contarHistoricoMismaFranja(Long filaId, LocalDateTime ahora) {
        long total = 0L;

        for (int semana = 1; semana <= 4; semana++) {
            LocalDateTime desde = ahora.minusWeeks(semana).withMinute(0).withSecond(0).withNano(0);
            LocalDateTime hasta = desde.plusHours(1);
            total += contarInscripciones(filaId, desde, hasta);
        }

        return total;
    }

    private long contarInscripciones(Long filaId, LocalDateTime desde, LocalDateTime hasta) {
        return metricasFilaService.contarInscripcionesEntre(filaId, desde, hasta);
    }
}
