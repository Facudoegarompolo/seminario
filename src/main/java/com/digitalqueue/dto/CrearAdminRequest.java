package com.digitalqueue.dto;

import com.digitalqueue.model.enums.TipoOperacionLocal;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CrearAdminRequest {

    @NotBlank
    private String nombre;

    @Email
    @NotBlank
    private String email;

    @NotBlank
    @Size(min = 6)
    private String password;

    @NotBlank
    private String nombreLocal;

    @NotBlank
    private String direccionLocal;

    @Size(max = 2048)
    private String linkImagenLogoLocal;

    @Positive
    private Integer capacidadMaxima;

    private TipoOperacionLocal tipoOperacionLocal;
}
