package com.digitalqueue.repository.metrics;

import com.digitalqueue.model.metrics.MetricaLlegadasFranja;
import com.digitalqueue.model.metrics.MetricaLlegadasFranjaId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MetricaLlegadasFranjaRepository
        extends JpaRepository<MetricaLlegadasFranja, MetricaLlegadasFranjaId> {
}
