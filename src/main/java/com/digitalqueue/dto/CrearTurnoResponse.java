package com.digitalqueue.dto;

import com.digitalqueue.model.enums.EstadoTurno;
import com.digitalqueue.model.enums.QueueStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CrearTurnoResponse {

    private Long turnoId;
    private Integer numeroTurno;
    private String tokenPublico;
    private EstadoTurno estado;
    private QueueStatus queueStatus;
    private Long personasAdelante;
    private Integer tiempoEstimadoMinutos;
    private Integer tiempoEstimadoMinimoMinutos;
    private Integer tiempoEstimadoMaximoMinutos;
}
