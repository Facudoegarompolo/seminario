package com.digitalqueue.repository;

import com.digitalqueue.model.Turno;
import com.digitalqueue.model.enums.EstadoTurno;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface TurnoRepository extends JpaRepository<Turno, Long> {

    Optional<Turno> findByTokenPublico(String tokenPublico);

    List<Turno> findByFilaIdOrderByCreatedAtAsc(Long filaId);

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

}
