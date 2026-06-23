package com.digitalqueue.repository;

import com.digitalqueue.model.Fila;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FilaRepository extends JpaRepository<Fila, Long> {

    List<Fila> findByLocalId(Long localId);

    Optional<Fila> findFirstByLocalIdOrderByIdAsc(Long localId);

    boolean existsByIdAndLocalId(Long id, Long localId);
}
