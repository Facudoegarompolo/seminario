package com.digitalqueue.dto;

import com.digitalqueue.model.enums.EstadoTurno;
import com.digitalqueue.model.enums.QueueStatus;
import com.digitalqueue.model.enums.TipoCliente;
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
    private String nombreCliente;
    private TipoCliente tipoCliente;
    private QueueStatus queueStatus;
    private Long personasAdelante;
    private Integer tiempoEstimadoMinutos;
    private Integer tiempoEstimadoMinimoMinutos;
    private Integer tiempoEstimadoMaximoMinutos;
}
