package com.digitalqueue.controller;

import com.digitalqueue.dto.dashboard.DashboardSummaryResponse;
import com.digitalqueue.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/dashboard")
@RequiredArgsConstructor
public class AdminDashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/summary")
    public ResponseEntity<DashboardSummaryResponse> obtenerResumen(
            @RequestParam(required = false) Long filaId
    ) {
        return ResponseEntity.ok(dashboardService.obtenerResumen(filaId));
    }
}
