package com.digitalqueue.dto.login;

import com.digitalqueue.model.enums.RolAdmin;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoginResponse {

    private String token;
    private Long usuarioId;
    private Long localId;
    private String nombre;
    private String email;
    private RolAdmin rol;
}