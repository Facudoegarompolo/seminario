package com.digitalqueue.dto;

import com.digitalqueue.model.enums.EstadoTurno;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TurnoAdminResponse {

    private Long turnoId;
    private Integer numeroTurno;
    private EstadoTurno estado;
    private LocalDateTime createdAt;
    private LocalDateTime calledAt;
    private LocalDateTime completedAt;
}