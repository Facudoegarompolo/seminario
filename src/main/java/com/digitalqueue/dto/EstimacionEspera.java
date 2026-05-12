package com.digitalqueue.dto;

import com.digitalqueue.model.enums.QueueStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EstimacionEspera {

    private QueueStatus queueStatus;
    private Integer tiempoEstimadoMinutos;
    private Integer tiempoEstimadoMinimoMinutos;
    private Integer tiempoEstimadoMaximoMinutos;
    private Double tiempoPorPersonaMinutos;
}
