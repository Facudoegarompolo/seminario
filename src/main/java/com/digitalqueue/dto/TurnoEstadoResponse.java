package com.digitalqueue.dto;

import com.digitalqueue.model.enums.EstadoTurno;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TurnoEstadoResponse {

    private Long turnoId;
    private Long filaId;
    private Integer numeroTurno;
    private EstadoTurno estado;
    private Long personasAdelante;
    private Integer tiempoEstimadoMinutos;
}