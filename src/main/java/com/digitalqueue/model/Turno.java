package com.digitalqueue.model;

import com.digitalqueue.model.enums.EstadoTurno;
import com.digitalqueue.model.enums.QueueStatus;
import com.digitalqueue.model.enums.TipoCliente;
import jakarta.persistence.*;
import lombok.*;

import java.time.DayOfWeek;
import java.time.LocalDateTime;

@Entity
@Table(name = "turnos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Turno {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "fila_id", nullable = false)
    private Fila fila;

    @Column(name = "numero_turno", nullable = false)
    private Integer numeroTurno;

    @Column(name = "token_publico", nullable = false, unique = true)
    private String tokenPublico;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoTurno estado;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "called_at")
    private LocalDateTime calledAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @ManyToOne
    @JoinColumn(name = "cliente_id")
    private Cliente cliente;

    @Column(name = "nombre_cliente", length = 50)
    private String nombreCliente;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_cliente", length = 20)
    private TipoCliente tipoCliente;

    @Column(name = "cantidad_integrantes")
    private Integer cantidadIntegrantes;

    @Column(name = "posicion_inicial")
    private Integer posicionInicial;

    @Column(name = "personas_adelante_al_anotarse")
    private Long personasAdelanteAlAnotarse;

    @Enumerated(EnumType.STRING)
    @Column(name = "queue_status_al_anotarse")
    private QueueStatus queueStatusAlAnotarse;

    @Column(name = "tiempo_estimado_informado_minutos")
    private Integer tiempoEstimadoInformadoMinutos;

    @Column(name = "tiempo_estimado_minimo_minutos")
    private Integer tiempoEstimadoMinimoMinutos;

    @Column(name = "tiempo_estimado_maximo_minutos")
    private Integer tiempoEstimadoMaximoMinutos;

    @Column(name = "tiempo_real_espera_minutos")
    private Integer tiempoRealEsperaMinutos;

    @Column(name = "error_prediccion_minutos")
    private Integer errorPrediccionMinutos;

    @Enumerated(EnumType.STRING)
    @Column(name = "dia_semana")
    private DayOfWeek diaSemana;

    @Column(name = "franja_horaria")
    private Integer franjaHoraria;

    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (cantidadIntegrantes == null) {
            cantidadIntegrantes = 1;
        }
        if (tipoCliente == null) {
            tipoCliente = TipoCliente.ANONIMO;
        }
        if (nombreCliente == null || nombreCliente.isBlank()) {
            nombreCliente = "Cliente anónimo";
        }
        if (diaSemana == null) {
            diaSemana = createdAt.getDayOfWeek();
        }
        if (franjaHoraria == null) {
            franjaHoraria = createdAt.getHour();
        }
    }
}
