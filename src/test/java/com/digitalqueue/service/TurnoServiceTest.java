package com.digitalqueue.service;

import com.digitalqueue.dto.TurnoEstadoResponse;
import com.digitalqueue.model.Fila;
import com.digitalqueue.model.Local;
import com.digitalqueue.model.PuntoAcceso;
import com.digitalqueue.model.Turno;
import com.digitalqueue.model.enums.EstadoFila;
import com.digitalqueue.model.enums.EstadoTurno;
import com.digitalqueue.model.enums.QueueStatus;
import com.digitalqueue.model.enums.TipoCliente;
import com.digitalqueue.repository.LocalRepository;
import com.digitalqueue.repository.PuntoAccesoRepository;
import com.digitalqueue.repository.TurnoRepository;
import com.digitalqueue.service.metrics.MetricasFilaService;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class TurnoServiceTest {

    private final TurnoRepository turnoRepository = mock(TurnoRepository.class);
    private final PuntoAccesoRepository puntoAccesoRepository = mock(PuntoAccesoRepository.class);
    private final TurnoService service = new TurnoService(
            turnoRepository,
            puntoAccesoRepository,
            mock(LocalRepository.class),
            mock(EstimacionEsperaService.class),
            mock(MetricasFilaService.class),
            mock(PushNotificationService.class)
    );

    @Test
    void estadoDelTurnoIncluyeElRestaurantePublico() {
        Local local = Local.builder()
                .id(4L)
                .nombre("Cafe Central")
                .direccion("Calle 123")
                .activo(true)
                .build();
        Fila fila = Fila.builder()
                .id(7L)
                .local(local)
                .nombre("Fila principal")
                .estado(EstadoFila.ABIERTA)
                .queueStatus(QueueStatus.NORMAL)
                .tiempoPromedioAtencionMinutos(5)
                .build();
        Turno turno = Turno.builder()
                .id(10L)
                .fila(fila)
                .numeroTurno(8)
                .tokenPublico("turno-test")
                .estado(EstadoTurno.CANCELADO)
                .nombreCliente("Ana")
                .tipoCliente(TipoCliente.ANONIMO)
                .cantidadIntegrantes(1)
                .build();
        PuntoAcceso puntoAcceso = PuntoAcceso.builder()
                .id(12L)
                .fila(fila)
                .codigoPublico("cafe-central")
                .activo(true)
                .build();

        when(turnoRepository.findByTokenPublico("turno-test")).thenReturn(Optional.of(turno));
        when(puntoAccesoRepository.findFirstByFilaIdAndActivoTrueOrderByIdAsc(7L))
                .thenReturn(Optional.of(puntoAcceso));

        TurnoEstadoResponse response = service.obtenerEstadoTurno("turno-test");

        assertEquals("cafe-central", response.getCodigoPublico());
        assertEquals("Cafe Central", response.getNombreLocal());
    }
}
