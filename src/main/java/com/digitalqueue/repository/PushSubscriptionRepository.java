package com.digitalqueue.repository;

import com.digitalqueue.model.PushSubscription;
import com.digitalqueue.model.enums.EstadoTurno;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PushSubscriptionRepository extends JpaRepository<PushSubscription, Long> {

    List<PushSubscription> findByTurnoIdAndActivoTrue(Long turnoId);

    Optional<PushSubscription> findByTurnoIdAndEndpoint(Long turnoId, String endpoint);

    Optional<PushSubscription> findFirstByEndpointAndActivoTrueAndTurnoEstadoInOrderByUpdatedAtDesc(
            String endpoint,
            List<EstadoTurno> estados
    );

    boolean existsByTurnoIdAndActivoTrue(Long turnoId);
}
