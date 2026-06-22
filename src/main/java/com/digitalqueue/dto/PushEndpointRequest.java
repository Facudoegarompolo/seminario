package com.digitalqueue.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PushEndpointRequest {

    @NotBlank
    private String endpoint;
}
