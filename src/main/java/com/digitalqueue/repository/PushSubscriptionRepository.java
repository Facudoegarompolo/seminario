package com.digitalqueue.repository;

import com.digitalqueue.model.PushSubscription;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PushSubscriptionRepository extends JpaRepository<PushSubscription, Long> {

    List<PushSubscription> findByTurnoIdAndActivoTrue(Long turnoId);

    Optional<PushSubscription> findByTurnoIdAndEndpoint(Long turnoId, String endpoint);

    boolean existsByTurnoIdAndActivoTrue(Long turnoId);
}
