package com.digitalqueue.model.metrics;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "metricas_llegadas_por_franja")
@IdClass(MetricaLlegadasFranjaId.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MetricaLlegadasFranja {

    @Id
    @Column(name = "fila_id", nullable = false)
    private Long filaId;

    @Id
    @Column(name = "dia_semana", nullable = false, length = 20)
    private String diaSemana;

    @Id
    @Column(name = "franja_horaria", nullable = false)
    private Integer franjaHoraria;

    @Id
    @Column(nullable = false)
    private LocalDate fecha;

    @Column(nullable = false)
    private Long cantidad;
}
