package com.digitalqueue.dto;

import com.digitalqueue.model.enums.RolAdmin;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CrearAdminResponse {

    private Boolean creado;
    private Boolean emailExistente;
    private String mensaje;
    private Long usuarioId;
    private Long localId;
    private String nombre;
    private String email;
    private RolAdmin rol;
}
