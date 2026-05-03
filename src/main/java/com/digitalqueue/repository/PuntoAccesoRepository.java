package com.digitalqueue.repository;

import com.digitalqueue.model.PuntoAcceso;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PuntoAccesoRepository extends JpaRepository<PuntoAcceso, Long> {

    Optional<PuntoAcceso> findByCodigoPublico(String codigoPublico);

    Optional<PuntoAcceso> findByCodigoPublicoAndActivoTrue(String codigoPublico);

    List<PuntoAcceso> findByFilaId(Long filaId);
}