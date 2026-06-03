package com.digitalqueue.model.metrics;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class MetricaLlegadasFranjaId implements Serializable {

    private Long filaId;
    private String diaSemana;
    private Integer franjaHoraria;
    private LocalDate fecha;
}
