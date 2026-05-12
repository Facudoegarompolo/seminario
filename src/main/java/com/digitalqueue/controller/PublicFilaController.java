package com.digitalqueue.controller;

import com.digitalqueue.dto.CrearTurnoRequest;
import com.digitalqueue.dto.CrearTurnoResponse;
import com.digitalqueue.dto.FilaEstadoResponse;
import com.digitalqueue.service.FilaService;
import com.digitalqueue.service.TurnoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/public/filas")
@RequiredArgsConstructor
public class PublicFilaController {

    private final FilaService filaService;
    private final TurnoService turnoService;

    @GetMapping("/{codigoPublico}/estado")
    public ResponseEntity<FilaEstadoResponse> obtenerEstadoFila(
            @PathVariable String codigoPublico
    ) {
        FilaEstadoResponse response = filaService.obtenerEstadoFila(codigoPublico);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{codigoPublico}/turnos")
    public ResponseEntity<CrearTurnoResponse> crearTurnoAnonimo(
            @PathVariable String codigoPublico,
            @Valid @RequestBody(required = false) CrearTurnoRequest request
    ) {
        CrearTurnoResponse response = turnoService.crearTurnoAnonimo(codigoPublico, request);
        return ResponseEntity.ok(response);
    }
}
