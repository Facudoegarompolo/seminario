package com.digitalqueue.model;

import com.digitalqueue.model.enums.RolAdmin;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "usuarios_admin")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UsuarioAdmin {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "local_id", nullable = false)
    private Local local;

    @Column(nullable = false)
    private String nombre;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RolAdmin rol;

    @Column(nullable = false)
    private Boolean activo;
}