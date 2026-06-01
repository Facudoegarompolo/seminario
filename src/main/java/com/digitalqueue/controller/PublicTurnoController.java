package com.digitalqueue.controller;

import com.digitalqueue.dto.TurnoEstadoResponse;
import com.digitalqueue.service.TurnoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/public/turnos")
@RequiredArgsConstructor
public class PublicTurnoController {

    private final TurnoService turnoService;

    @GetMapping("/{tokenPublico}")
    public ResponseEntity<TurnoEstadoResponse> obtenerEstadoTurno(
            @PathVariable String tokenPublico
    ) {
        TurnoEstadoResponse response = turnoService.obtenerEstadoTurno(tokenPublico);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{tokenPublico}")
    public ResponseEntity<TurnoEstadoResponse> cancelarTurno(
            @PathVariable String tokenPublico
    ) {
        TurnoEstadoResponse response = turnoService.cancelarTurno(tokenPublico);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{tokenPublico}/cancelar")
    public ResponseEntity<TurnoEstadoResponse> cancelarTurnoPorPost(
            @PathVariable String tokenPublico
    ) {
        TurnoEstadoResponse response = turnoService.cancelarTurno(tokenPublico);
        return ResponseEntity.ok(response);
    }
}
