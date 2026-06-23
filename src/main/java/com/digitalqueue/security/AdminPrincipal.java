package com.digitalqueue.security;

import com.digitalqueue.model.enums.RolAdmin;

public record AdminPrincipal(
        Long usuarioId,
        Long localId,
        String email,
        RolAdmin rol
) {
}
