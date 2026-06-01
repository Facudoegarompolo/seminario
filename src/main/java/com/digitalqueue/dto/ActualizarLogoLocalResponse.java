package com.digitalqueue.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ActualizarLogoLocalResponse {

    private Boolean actualizado;
    private String mensaje;
}
