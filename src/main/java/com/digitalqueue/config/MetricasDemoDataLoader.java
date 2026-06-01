package com.digitalqueue.config;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.Row;
import com.datastax.oss.driver.api.core.cql.SimpleStatement;
import com.digitalqueue.model.Fila;
import com.digitalqueue.model.enums.QueueStatus;
import com.digitalqueue.model.enums.TipoCliente;
import com.digitalqueue.repository.FilaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
@Order(2)
public class MetricasDemoDataLoader implements CommandLineRunner {

    private static final ZoneId ZONA_APP = ZoneId.systemDefault();

    private static final String INSERT_INSCRIPCION = """
            INSERT INTO metricas_inscripcion_por_fila_dia (
                fila_id,
                fecha,
                created_at,
                turno_id,
                cantidad_integrantes,
                nombre_cliente,
                tipo_cliente,
                personas_adelante,
                queue_status,
                tiempo_estimado_informado,
                tiempo_estimado_minimo,
                tiempo_estimado_maximo,
                dia_semana,
                franja_horaria
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;

    private static final String INSERT_LLAMADO = """
            INSERT INTO metricas_llamado_por_fila_dia (
                fila_id,
                fecha,
                called_at,
                turno_id,
                tiempo_real_espera,
                error_prediccion
            ) VALUES (?, ?, ?, ?, ?, ?)
            """;

    private static final String COUNT_INSCRIPCIONES = """
            SELECT count(*)
            FROM metricas_inscripcion_por_fila_dia
            WHERE fila_id = ?
              AND fecha = ?
              AND created_at >= ?
              AND created_at < ?
            """;

    private final FilaRepository filaRepository;
    private final CqlSession cqlSession;

    @Override
    public void run(String... args) {
        List<Fila> filas = filaRepository.findAll();
        if (filas.isEmpty()) {
            return;
        }

        try {
            Fila fila = filas.getFirst();
            LocalDateTime ahora = LocalDateTime.now().withSecond(0).withNano(0);

            if (contarInscripciones(fila.getId(), ahora.minusMinutes(15), ahora) == 0) {
                insertarMetricasRecientes(fila, ahora);
            }

            if (contarHistoricoMismaFranja(fila.getId(), ahora) == 0) {
                insertarMetricasHistoricas(fila, ahora);
            }
        } catch (RuntimeException ex) {
            log.warn("No se pudieron sembrar métricas demo en Cassandra: {}", ex.getMessage());
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
        cqlSession.execute(SimpleStatement.newInstance(
                INSERT_INSCRIPCION,
                fila.getId(),
                createdAt.toLocalDate(),
                toInstant(createdAt),
                turnoId,
                cantidadIntegrantes,
                "Cliente demo",
                TipoCliente.ANONIMO.name(),
                personasAdelante,
                QueueStatus.NORMAL.name(),
                tiempoEstimado,
                tiempoMinimo,
                tiempoMaximo,
                createdAt.getDayOfWeek().name(),
                createdAt.getHour()
        ));
    }

    private void insertarLlamado(Fila fila, LocalDateTime calledAt, Long turnoId, Integer tiempoReal, Integer errorPrediccion) {
        cqlSession.execute(SimpleStatement.newInstance(
                INSERT_LLAMADO,
                fila.getId(),
                calledAt.toLocalDate(),
                toInstant(calledAt),
                turnoId,
                tiempoReal,
                errorPrediccion
        ));
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
        Row row = cqlSession.execute(SimpleStatement.newInstance(
                COUNT_INSCRIPCIONES,
                filaId,
                desde.toLocalDate(),
                toInstant(desde),
                toInstant(hasta)
        )).one();

        return row == null ? 0L : row.getLong(0);
    }

    private Instant toInstant(LocalDateTime fechaHora) {
        return fechaHora.atZone(ZONA_APP).toInstant();
    }
}
