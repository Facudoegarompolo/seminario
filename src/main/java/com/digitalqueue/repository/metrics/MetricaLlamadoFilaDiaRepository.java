package com.digitalqueue.repository.metrics;

import com.digitalqueue.model.metrics.MetricaLlamadoFilaDia;
import com.digitalqueue.model.metrics.MetricaLlamadoFilaDiaId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

public interface MetricaLlamadoFilaDiaRepository
        extends JpaRepository<MetricaLlamadoFilaDia, MetricaLlamadoFilaDiaId> {

    long countByFilaIdAndCalledAtGreaterThanEqualAndCalledAtBefore(
            Long filaId,
            LocalDateTime desde,
            LocalDateTime hasta
    );

    @Query("""
            SELECT COALESCE(AVG(m.errorPrediccion), 0.0)
            FROM MetricaLlamadoFilaDia m
            WHERE m.filaId = :filaId
              AND m.calledAt >= :desde
              AND m.calledAt < :hasta
            """)
    double promedioErrorPrediccionDesde(
            @Param("filaId") Long filaId,
            @Param("desde") LocalDateTime desde,
            @Param("hasta") LocalDateTime hasta
    );
}
