package com.digitalqueue.repository.metrics;

import com.digitalqueue.model.metrics.MetricaInscripcionFilaDia;
import com.digitalqueue.model.metrics.MetricaInscripcionFilaDiaId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;

public interface MetricaInscripcionFilaDiaRepository
        extends JpaRepository<MetricaInscripcionFilaDia, MetricaInscripcionFilaDiaId> {

    long countByFilaIdAndCreatedAtGreaterThanEqualAndCreatedAtBefore(
            Long filaId,
            LocalDateTime desde,
            LocalDateTime hasta
    );

    long countByFilaIdAndDiaSemanaAndFranjaHorariaAndCreatedAtGreaterThanEqualAndCreatedAtBefore(
            Long filaId,
            String diaSemana,
            Integer franjaHoraria,
            LocalDateTime desde,
            LocalDateTime hasta
    );
}
