package com.digitalqueue.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ActualizarLogoLocalRequest {

    @NotNull
    @Size(max = 2048)
    private String linkImagenLogo;
}
