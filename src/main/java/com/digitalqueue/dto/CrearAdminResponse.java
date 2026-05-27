package com.digitalqueue.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CrearAdminResponse {

    private Boolean creado;
    private String mensaje;
}
