package com.digitalqueue.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DashboardStatsResponse {

    private long atendidosHoy;
    private String tiempoPromedio;
    private long ausentes;
    private String maximoEspera;
}
