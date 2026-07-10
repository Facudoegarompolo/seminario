package com.digitalqueue.repository;

import com.digitalqueue.model.Turno;
import com.digitalqueue.model.enums.EstadoTurno;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface TurnoRepository extends JpaRepository<Turno, Long> {

    Optional<Turno> findByTokenPublico(String tokenPublico);

    boolean existsByIdAndFilaLocalId(Long id, Long localId);

    List<Turno> findByFilaIdOrderByCreatedAtAsc(Long filaId);

    @Query("""
            SELECT t FROM Turno t
            WHERE t.fila.id = :filaId
              AND (t.ocultoEnFila = false OR t.ocultoEnFila IS NULL)
            ORDER BY t.createdAt ASC
            """)
    List<Turno> findVisiblesByFilaIdOrderByCreatedAtAsc(@Param("filaId") Long filaId);

    @Query("""
            SELECT t FROM Turno t
            WHERE t.fila.id = :filaId
              AND t.estado IN :estados
              AND (t.ocultoEnFila = false OR t.ocultoEnFila IS NULL)
            ORDER BY t.createdAt ASC
            """)
    List<Turno> findVisiblesByFilaIdAndEstadoInOrderByCreatedAtAsc(
            @Param("filaId") Long filaId,
            @Param("estados") Collection<EstadoTurno> estados
    );

    Optional<Turno> findTopByFilaIdOrderByNumeroTurnoDesc(Long filaId);

    Optional<Turno> findFirstByFilaIdAndEstadoInOrderByCreatedAtAsc(
            Long filaId,
            Collection<EstadoTurno> estados
    );

    long countByFilaIdAndEstadoInAndCreatedAtBefore(
            Long filaId,
            Collection<EstadoTurno> estados,
            LocalDateTime createdAt
    );

    long countByFilaIdAndEstadoIn(
            Long filaId,
            Collection<EstadoTurno> estados
    );

    boolean existsByFilaIdAndEstadoIn(Long filaId, Collection<EstadoTurno> estados);

    long countByFilaIdAndEstadoAndCompletedAtGreaterThanEqualAndCompletedAtLessThan(
            Long filaId,
            EstadoTurno estado,
            LocalDateTime desde,
            LocalDateTime hasta
    );

    @Query("""
            SELECT t FROM Turno t
            WHERE t.fila.id = :filaId
              AND t.estado IN :estados
            ORDER BY COALESCE(t.completedAt, t.calledAt) DESC
            """)
    List<Turno> findUltimosEventos(
            @Param("filaId") Long filaId,
            @Param("estados") Collection<EstadoTurno> estados,
            Pageable pageable
    );

    long countByFilaIdAndCreatedAtGreaterThanEqualAndCreatedAtLessThan(
            Long filaId,
            LocalDateTime desde,
            LocalDateTime hasta
    );

    List<Turno> findByFilaIdAndCalledAtGreaterThanEqualAndCalledAtLessThan(
            Long filaId,
            LocalDateTime desde,
            LocalDateTime hasta
    );

}
