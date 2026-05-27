package com.digitalqueue.controller;

import com.digitalqueue.dto.dashboard.ChartDataPointResponse;
import com.digitalqueue.dto.dashboard.DashboardStatsResponse;
import com.digitalqueue.dto.dashboard.DashboardSummaryResponse;
import com.digitalqueue.dto.dashboard.HistoryEventResponse;
import com.digitalqueue.dto.dashboard.RecentEventResponse;
import com.digitalqueue.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

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

    @GetMapping("/realtime")
    public ResponseEntity<List<RecentEventResponse>> obtenerEventosTiempoReal(
            @RequestParam(required = false) Long filaId
    ) {
        return ResponseEntity.ok(dashboardService.obtenerEventosTiempoReal(filaId));
    }

    @GetMapping("/stats")
    public ResponseEntity<DashboardStatsResponse> obtenerEstadisticas(
            @RequestParam(required = false) Long filaId
    ) {
        return ResponseEntity.ok(dashboardService.obtenerEstadisticas(filaId));
    }

    @GetMapping("/recent-events")
    public ResponseEntity<List<HistoryEventResponse>> obtenerHistorialReciente(
            @RequestParam(required = false) Long filaId
    ) {
        return ResponseEntity.ok(dashboardService.obtenerHistorialReciente(filaId));
    }

    @GetMapping("/activity-chart")
    public ResponseEntity<List<ChartDataPointResponse>> obtenerGraficoActividad(
            @RequestParam(required = false) Long filaId
    ) {
        return ResponseEntity.ok(dashboardService.obtenerGraficoActividad(filaId));
    }
}
