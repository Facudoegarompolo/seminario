package com.digitalqueue.repository;

import com.digitalqueue.model.Fila;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FilaRepository extends JpaRepository<Fila, Long> {

    List<Fila> findByLocalId(Long localId);
}