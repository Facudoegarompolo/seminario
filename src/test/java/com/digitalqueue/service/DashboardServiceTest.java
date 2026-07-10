package com.digitalqueue.service;

import com.digitalqueue.dto.dashboard.HistoryEventResponse;
import com.digitalqueue.model.Fila;
import com.digitalqueue.model.Turno;
import com.digitalqueue.model.enums.EstadoTurno;
import com.digitalqueue.repository.FilaRepository;
import com.digitalqueue.repository.TurnoRepository;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DashboardServiceTest {

    private final FilaRepository filaRepository = mock(FilaRepository.class);
    private final TurnoRepository turnoRepository = mock(TurnoRepository.class);
    private final DashboardService service = new DashboardService(
            filaRepository,
            turnoRepository,
            mock(EstimacionEsperaService.class)
    );

    @Test
    void historialRecienteExponeFechaYHoraEnHorarioArgentina() {
        Fila fila = Fila.builder()
                .id(1L)
                .build();
        Turno turno = Turno.builder()
                .id(10L)
                .fila(fila)
                .numeroTurno(7)
                .estado(EstadoTurno.LLAMADO)
                .calledAt(LocalDateTime.of(2026, 7, 8, 3, 30))
                .build();

        when(filaRepository.findById(1L)).thenReturn(Optional.of(fila));
        when(turnoRepository.findUltimosEventos(eq(1L), anyCollection(), any(Pageable.class)))
                .thenReturn(List.of(turno));

        HistoryEventResponse event = service.obtenerHistorialReciente(1L).getFirst();

        assertEquals("00:30", event.getTime());
        assertEquals("2026-07-08T00:30:00-03:00", event.getDateTime());
    }
}
