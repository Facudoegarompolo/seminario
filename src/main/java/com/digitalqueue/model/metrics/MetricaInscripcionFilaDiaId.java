package com.digitalqueue.model.metrics;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class MetricaInscripcionFilaDiaId implements Serializable {

    private Long filaId;
    private LocalDate fecha;
    private LocalDateTime createdAt;
    private Long turnoId;
}
