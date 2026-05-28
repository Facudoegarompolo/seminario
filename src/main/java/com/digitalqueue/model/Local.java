package com.digitalqueue.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "locales")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Local {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombre;

    @Column(nullable = false)
    private String direccion;

    @Column(nullable = false)
    private Boolean activo;

    @Column(name = "capacidad_maxima")
    private Integer capacidadMaxima;

    @Column(name = "personas_actuales")
    private Integer personasActuales;
}
