package com.digitalqueue.controller;

import com.digitalqueue.dto.PushEndpointRequest;
import com.digitalqueue.dto.TurnoActivoPushResponse;
import com.digitalqueue.service.PushNotificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/public/push-subscriptions")
@RequiredArgsConstructor
public class PublicPushRecoveryController {

    private final PushNotificationService pushNotificationService;

    @PostMapping("/turno-activo")
    public ResponseEntity<TurnoActivoPushResponse> recuperarTurnoActivo(
            @Valid @RequestBody PushEndpointRequest request
    ) {
        String tokenPublico = pushNotificationService.recuperarTokenTurnoActivo(request.getEndpoint());
        return ResponseEntity.ok(new TurnoActivoPushResponse(tokenPublico));
    }
}
