package com.digitalqueue.model;

import com.digitalqueue.model.enums.EstadoFila;
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

    @Column(name = "tiempo_promedio_atencion_minutos", nullable = false)
    private Integer tiempoPromedioAtencionMinutos;
}