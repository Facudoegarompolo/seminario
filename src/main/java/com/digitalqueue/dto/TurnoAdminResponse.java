package com.digitalqueue.dto;

import com.digitalqueue.model.enums.EstadoTurno;
import com.digitalqueue.model.enums.QueueStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TurnoAdminResponse {

    private Long turnoId;
    private Integer numeroTurno;
    private EstadoTurno estado;
    private QueueStatus queueStatusAlAnotarse;
    private Integer cantidadIntegrantes;
    private Long personasAdelanteAlAnotarse;
    private Integer tiempoEstimadoInformadoMinutos;
    private Integer tiempoRealEsperaMinutos;
    private Integer errorPrediccionMinutos;
    private LocalDateTime createdAt;
    private LocalDateTime calledAt;
    private LocalDateTime completedAt;
}
