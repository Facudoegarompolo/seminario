package com.digitalqueue.controller;

import com.digitalqueue.dto.PushSubscriptionRequest;
import com.digitalqueue.service.PushNotificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/public/turnos/{tokenPublico}/push-subscriptions")
@RequiredArgsConstructor
public class PublicPushSubscriptionController {

    private final PushNotificationService pushNotificationService;

    @PostMapping
    public ResponseEntity<Void> registrarSuscripcion(
            @PathVariable String tokenPublico,
            @Valid @RequestBody PushSubscriptionRequest request
    ) {
        pushNotificationService.registrarSuscripcion(tokenPublico, request);
        return ResponseEntity.noContent().build();
    }
}
