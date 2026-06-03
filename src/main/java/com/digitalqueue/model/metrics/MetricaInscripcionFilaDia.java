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
        name = "metricas_inscripcion_por_fila_dia",
        indexes = {
                @Index(name = "idx_metricas_inscripcion_fila_created", columnList = "fila_id, created_at"),
                @Index(
                        name = "idx_metricas_inscripcion_fila_dia_franja_created",
                        columnList = "fila_id, dia_semana, franja_horaria, created_at"
                )
        }
)
@IdClass(MetricaInscripcionFilaDiaId.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MetricaInscripcionFilaDia {

    @Id
    @Column(name = "fila_id", nullable = false)
    private Long filaId;

    @Id
    @Column(nullable = false)
    private LocalDate fecha;

    @Id
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Id
    @Column(name = "turno_id", nullable = false)
    private Long turnoId;

    @Column(name = "cantidad_integrantes")
    private Integer cantidadIntegrantes;

    @Column(name = "nombre_cliente", length = 50)
    private String nombreCliente;

    @Column(name = "tipo_cliente", length = 20)
    private String tipoCliente;

    @Column(name = "personas_adelante")
    private Long personasAdelante;

    @Column(name = "queue_status", length = 20)
    private String queueStatus;

    @Column(name = "tiempo_estimado_informado")
    private Integer tiempoEstimadoInformado;

    @Column(name = "tiempo_estimado_minimo")
    private Integer tiempoEstimadoMinimo;

    @Column(name = "tiempo_estimado_maximo")
    private Integer tiempoEstimadoMaximo;

    @Column(name = "dia_semana", length = 20)
    private String diaSemana;

    @Column(name = "franja_horaria")
    private Integer franjaHoraria;
}
