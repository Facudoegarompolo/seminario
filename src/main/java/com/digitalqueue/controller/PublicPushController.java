package com.digitalqueue.controller;

import com.digitalqueue.dto.PushPublicKeyResponse;
import com.digitalqueue.service.PushNotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/public/push")
@RequiredArgsConstructor
public class PublicPushController {

    private final PushNotificationService pushNotificationService;

    @GetMapping("/public-key")
    public ResponseEntity<PushPublicKeyResponse> obtenerClavePublica() {
        return ResponseEntity.ok(new PushPublicKeyResponse(pushNotificationService.obtenerClavePublica()));
    }
}
