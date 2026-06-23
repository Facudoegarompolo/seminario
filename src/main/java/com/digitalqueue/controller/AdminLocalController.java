package com.digitalqueue.controller;

import com.digitalqueue.dto.ActualizarLogoLocalRequest;
import com.digitalqueue.dto.ActualizarLogoLocalResponse;
import com.digitalqueue.service.LocalService;
import com.digitalqueue.service.AdminAccessService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/locales")
@RequiredArgsConstructor
public class AdminLocalController {

    private final LocalService localService;
    private final AdminAccessService adminAccessService;

    @PatchMapping("/{localId}/logo")
    public ResponseEntity<ActualizarLogoLocalResponse> actualizarLogo(
            @PathVariable Long localId,
            @Valid @RequestBody ActualizarLogoLocalRequest request
    ) {
        adminAccessService.validarLocal(localId);
        ActualizarLogoLocalResponse response = localService.actualizarLogo(localId, request);
        return ResponseEntity.ok(response);
    }
}
