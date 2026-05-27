package com.digitalqueue.service;

import com.digitalqueue.dto.EstimacionEspera;
import com.digitalqueue.dto.dashboard.ChartDataPointResponse;
import com.digitalqueue.dto.dashboard.DashboardStatsResponse;
import com.digitalqueue.dto.dashboard.DashboardSummaryResponse;
import com.digitalqueue.dto.dashboard.HistoryEventResponse;
import com.digitalqueue.dto.dashboard.RecentEventResponse;
import com.digitalqueue.exception.RecursoNoEncontradoException;
import com.digitalqueue.model.Fila;
import com.digitalqueue.model.Turno;
import com.digitalqueue.model.enums.EstadoTurno;
import com.digitalqueue.repository.FilaRepository;
import com.digitalqueue.repository.TurnoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.OptionalDouble;
import java.util.OptionalInt;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private static final Long FILA_DEFAULT_ID = 1L;
    private static final int REALTIME_EVENT_LIMIT = 10;
    private static final int HISTORY_EVENT_LIMIT = 20;
    private static final List<Integer> CHART_HOURS = List.of(12, 15, 18, 21);

    private static final List<EstadoTurno> ESTADOS_EN_ESPERA = List.of(
            EstadoTurno.ESPERANDO,
            EstadoTurno.PROXIMO
    );
    private static final List<EstadoTurno> ESTADOS_EVENTO_RECIENTE = List.of(
            EstadoTurno.LLAMADO,
            EstadoTurno.ATENDIENDO,
            EstadoTurno.FINALIZADO,
            EstadoTurno.NO_PRESENTADO
    );

    private static final DateTimeFormatter HORA_FORMATO =
            DateTimeFormatter.ofPattern("h:mm a", Locale.forLanguageTag("es-AR"));

    private final FilaRepository filaRepository;
    private final TurnoRepository turnoRepository;
    private final EstimacionEsperaService estimacionEsperaService;

    @Transactional(readOnly = true)
    public DashboardSummaryResponse obtenerResumen(Long filaId) {
        Fila fila = obtenerFila(filaId);
        RangoDia rango = rangoDiaActual();

        long waiting = turnoRepository.countByFilaIdAndEstadoIn(fila.getId(), ESTADOS_EN_ESPERA);
        EstimacionEspera estimacion = estimacionEsperaService.calcularEstimacion(fila, waiting, 1);

        long servedToday = contarPorEstadoEnRango(fila.getId(), EstadoTurno.FINALIZADO, rango);
        long noShows = contarPorEstadoEnRango(fila.getId(), EstadoTurno.NO_PRESENTADO, rango);

        List<RecentEventResponse> recent = turnoRepository
                .findUltimosEventos(fila.getId(), ESTADOS_EVENTO_RECIENTE, PageRequest.of(0, 4))
                .stream()
                .map(this::mapToRecentEvent)
                .toList();

        return new DashboardSummaryResponse(
                waiting,
                formatearTiempoEspera(estimacion.getTiempoEstimadoMinutos()),
                servedToday,
                noShows,
                recent
        );
    }

    @Transactional(readOnly = true)
    public List<RecentEventResponse> obtenerEventosTiempoReal(Long filaId) {
        Fila fila = obtenerFila(filaId);

        return turnoRepository
                .findUltimosEventos(fila.getId(), ESTADOS_EVENTO_RECIENTE, PageRequest.of(0, REALTIME_EVENT_LIMIT))
                .stream()
                .map(this::mapToRecentEvent)
                .toList();
    }

    @Transactional(readOnly = true)
    public DashboardStatsResponse obtenerEstadisticas(Long filaId) {
        Fila fila = obtenerFila(filaId);
        RangoDia rango = rangoDiaActual();

        long atendidosHoy = contarPorEstadoEnRango(fila.getId(), EstadoTurno.FINALIZADO, rango);
        long ausentes = contarPorEstadoEnRango(fila.getId(), EstadoTurno.NO_PRESENTADO, rango);

        List<Turno> turnosLlamados = turnoRepository.findByFilaIdAndCalledAtGreaterThanEqualAndCalledAtLessThan(
                fila.getId(),
                rango.inicio(),
                rango.fin()
        );

        OptionalDouble promedio = turnosLlamados.stream()
                .map(Turno::getTiempoRealEsperaMinutos)
                .filter(java.util.Objects::nonNull)
                .mapToInt(Integer::intValue)
                .average();

        OptionalInt maximo = turnosLlamados.stream()
                .map(Turno::getTiempoRealEsperaMinutos)
                .filter(java.util.Objects::nonNull)
                .mapToInt(Integer::intValue)
                .max();

        return new DashboardStatsResponse(
                atendidosHoy,
                promedio.isPresent() ? formatearTiempoEspera((int) Math.round(promedio.getAsDouble())) : "0 min",
                ausentes,
                maximo.isPresent() ? formatearTiempoEspera(maximo.getAsInt()) : "0 min"
        );
    }

    @Transactional(readOnly = true)
    public List<HistoryEventResponse> obtenerHistorialReciente(Long filaId) {
        Fila fila = obtenerFila(filaId);

        return turnoRepository
                .findUltimosEventos(fila.getId(), ESTADOS_EVENTO_RECIENTE, PageRequest.of(0, HISTORY_EVENT_LIMIT))
                .stream()
                .map(this::mapToHistoryEvent)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ChartDataPointResponse> obtenerGraficoActividad(Long filaId) {
        Fila fila = obtenerFila(filaId);
        LocalDate hoy = LocalDate.now();

        return CHART_HOURS.stream()
                .map(hora -> {
                    LocalDateTime inicio = hoy.atTime(hora, 0);
                    LocalDateTime fin = inicio.plusHours(3);
                    long cantidad = turnoRepository.countByFilaIdAndCreatedAtGreaterThanEqualAndCreatedAtLessThan(
                            fila.getId(),
                            inicio,
                            fin
                    );
                    return new ChartDataPointResponse(inicio.format(HORA_FORMATO), cantidad);
                })
                .toList();
    }

    private Fila obtenerFila(Long filaId) {
        Long filaIdResuelto = filaId != null ? filaId : FILA_DEFAULT_ID;
        return filaRepository.findById(filaIdResuelto)
                .orElseThrow(() -> new RecursoNoEncontradoException("Fila no encontrada"));
    }

    private RangoDia rangoDiaActual() {
        LocalDateTime inicio = LocalDate.now().atStartOfDay();
        return new RangoDia(inicio, inicio.plusDays(1));
    }

    private long contarPorEstadoEnRango(Long filaId, EstadoTurno estado, RangoDia rango) {
        return turnoRepository.countByFilaIdAndEstadoAndCompletedAtGreaterThanEqualAndCompletedAtLessThan(
                filaId,
                estado,
                rango.inicio(),
                rango.fin()
        );
    }

    private RecentEventResponse mapToRecentEvent(Turno turno) {
        LocalDateTime fechaEvento = turno.getCompletedAt() != null
                ? turno.getCompletedAt()
                : turno.getCalledAt();

        return new RecentEventResponse(
                turno.getId().toString(),
                turno.getNumeroTurno(),
                etiquetaEstado(turno.getEstado()),
                fechaEvento == null ? "" : fechaEvento.format(HORA_FORMATO)
        );
    }

    private HistoryEventResponse mapToHistoryEvent(Turno turno) {
        LocalDateTime fechaEvento = turno.getCompletedAt() != null
                ? turno.getCompletedAt()
                : turno.getCalledAt();

        return new HistoryEventResponse(
                turno.getId().toString(),
                fechaEvento == null ? "" : fechaEvento.format(HORA_FORMATO),
                textoHistorial(turno)
        );
    }

    private String textoHistorial(Turno turno) {
        return switch (turno.getEstado()) {
            case LLAMADO -> "Se llamó al #" + turno.getNumeroTurno();
            case ATENDIENDO -> "Se está atendiendo al #" + turno.getNumeroTurno();
            case FINALIZADO -> "Se atendió al #" + turno.getNumeroTurno();
            case NO_PRESENTADO -> "No se presentó el #" + turno.getNumeroTurno();
            default -> "Evento del turno #" + turno.getNumeroTurno();
        };
    }

    private String etiquetaEstado(EstadoTurno estado) {
        return switch (estado) {
            case LLAMADO -> "Llamado";
            case ATENDIENDO -> "Atendiendo";
            case FINALIZADO -> "Atendido";
            case NO_PRESENTADO -> "No se presentó";
            default -> estado.name();
        };
    }

    private String formatearTiempoEspera(int minutos) {
        if (minutos <= 0) {
            return "0 min";
        }
        return minutos + " min";
    }

    private record RangoDia(LocalDateTime inicio, LocalDateTime fin) {
    }
}
