package com.digitalqueue.service;

import com.digitalqueue.dto.EstimacionEspera;
import com.digitalqueue.dto.dashboard.DashboardSummaryResponse;
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

@Service
@RequiredArgsConstructor
public class DashboardService {

    private static final Long FILA_DEFAULT_ID = 1L;
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
        Long filaIdResuelto = filaId != null ? filaId : FILA_DEFAULT_ID;
        Fila fila = filaRepository.findById(filaIdResuelto)
                .orElseThrow(() -> new RecursoNoEncontradoException("Fila no encontrada"));

        long waiting = turnoRepository.countByFilaIdAndEstadoIn(fila.getId(), ESTADOS_EN_ESPERA);
        EstimacionEspera estimacion = estimacionEsperaService.calcularEstimacion(fila, waiting, 1);

        LocalDateTime inicioDia = LocalDate.now().atStartOfDay();
        LocalDateTime finDia = inicioDia.plusDays(1);

        long servedToday = turnoRepository.countByFilaIdAndEstadoAndCompletedAtGreaterThanEqualAndCompletedAtLessThan(
                fila.getId(),
                EstadoTurno.FINALIZADO,
                inicioDia,
                finDia
        );
        long noShows = turnoRepository.countByFilaIdAndEstadoAndCompletedAtGreaterThanEqualAndCompletedAtLessThan(
                fila.getId(),
                EstadoTurno.NO_PRESENTADO,
                inicioDia,
                finDia
        );

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
}
