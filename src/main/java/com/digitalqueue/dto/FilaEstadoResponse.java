package com.digitalqueue.dto;

import com.digitalqueue.model.enums.EstadoFila;
import com.digitalqueue.model.enums.QueueStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FilaEstadoResponse {

    private Long filaId;
    private String nombreLocal;
    private String nombreFila;
    private EstadoFila estado;
    private QueueStatus queueStatus;
    private Long personasEsperando;
    private Integer tiempoEstimadoMinutos;
    private Integer tiempoEstimadoMinimoMinutos;
    private Integer tiempoEstimadoMaximoMinutos;
}
