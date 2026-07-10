package com.digitalqueue.dto;

import com.digitalqueue.model.enums.EstadoTurno;
import com.digitalqueue.model.enums.QueueStatus;
import com.digitalqueue.model.enums.TipoCliente;
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
    private String nombreCliente;
    private TipoCliente tipoCliente;
    private Long personasAdelanteAlAnotarse;
    private Integer tiempoEstimadoInformadoMinutos;
    private Integer tiempoRealEsperaMinutos;
    private Integer errorPrediccionMinutos;
    private LocalDateTime createdAt;
    private LocalDateTime calledAt;
    private LocalDateTime completedAt;
    private Boolean prioridad;
    private LocalDateTime fechaSolicitudPrioridad;
    private Boolean ocultoEnFila;

}
