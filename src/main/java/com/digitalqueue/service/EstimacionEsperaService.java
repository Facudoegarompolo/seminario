package com.digitalqueue.service;

import com.digitalqueue.dto.EstimacionEspera;
import com.digitalqueue.model.Fila;
import com.digitalqueue.model.Local;
import com.digitalqueue.model.enums.QueueStatus;
import com.digitalqueue.model.enums.TipoDia;
import com.digitalqueue.repository.FilaRepository;
import com.digitalqueue.service.metrics.MetricasFilaService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class EstimacionEsperaService {

    private static final int VENTANA_MINUTOS = 15;
    private static final int DIAS_HISTORICOS = 28;
    private static final double UMBRAL_ALERTA = 1.5;
    private static final double UMBRAL_PICO = 2.0;
    private static final int ERROR_ALERTA_MINUTOS = 5;
    private static final int ERROR_PICO_MINUTOS = 10;

    private final FilaRepository filaRepository;
    private final MetricasFilaService metricasFilaService;

    @Transactional
    public EstimacionEspera calcularEstimacion(Fila fila, Long personasAdelante, Integer cantidadIntegrantes) {
        LocalDateTime ahora = LocalDateTime.now();
        QueueStatus queueStatus = determinarEstado(fila, personasAdelante, cantidadIntegrantes, ahora);

        if (queueStatus != fila.getQueueStatus()) {
            fila.setQueueStatus(queueStatus);
            filaRepository.save(fila);
        }

        if (queueStatus == QueueStatus.SIN_ESPERA) {
            return new EstimacionEspera(queueStatus, 0, 0, 0, 0.0);
        }

        double historico = obtenerTiempoHistoricoPorPersona(fila);
        double reciente = obtenerTiempoRecientePorPersona(fila, ahora, historico);
        double tiempoPorPersona = ponderarTiempoPorEstado(queueStatus, historico, reciente);

        TipoDia tipoDia = fila.getTipoDia() == null ? TipoDia.NORMAL : fila.getTipoDia();
        double estimado = personasAdelante * tiempoPorPersona * tipoDia.getMultiplicador();

        int promedio = redondearHaciaArriba(estimado);
        int minimo = estimado <= 0 ? 0 : Math.max(1, redondearHaciaArriba(estimado * 0.85));
        int maximo = estimado <= 0 ? 0 : Math.max(minimo, redondearHaciaArriba(estimado * 1.20));

        return new EstimacionEspera(queueStatus, promedio, minimo, maximo, tiempoPorPersona);
    }

    private QueueStatus determinarEstado(Fila fila, Long personasAdelante, Integer cantidadIntegrantes, LocalDateTime ahora) {
        QueueStatus estadoActual = fila.getQueueStatus() == null ? QueueStatus.NORMAL : fila.getQueueStatus();

        if (hayCapacidadDisponible(fila.getLocal(), cantidadIntegrantes) && personasAdelante == 0) {
            return QueueStatus.SIN_ESPERA;
        }

        LocalDateTime inicioVentana = ahora.minusMinutes(VENTANA_MINUTOS);

        long llegadasRecientes = metricasFilaService.contarInscripcionesEntre(fila.getId(), inicioVentana, ahora);
        long llamadosRecientes = metricasFilaService.contarLlamadosEntre(fila.getId(), inicioVentana, ahora);

        double tasaActual = llegadasRecientes / (double) VENTANA_MINUTOS;
        double tasaHistorica = obtenerTasaLlegadaHistorica(fila, ahora);
        double velocidadAtencionReciente = llamadosRecientes / (double) VENTANA_MINUTOS;
        double crecimientoFila = tasaActual - velocidadAtencionReciente;
        double errorPromedio = obtenerErrorPromedioReciente(fila, ahora);

        boolean alerta = tasaActual > tasaHistorica * UMBRAL_ALERTA
                || crecimientoFila > 0.15
                || errorPromedio >= ERROR_ALERTA_MINUTOS;

        boolean pico = tasaActual > tasaHistorica * UMBRAL_PICO
                && (crecimientoFila > 0.25 || errorPromedio >= ERROR_PICO_MINUTOS);

        return switch (estadoActual) {
            case SIN_ESPERA, NORMAL -> pico ? QueueStatus.ALERTA : (alerta ? QueueStatus.ALERTA : QueueStatus.NORMAL);
            case ALERTA -> pico ? QueueStatus.PICO : (alerta ? QueueStatus.ALERTA : QueueStatus.NORMAL);
            case PICO -> pico ? QueueStatus.PICO : QueueStatus.RECUPERACION;
            case RECUPERACION -> alerta ? QueueStatus.RECUPERACION : QueueStatus.NORMAL;
        };
    }

    private boolean hayCapacidadDisponible(Local local, Integer cantidadIntegrantes) {
        int capacidad = local.getCapacidadMaxima() == null ? 0 : local.getCapacidadMaxima();
        int actuales = local.getPersonasActuales() == null ? 0 : local.getPersonasActuales();
        int integrantes = cantidadIntegrantes == null ? 1 : cantidadIntegrantes;
        return capacidad > 0 && actuales + integrantes <= capacidad;
    }

    private double obtenerTasaLlegadaHistorica(Fila fila, LocalDateTime ahora) {
        LocalDateTime desde = ahora.minusDays(DIAS_HISTORICOS);
        long cantidadHistorica = metricasFilaService.contarHistoricoPorDiaYFranja(
                fila.getId(),
                desde,
                ahora.minusMinutes(VENTANA_MINUTOS),
                ahora.getDayOfWeek(),
                ahora.getHour()
        );

        if (cantidadHistorica == 0) {
            return 1.0 / Math.max(obtenerTiempoHistoricoPorPersona(fila), 1.0);
        }

        double repeticionesEstimadas = DIAS_HISTORICOS / 7.0;
        double minutosComparables = repeticionesEstimadas * 60.0;
        return cantidadHistorica / minutosComparables;
    }

    private double obtenerTiempoHistoricoPorPersona(Fila fila) {
        if (fila.getTiempoPromedioAtencionMinutos() == null || fila.getTiempoPromedioAtencionMinutos() <= 0) {
            return 3.0;
        }
        return fila.getTiempoPromedioAtencionMinutos();
    }

    private double obtenerTiempoRecientePorPersona(Fila fila, LocalDateTime ahora, double historico) {
        LocalDateTime inicioVentana = ahora.minusMinutes(VENTANA_MINUTOS);
        long llamadosRecientes = metricasFilaService.contarLlamadosEntre(fila.getId(), inicioVentana, ahora);

        if (llamadosRecientes == 0) {
            return historico;
        }

        return VENTANA_MINUTOS / (double) llamadosRecientes;
    }

    private double obtenerErrorPromedioReciente(Fila fila, LocalDateTime ahora) {
        return metricasFilaService.promedioErrorPrediccionDesde(
                fila.getId(),
                ahora.minusMinutes(60),
                ahora
        );
    }

    private double ponderarTiempoPorEstado(QueueStatus queueStatus, double historico, double reciente) {
        return switch (queueStatus) {
            case NORMAL -> historico;
            case ALERTA -> historico * 0.5 + reciente * 0.5;
            case PICO -> historico * 0.2 + reciente * 0.8;
            case RECUPERACION -> historico * 0.4 + reciente * 0.6;
            case SIN_ESPERA -> 0.0;
        };
    }

    private int redondearHaciaArriba(double valor) {
        return (int) Math.ceil(valor);
    }
}
