package com.digitalqueue.controller;

import com.digitalqueue.dto.LimpiarFilaResponse;
import com.digitalqueue.dto.TurnoAdminResponse;
import com.digitalqueue.dto.TurnoEstadoResponse;
import com.digitalqueue.service.TurnoService;
import com.digitalqueue.service.AdminAccessService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminFilaController {

    private final TurnoService turnoService;
    private final AdminAccessService adminAccessService;

    @GetMapping("/fila/turnos")
    public ResponseEntity<List<TurnoAdminResponse>> obtenerTurnosDeFilaActual() {
        Long filaId = adminAccessService.resolverFilaId(null);
        return ResponseEntity.ok(turnoService.obtenerTurnosDeFila(filaId));
    }

    @PostMapping("/fila/llamar-siguiente")
    public ResponseEntity<TurnoEstadoResponse> llamarSiguienteDeFilaActual() {
        Long filaId = adminAccessService.resolverFilaId(null);
        return ResponseEntity.ok(turnoService.llamarSiguiente(filaId));
    }

    @GetMapping("/filas/{filaId}/turnos")
    public ResponseEntity<List<TurnoAdminResponse>> obtenerTurnosDeFila(
            @PathVariable Long filaId
    ) {
        Long filaIdAutorizada = adminAccessService.resolverFilaId(filaId);
        List<TurnoAdminResponse> response = turnoService.obtenerTurnosDeFila(filaIdAutorizada);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/filas/{filaId}/llamar-siguiente")
    public ResponseEntity<TurnoEstadoResponse> llamarSiguiente(
            @PathVariable Long filaId
    ) {
        Long filaIdAutorizada = adminAccessService.resolverFilaId(filaId);
        TurnoEstadoResponse response = turnoService.llamarSiguiente(filaIdAutorizada);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/turnos/{turnoId}/finalizar")
    public ResponseEntity<TurnoEstadoResponse> finalizarTurno(
            @PathVariable Long turnoId
    ) {
        adminAccessService.validarTurno(turnoId);
        TurnoEstadoResponse response = turnoService.finalizarTurno(turnoId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/turnos/{turnoId}/no-presentado")
    public ResponseEntity<TurnoEstadoResponse> marcarNoPresentado(
            @PathVariable Long turnoId
    ) {
        adminAccessService.validarTurno(turnoId);
        TurnoEstadoResponse response = turnoService.marcarNoPresentado(turnoId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/fila/limpiar-atendidos")
    public ResponseEntity<LimpiarFilaResponse> limpiarAtendidosDeFilaActual() {
        Long filaId = adminAccessService.resolverFilaId(null);
        return ResponseEntity.ok(turnoService.limpiarTurnosTerminadosDeFila(filaId));
    }

    @PostMapping("/filas/{filaId}/limpiar-atendidos")
    public ResponseEntity<LimpiarFilaResponse> limpiarAtendidos(
            @PathVariable Long filaId
    ) {
        Long filaIdAutorizada = adminAccessService.resolverFilaId(filaId);
        return ResponseEntity.ok(turnoService.limpiarTurnosTerminadosDeFila(filaIdAutorizada));
    }

    @DeleteMapping("/turnos/{turnoId}/fila-virtual")
    public ResponseEntity<Void> quitarTurnoDeFilaVirtual(
            @PathVariable Long turnoId
    ) {
        adminAccessService.validarTurno(turnoId);
        turnoService.ocultarTurnoEnFila(turnoId);
        return ResponseEntity.noContent().build();
    }
}
