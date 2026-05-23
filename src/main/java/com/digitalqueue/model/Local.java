package com.digitalqueue.model;

import com.digitalqueue.model.enums.TipoOperacionLocal;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

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

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_operacion", nullable = false)
    private TipoOperacionLocal tipoOperacion = TipoOperacionLocal.ATENCION_RAPIDA;

    @Column(name = "capacidad_maxima")
    private Integer capacidadMaxima;

    @Column(name = "capacidad_operativa_actual")
    private Integer capacidadOperativaActual;

    @Column(name = "personas_actuales")
    private Integer personasActuales;

    @Column(name = "lleno_desde")
    private LocalDateTime llenoDesde;

    @PrePersist
    public void prePersist() {
        if (tipoOperacion == null) {
            tipoOperacion = TipoOperacionLocal.ATENCION_RAPIDA;
        }
    }
}
