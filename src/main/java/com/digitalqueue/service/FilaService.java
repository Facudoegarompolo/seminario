package com.digitalqueue.service;

import com.digitalqueue.dto.EstimacionEspera;
import com.digitalqueue.dto.FilaEstadoResponse;
import com.digitalqueue.exception.OperacionInvalidaException;
import com.digitalqueue.exception.RecursoNoEncontradoException;
import com.digitalqueue.model.Fila;
import com.digitalqueue.model.PuntoAcceso;
import com.digitalqueue.model.enums.EstadoFila;
import com.digitalqueue.model.enums.EstadoTurno;
import com.digitalqueue.repository.PuntoAccesoRepository;
import com.digitalqueue.repository.TurnoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FilaService {

    private final PuntoAccesoRepository puntoAccesoRepository;
    private final TurnoRepository turnoRepository;
    private final EstimacionEsperaService estimacionEsperaService;

    private static final List<EstadoTurno> ESTADOS_EN_ESPERA = List.of(
            EstadoTurno.ESPERANDO,
            EstadoTurno.PROXIMO
    );

    public FilaEstadoResponse obtenerEstadoFila(String codigoPublico) {
        PuntoAcceso puntoAcceso = puntoAccesoRepository
                .findByCodigoPublicoAndActivoTrue(codigoPublico)
                .orElseThrow(() -> new RecursoNoEncontradoException("Punto de acceso no encontrado o inactivo"));

        Fila fila = puntoAcceso.getFila();

        Long personasEsperando = turnoRepository.countByFilaIdAndEstadoIn(
                fila.getId(),
                ESTADOS_EN_ESPERA
        );

        EstimacionEspera estimacion = estimacionEsperaService.calcularEstimacion(fila, personasEsperando, 1);

        return new FilaEstadoResponse(
                fila.getId(),
                fila.getLocal().getNombre(),
                fila.getNombre(),
                fila.getEstado(),
                estimacion.getQueueStatus(),
                personasEsperando,
                estimacion.getTiempoEstimadoMinutos(),
                estimacion.getTiempoEstimadoMinimoMinutos(),
                estimacion.getTiempoEstimadoMaximoMinutos()
        );
    }

    public void validarFilaAbierta(Fila fila) {
        if (fila.getEstado() != EstadoFila.ABIERTA) {
            throw new OperacionInvalidaException("La fila no está abierta");
        }
    }
}
