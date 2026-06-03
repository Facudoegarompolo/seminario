package com.digitalqueue.model.metrics;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "metricas_llamado_por_fila_dia",
        indexes = {
                @Index(name = "idx_metricas_llamado_fila_called", columnList = "fila_id, called_at")
        }
)
@IdClass(MetricaLlamadoFilaDiaId.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MetricaLlamadoFilaDia {

    @Id
    @Column(name = "fila_id", nullable = false)
    private Long filaId;

    @Id
    @Column(nullable = false)
    private LocalDate fecha;

    @Id
    @Column(name = "called_at", nullable = false)
    private LocalDateTime calledAt;

    @Id
    @Column(name = "turno_id", nullable = false)
    private Long turnoId;

    @Column(name = "tiempo_real_espera")
    private Integer tiempoRealEspera;

    @Column(name = "error_prediccion")
    private Integer errorPrediccion;
}
