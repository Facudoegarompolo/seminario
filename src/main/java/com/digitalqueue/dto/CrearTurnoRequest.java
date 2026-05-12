package com.digitalqueue.dto;

import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
public class CrearTurnoRequest {

    @Min(value = 1, message = "La cantidad de integrantes debe ser mayor a 0")
    private Integer cantidadIntegrantes = 1;
}
