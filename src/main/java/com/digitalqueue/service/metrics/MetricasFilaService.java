package com.digitalqueue.service.metrics;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.ResultSet;
import com.datastax.oss.driver.api.core.cql.Row;
import com.datastax.oss.driver.api.core.cql.SimpleStatement;
import com.digitalqueue.model.Turno;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class MetricasFilaService {

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

    private static final String UPDATE_LLEGADAS_POR_FRANJA = """
            UPDATE metricas_llegadas_por_franja
            SET cantidad = cantidad + 1
            WHERE fila_id = ?
              AND dia_semana = ?
              AND franja_horaria = ?
              AND fecha = ?
            """;

    private static final String COUNT_INSCRIPCIONES_POR_FECHA = """
            SELECT count(*)
            FROM metricas_inscripcion_por_fila_dia
            WHERE fila_id = ?
              AND fecha = ?
              AND created_at >= ?
              AND created_at < ?
            """;

    private static final String COUNT_LLAMADOS_POR_FECHA = """
            SELECT count(*)
            FROM metricas_llamado_por_fila_dia
            WHERE fila_id = ?
              AND fecha = ?
              AND called_at >= ?
              AND called_at < ?
            """;

    private static final String SELECT_ERRORES_POR_FECHA = """
            SELECT error_prediccion
            FROM metricas_llamado_por_fila_dia
            WHERE fila_id = ?
              AND fecha = ?
              AND called_at >= ?
              AND called_at < ?
            """;

    private final CqlSession cqlSession;

    public void registrarInscripcion(Turno turno) {
        if (!turnoTieneDatosMinimos(turno) || turno.getCreatedAt() == null) {
            return;
        }

        LocalDateTime createdAt = turno.getCreatedAt();
        LocalDate fecha = createdAt.toLocalDate();
        String diaSemana = obtenerDiaSemana(turno, createdAt);
        Integer franjaHoraria = obtenerFranjaHoraria(turno, createdAt);

        cqlSession.execute(SimpleStatement.newInstance(
                INSERT_INSCRIPCION,
                turno.getFila().getId(),
                fecha,
                toInstant(createdAt),
                turno.getId(),
                valorEntero(turno.getCantidadIntegrantes(), 1),
                turno.getNombreCliente(),
                turno.getTipoCliente() == null ? null : turno.getTipoCliente().name(),
                valorLong(turno.getPersonasAdelanteAlAnotarse(), 0L),
                turno.getQueueStatusAlAnotarse() == null ? null : turno.getQueueStatusAlAnotarse().name(),
                turno.getTiempoEstimadoInformadoMinutos(),
                turno.getTiempoEstimadoMinimoMinutos(),
                turno.getTiempoEstimadoMaximoMinutos(),
                diaSemana,
                franjaHoraria
        ));

        cqlSession.execute(SimpleStatement.newInstance(
                UPDATE_LLEGADAS_POR_FRANJA,
                turno.getFila().getId(),
                diaSemana,
                franjaHoraria,
                fecha
        ));
    }

    public void registrarLlamado(Turno turno) {
        if (!turnoTieneDatosMinimos(turno) || turno.getCalledAt() == null) {
            return;
        }

        LocalDateTime calledAt = turno.getCalledAt();

        cqlSession.execute(SimpleStatement.newInstance(
                INSERT_LLAMADO,
                turno.getFila().getId(),
                calledAt.toLocalDate(),
                toInstant(calledAt),
                turno.getId(),
                valorEntero(turno.getTiempoRealEsperaMinutos(), 0),
                valorEntero(turno.getErrorPrediccionMinutos(), 0)
        ));
    }

    public long contarInscripcionesEntre(Long filaId, LocalDateTime desde, LocalDateTime hasta) {
        return sumarPorDia(filaId, desde, hasta, COUNT_INSCRIPCIONES_POR_FECHA);
    }

    public long contarLlamadosEntre(Long filaId, LocalDateTime desde, LocalDateTime hasta) {
        return sumarPorDia(filaId, desde, hasta, COUNT_LLAMADOS_POR_FECHA);
    }

    public long contarHistoricoPorDiaYFranja(
            Long filaId,
            LocalDateTime desde,
            LocalDateTime hasta,
            DayOfWeek diaSemana,
            Integer franjaHoraria
    ) {
        if (filaId == null || desde == null || hasta == null || !desde.isBefore(hasta)) {
            return 0L;
        }

        int hora = franjaHoraria == null ? LocalDateTime.now().getHour() : franjaHoraria;
        long total = 0L;
        LocalDate fecha = desde.toLocalDate();
        LocalDate fechaHasta = hasta.toLocalDate();

        while (!fecha.isAfter(fechaHasta)) {
            if (fecha.getDayOfWeek() == diaSemana) {
                LocalDateTime inicioFranja = fecha.atTime(hora, 0);
                LocalDateTime finFranja = inicioFranja.plusHours(1);
                LocalDateTime inicioConsulta = max(inicioFranja, desde);
                LocalDateTime finConsulta = min(finFranja, hasta);

                if (inicioConsulta.isBefore(finConsulta)) {
                    total += contarInscripcionesEntre(filaId, inicioConsulta, finConsulta);
                }
            }
            fecha = fecha.plusDays(1);
        }

        return total;
    }

    public double promedioErrorPrediccionDesde(Long filaId, LocalDateTime desde, LocalDateTime hasta) {
        if (filaId == null || desde == null || hasta == null || !desde.isBefore(hasta)) {
            return 0.0;
        }

        long suma = 0L;
        long cantidad = 0L;
        LocalDate fecha = desde.toLocalDate();
        LocalDate fechaHasta = hasta.toLocalDate();

        while (!fecha.isAfter(fechaHasta)) {
            LocalDateTime inicioDia = fecha.atStartOfDay();
            LocalDateTime finDia = inicioDia.plusDays(1);
            LocalDateTime inicioConsulta = max(desde, inicioDia);
            LocalDateTime finConsulta = min(hasta, finDia);

            if (inicioConsulta.isBefore(finConsulta)) {
                ResultSet resultSet = cqlSession.execute(SimpleStatement.newInstance(
                        SELECT_ERRORES_POR_FECHA,
                        filaId,
                        fecha,
                        toInstant(inicioConsulta),
                        toInstant(finConsulta)
                ));

                for (Row row : resultSet) {
                    if (!row.isNull("error_prediccion")) {
                        suma += row.getInt("error_prediccion");
                        cantidad++;
                    }
                }
            }

            fecha = fecha.plusDays(1);
        }

        return cantidad == 0 ? 0.0 : suma / (double) cantidad;
    }

    private long sumarPorDia(Long filaId, LocalDateTime desde, LocalDateTime hasta, String cql) {
        if (filaId == null || desde == null || hasta == null || !desde.isBefore(hasta)) {
            return 0L;
        }

        long total = 0L;
        LocalDate fecha = desde.toLocalDate();
        LocalDate fechaHasta = hasta.toLocalDate();

        while (!fecha.isAfter(fechaHasta)) {
            LocalDateTime inicioDia = fecha.atStartOfDay();
            LocalDateTime finDia = inicioDia.plusDays(1);
            LocalDateTime inicioConsulta = max(desde, inicioDia);
            LocalDateTime finConsulta = min(hasta, finDia);

            if (inicioConsulta.isBefore(finConsulta)) {
                ResultSet resultSet = cqlSession.execute(SimpleStatement.newInstance(
                        cql,
                        filaId,
                        fecha,
                        toInstant(inicioConsulta),
                        toInstant(finConsulta)
                ));
                Row row = resultSet.one();
                total += row == null ? 0L : row.getLong(0);
            }

            fecha = fecha.plusDays(1);
        }

        return total;
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

    private Instant toInstant(LocalDateTime fechaHora) {
        return fechaHora.atZone(ZONA_APP).toInstant();
    }

    private Integer valorEntero(Integer valor, Integer defaultValue) {
        return Objects.requireNonNullElse(valor, defaultValue);
    }

    private Long valorLong(Long valor, Long defaultValue) {
        return Objects.requireNonNullElse(valor, defaultValue);
    }

    private LocalDateTime max(LocalDateTime a, LocalDateTime b) {
        return a.isAfter(b) ? a : b;
    }

    private LocalDateTime min(LocalDateTime a, LocalDateTime b) {
        return a.isBefore(b) ? a : b;
    }
}
