package com.digitalqueue.service;

import com.digitalqueue.dto.EstimacionEspera;
import com.digitalqueue.model.Fila;
import com.digitalqueue.model.Local;
import com.digitalqueue.model.enums.QueueStatus;
import com.digitalqueue.model.enums.TipoDia;
import com.digitalqueue.model.enums.TipoOperacionLocal;
import com.digitalqueue.repository.FilaRepository;
import com.digitalqueue.repository.LocalRepository;
import com.digitalqueue.service.metrics.MetricasFilaService;
import com.digitalqueue.util.BusinessTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
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
    private static final int MINIMO_ESPERA_SIN_CUPO_MINUTOS = 1;
    private static final double TIEMPO_BASE_DEFAULT_POR_PERSONA = 3.0;
    private static final double TIEMPO_BASE_MINIMO_POR_PERSONA = 3.0;
    private static final double TIEMPO_BASE_MAXIMO_POR_PERSONA = 4.0;
    private static final double TIEMPO_RECIENTE_MAXIMO_POR_PERSONA = 6.0;
    private static final double TIEMPO_ALERTA_MAXIMO_POR_PERSONA = 5.0;
    private static final double TIEMPO_PICO_MAXIMO_POR_PERSONA = 6.0;

    private final FilaRepository filaRepository;
    private final LocalRepository localRepository;
    private final MetricasFilaService metricasFilaService;

    @Transactional
    public EstimacionEspera calcularEstimacion(Fila fila, Long personasAdelante, Integer cantidadIntegrantes) {
        LocalDateTime ahora = BusinessTime.nowStorage();
        if (esConsumoEnLocal(fila.getLocal())) {
            actualizarMomentoLleno(fila.getLocal(), ahora);
        }

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
        long personasEnEspera = personasAdelante == null ? 0L : personasAdelante;
        double esperaPorCapacidad = esConsumoEnLocal(fila.getLocal())
                ? calcularEsperaPorCapacidad(
                        fila.getLocal(),
                        cantidadIntegrantes,
                        ahora,
                        obtenerTiempoCapacidadMinutos(fila)
                )
                : 0.0;
        double estimado = (esperaPorCapacidad + personasEnEspera * tiempoPorPersona) * tipoDia.getMultiplicador();

        int promedio = redondearHaciaArriba(estimado);
        int minimo = estimado <= 0 ? 0 : Math.max(1, redondearHaciaArriba(estimado * 0.85));
        int maximo = estimado <= 0 ? 0 : Math.max(minimo, redondearHaciaArriba(estimado * 1.20));

        return new EstimacionEspera(queueStatus, promedio, minimo, maximo, tiempoPorPersona);
    }

    private QueueStatus determinarEstado(Fila fila, Long personasAdelante, Integer cantidadIntegrantes, LocalDateTime ahora) {
        QueueStatus estadoActual = fila.getQueueStatus() == null ? QueueStatus.NORMAL : fila.getQueueStatus();
        long personasEnEspera = personasAdelante == null ? 0L : personasAdelante;

        if (personasEnEspera == 0 && esAtencionRapida(fila.getLocal())) {
            return QueueStatus.SIN_ESPERA;
        }

        if (personasEnEspera == 0 && hayCapacidadDisponible(fila.getLocal(), cantidadIntegrantes)) {
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
        if (esAtencionRapida(local)) {
            return true;
        }

        int capacidad = obtenerCapacidadEfectiva(local);
        int actuales = obtenerPersonasActuales(local);
        int integrantes = cantidadIntegrantes == null ? 1 : cantidadIntegrantes;
        return capacidad > 0 && actuales + integrantes <= capacidad;
    }

    private boolean esAtencionRapida(Local local) {
        return obtenerTipoOperacion(local) == TipoOperacionLocal.ATENCION_RAPIDA;
    }

    private boolean esConsumoEnLocal(Local local) {
        return obtenerTipoOperacion(local) == TipoOperacionLocal.CONSUMO_EN_LOCAL;
    }

    private TipoOperacionLocal obtenerTipoOperacion(Local local) {
        return local.getTipoOperacion() == null ? TipoOperacionLocal.ATENCION_RAPIDA : local.getTipoOperacion();
    }

    private void actualizarMomentoLleno(Local local, LocalDateTime ahora) {
        int capacidad = obtenerCapacidadEfectiva(local);
        if (capacidad <= 0) {
            return;
        }

        int actuales = obtenerPersonasActuales(local);
        boolean estaLleno = actuales >= capacidad;

        if (estaLleno && local.getLlenoDesde() == null) {
            local.setLlenoDesde(ahora);
            localRepository.save(local);
        } else if (!estaLleno && local.getLlenoDesde() != null) {
            local.setLlenoDesde(null);
            localRepository.save(local);
        }
    }

    private double calcularEsperaPorCapacidad(
            Local local,
            Integer cantidadIntegrantes,
            LocalDateTime ahora,
            double tiempoPorPersona
    ) {
        if (obtenerCapacidadEfectiva(local) <= 0 || hayCapacidadDisponible(local, cantidadIntegrantes)) {
            return 0.0;
        }

        LocalDateTime llenoDesde = obtenerPersonasActuales(local) >= obtenerCapacidadEfectiva(local)
                ? local.getLlenoDesde()
                : null;
        long minutosDesdeLleno = llenoDesde == null
                ? 0L
                : Math.max(0L, Duration.between(llenoDesde, ahora).toMinutes());
        double esperaRestante = tiempoPorPersona - minutosDesdeLleno;

        return Math.max(MINIMO_ESPERA_SIN_CUPO_MINUTOS, esperaRestante);
    }

    private int obtenerCapacidadEfectiva(Local local) {
        int capacidadOperativa = local.getCapacidadOperativaActual() == null ? 0 : local.getCapacidadOperativaActual();
        if (capacidadOperativa > 0) {
            return capacidadOperativa;
        }

        return local.getCapacidadMaxima() == null ? 0 : local.getCapacidadMaxima();
    }

    private int obtenerPersonasActuales(Local local) {
        return local.getPersonasActuales() == null ? 0 : local.getPersonasActuales();
    }

    private double obtenerTasaLlegadaHistorica(Fila fila, LocalDateTime ahora) {
        LocalDateTime desde = ahora.minusDays(DIAS_HISTORICOS);
        LocalDateTime ahoraLocal = BusinessTime.storageToBusiness(ahora);
        long cantidadHistorica = metricasFilaService.contarHistoricoPorDiaYFranja(
                fila.getId(),
                desde,
                ahora.minusMinutes(VENTANA_MINUTOS),
                ahoraLocal.getDayOfWeek(),
                ahoraLocal.getHour()
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
            return TIEMPO_BASE_DEFAULT_POR_PERSONA;
        }
        return limitar(
                fila.getTiempoPromedioAtencionMinutos(),
                TIEMPO_BASE_MINIMO_POR_PERSONA,
                TIEMPO_BASE_MAXIMO_POR_PERSONA
        );
    }

    private double obtenerTiempoCapacidadMinutos(Fila fila) {
        if (fila.getTiempoPromedioAtencionMinutos() == null || fila.getTiempoPromedioAtencionMinutos() <= 0) {
            return TIEMPO_BASE_DEFAULT_POR_PERSONA;
        }
        return fila.getTiempoPromedioAtencionMinutos();
    }

    private double obtenerTiempoRecientePorPersona(Fila fila, LocalDateTime ahora, double historico) {
        LocalDateTime inicioVentana = ahora.minusMinutes(VENTANA_MINUTOS);
        long llamadosRecientes = metricasFilaService.contarLlamadosEntre(fila.getId(), inicioVentana, ahora);

        if (llamadosRecientes == 0) {
            return historico;
        }

        return limitar(
                VENTANA_MINUTOS / (double) llamadosRecientes,
                TIEMPO_BASE_MINIMO_POR_PERSONA,
                TIEMPO_RECIENTE_MAXIMO_POR_PERSONA
        );
    }

    private double obtenerErrorPromedioReciente(Fila fila, LocalDateTime ahora) {
        return metricasFilaService.promedioErrorPrediccionDesde(
                fila.getId(),
                ahora.minusMinutes(60),
                ahora
        );
    }

    private double ponderarTiempoPorEstado(QueueStatus queueStatus, double historico, double reciente) {
        double tiempoPorPersona = switch (queueStatus) {
            case NORMAL -> historico;
            case ALERTA -> historico * 0.5 + reciente * 0.5;
            case PICO -> historico * 0.2 + reciente * 0.8;
            case RECUPERACION -> historico * 0.4 + reciente * 0.6;
            case SIN_ESPERA -> 0.0;
        };

        return switch (queueStatus) {
            case NORMAL -> limitar(tiempoPorPersona, TIEMPO_BASE_MINIMO_POR_PERSONA, TIEMPO_BASE_MAXIMO_POR_PERSONA);
            case ALERTA, RECUPERACION -> limitar(tiempoPorPersona, TIEMPO_BASE_MINIMO_POR_PERSONA, TIEMPO_ALERTA_MAXIMO_POR_PERSONA);
            case PICO -> limitar(tiempoPorPersona, TIEMPO_BASE_MINIMO_POR_PERSONA, TIEMPO_PICO_MAXIMO_POR_PERSONA);
            case SIN_ESPERA -> 0.0;
        };
    }

    private int redondearHaciaArriba(double valor) {
        return (int) Math.ceil(valor);
    }

    private double limitar(double valor, double minimo, double maximo) {
        return Math.max(minimo, Math.min(maximo, valor));
    }
}
