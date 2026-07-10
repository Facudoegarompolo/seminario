package com.digitalqueue.service;

import com.digitalqueue.dto.TurnoEstadoResponse;
import com.digitalqueue.dto.LimpiarFilaResponse;
import com.digitalqueue.model.Fila;
import com.digitalqueue.model.Local;
import com.digitalqueue.model.PuntoAcceso;
import com.digitalqueue.model.Turno;
import com.digitalqueue.model.enums.EstadoFila;
import com.digitalqueue.model.enums.EstadoTurno;
import com.digitalqueue.model.enums.QueueStatus;
import com.digitalqueue.model.enums.TipoCliente;
import com.digitalqueue.model.enums.TipoNotificacion;
import com.digitalqueue.model.enums.TipoOperacionLocal;
import com.digitalqueue.repository.LocalRepository;
import com.digitalqueue.repository.PuntoAccesoRepository;
import com.digitalqueue.repository.TurnoRepository;
import com.digitalqueue.service.metrics.MetricasFilaService;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TurnoServiceTest {

    private final TurnoRepository turnoRepository = mock(TurnoRepository.class);
    private final PuntoAccesoRepository puntoAccesoRepository = mock(PuntoAccesoRepository.class);
    private final LocalRepository localRepository = mock(LocalRepository.class);
    private final PushNotificationService pushNotificationService = mock(PushNotificationService.class);
    private final TurnoService service = new TurnoService(
            turnoRepository,
            puntoAccesoRepository,
            localRepository,
            mock(EstimacionEsperaService.class),
            mock(MetricasFilaService.class),
            pushNotificationService
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

    @Test
    void permiteCancelarUnTurnoQueYaFueLlamado() {
        Local local = Local.builder()
                .id(4L)
                .nombre("Cafe Central")
                .build();
        Fila fila = Fila.builder()
                .id(7L)
                .local(local)
                .queueStatus(QueueStatus.NORMAL)
                .build();
        Turno turno = Turno.builder()
                .id(10L)
                .fila(fila)
                .numeroTurno(8)
                .tokenPublico("turno-llamado")
                .estado(EstadoTurno.LLAMADO)
                .nombreCliente("Ana")
                .cantidadIntegrantes(1)
                .build();

        when(turnoRepository.findByTokenPublico("turno-llamado")).thenReturn(Optional.of(turno));
        when(turnoRepository.save(turno)).thenReturn(turno);

        TurnoEstadoResponse response = service.cancelarTurno("turno-llamado");

        assertEquals(EstadoTurno.CANCELADO, response.getEstado());
        verify(pushNotificationService).registrarNotificacionPendiente(
                turno,
                com.digitalqueue.model.enums.TipoNotificacion.TURNO_CANCELADO
        );
    }

    @Test
    void noPermiteLlamarOtroTurnoMientrasHayUnoLlamado() {
        when(turnoRepository.existsByFilaIdAndEstadoIn(eq(7L), any()))
                .thenReturn(true);

        assertThrows(
                com.digitalqueue.exception.OperacionInvalidaException.class,
                () -> service.llamarSiguiente(7L)
        );
    }

    @Test
    void finalizarTurnoLlamadoEnviaNotificacionCorrecta() {
        Turno turno = turnoLlamado(TipoOperacionLocal.ATENCION_RAPIDA);
        when(turnoRepository.findById(10L)).thenReturn(Optional.of(turno));
        when(turnoRepository.save(turno)).thenReturn(turno);

        TurnoEstadoResponse response = service.finalizarTurno(10L);

        assertEquals(EstadoTurno.FINALIZADO, response.getEstado());
        verify(pushNotificationService).registrarNotificacionPendiente(
                turno,
                TipoNotificacion.TURNO_FINALIZADO
        );
    }

    @Test
    void noPresentadoDescuentaOcupacionYNotifica() {
        Turno turno = turnoLlamado(TipoOperacionLocal.CONSUMO_EN_LOCAL);
        turno.getFila().getLocal().setPersonasActuales(3);
        turno.setCantidadIntegrantes(2);
        when(turnoRepository.findById(10L)).thenReturn(Optional.of(turno));
        when(turnoRepository.save(turno)).thenReturn(turno);

        TurnoEstadoResponse response = service.marcarNoPresentado(10L);

        assertEquals(EstadoTurno.NO_PRESENTADO, response.getEstado());
        assertEquals(1, turno.getFila().getLocal().getPersonasActuales());
        verify(localRepository).save(turno.getFila().getLocal());
        verify(pushNotificationService).registrarNotificacionPendiente(
                turno,
                TipoNotificacion.NO_PRESENTADO
        );
    }

    @Test
    void limpiarTurnosTerminadosSoloLosOcultaDeLaFilaVirtual() {
        Turno finalizado = turnoConEstado(EstadoTurno.FINALIZADO);
        Turno noPresentado = turnoConEstado(EstadoTurno.NO_PRESENTADO);

        when(turnoRepository.findVisiblesByFilaIdAndEstadoInOrderByCreatedAtAsc(eq(7L), any()))
                .thenReturn(List.of(finalizado, noPresentado));

        LimpiarFilaResponse response = service.limpiarTurnosTerminadosDeFila(7L);

        assertEquals(2, response.getTurnosOcultados());
        assertEquals(true, finalizado.getOcultoEnFila());
        assertEquals(true, noPresentado.getOcultoEnFila());
        verify(turnoRepository).saveAll(List.of(finalizado, noPresentado));
    }

    @Test
    void noPermiteQuitarDeFilaVirtualTurnosActivos() {
        Turno turno = turnoConEstado(EstadoTurno.ESPERANDO);
        when(turnoRepository.findById(10L)).thenReturn(Optional.of(turno));

        assertThrows(
                com.digitalqueue.exception.OperacionInvalidaException.class,
                () -> service.ocultarTurnoEnFila(10L)
        );

        verify(turnoRepository, never()).save(turno);
    }

    private Turno turnoLlamado(TipoOperacionLocal tipoOperacion) {
        Local local = Local.builder()
                .id(4L)
                .nombre("Cafe Central")
                .tipoOperacion(tipoOperacion)
                .build();
        Fila fila = Fila.builder()
                .id(7L)
                .local(local)
                .queueStatus(QueueStatus.NORMAL)
                .build();
        return Turno.builder()
                .id(10L)
                .fila(fila)
                .numeroTurno(8)
                .tokenPublico("turno-llamado")
                .estado(EstadoTurno.LLAMADO)
                .nombreCliente("Ana")
                .cantidadIntegrantes(1)
                .build();
    }

    private Turno turnoConEstado(EstadoTurno estado) {
        Turno turno = turnoLlamado(TipoOperacionLocal.ATENCION_RAPIDA);
        turno.setEstado(estado);
        turno.setOcultoEnFila(false);
        return turno;
    }
}
