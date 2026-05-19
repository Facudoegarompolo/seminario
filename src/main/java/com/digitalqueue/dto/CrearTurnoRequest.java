package com.digitalqueue.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CrearTurnoRequest {

    @Min(value = 1, message = "La cantidad de integrantes debe ser mayor a 0")
    private Integer cantidadIntegrantes = 1;

    @Size(max = 50, message = "El nombre del cliente no puede superar los 50 caracteres")
    private String nombreCliente;
}
