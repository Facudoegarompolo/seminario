package com.digitalqueue.model;

import com.digitalqueue.model.enums.TipoAcceso;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "puntos_acceso")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PuntoAcceso {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "fila_id", nullable = false)
    private Fila fila;

    @Column(name = "codigo_publico", nullable = false, unique = true)
    private String codigoPublico;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_acceso", nullable = false)
    private TipoAcceso tipoAcceso;

    @Column(nullable = false)
    private Boolean activo;
}