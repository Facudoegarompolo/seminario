package com.digitalqueue.model;

import com.digitalqueue.model.enums.EstadoFila;
import com.digitalqueue.model.enums.QueueStatus;
import com.digitalqueue.model.enums.TipoDia;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "filas")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Fila {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "local_id", nullable = false)
    private Local local;

    @Column(nullable = false)
    private String nombre;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoFila estado;

    @Enumerated(EnumType.STRING)
    @Column(name = "queue_status")
    private QueueStatus queueStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_dia")
    private TipoDia tipoDia;

    @Column(name = "tiempo_promedio_atencion_minutos", nullable = false)
    private Integer tiempoPromedioAtencionMinutos;
}
