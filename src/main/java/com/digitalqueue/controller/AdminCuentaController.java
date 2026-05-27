package com.digitalqueue.controller;

import com.digitalqueue.dto.CrearAdminRequest;
import com.digitalqueue.dto.CrearAdminResponse;
import com.digitalqueue.service.UsuarioAdminService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/cuentas")
@RequiredArgsConstructor
public class AdminCuentaController {

    private final UsuarioAdminService usuarioAdminService;

    @PostMapping
    public ResponseEntity<CrearAdminResponse> crearCuentaAdministrador(
            @Valid @RequestBody CrearAdminRequest request
    ) {
        CrearAdminResponse response = usuarioAdminService.crearCuentaAdministrador(request);
        if (Boolean.TRUE.equals(response.getCreado())) {
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        }
        return ResponseEntity.ok(response);
    }
}
