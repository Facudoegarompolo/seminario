package com.digitalqueue.config;

import com.digitalqueue.model.Fila;
import com.digitalqueue.model.Turno;
import com.digitalqueue.model.enums.QueueStatus;
import com.digitalqueue.model.enums.TipoCliente;
import com.digitalqueue.repository.FilaRepository;
import com.digitalqueue.service.metrics.MetricasFilaService;
import com.digitalqueue.util.BusinessTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
@Order(2)
public class MetricasDemoDataLoader implements CommandLineRunner {

    private static final int DIAS_HISTORICOS = 28;
    private static final int PRIMERA_FRANJA_HORARIA = 8;
    private static final int ULTIMA_FRANJA_HORARIA = 23;
    private static final int VENTANA_RECIENTE_MINUTOS = 15;

    private static final long BASE_ID_INSCRIPCION_HISTORICA = -1_000_000_000L;
    private static final long BASE_ID_LLAMADO_HISTORICO = -2_000_000_000L;
    private static final long BASE_ID_INSCRIPCION_RECIENTE = -3_000_000_000L;
    private static final long BASE_ID_LLAMADO_RECIENTE = -4_000_000_000L;

    private final FilaRepository filaRepository;
    private final MetricasFilaService metricasFilaService;

    @Value("${app.metrics.seed.enabled:true}")
    private boolean seedEnabled;

    @Value("${app.metrics.seed.min-historical-inscriptions:600}")
    private long minHistoricalInscriptions;

    @Override
    public void run(String... args) {
        if (!seedEnabled) {
            log.info("Seed de metricas desactivado por configuracion");
            return;
        }

        List<Fila> filas = filaRepository.findAll();
        if (filas.isEmpty()) {
            return;
        }

        LocalDateTime ahora = BusinessTime.nowStorage().withSecond(0).withNano(0);

        for (Fila fila : filas) {
            sembrarMetricasSiHaceFalta(fila, ahora);
        }
    }

    private void sembrarMetricasSiHaceFalta(Fila fila, LocalDateTime ahora) {
        try {
            LocalDateTime desde = ahora.minusDays(DIAS_HISTORICOS);
            long historicoActual = contarInscripciones(fila.getId(), desde, ahora);

            if (historicoActual >= minHistoricalInscriptions) {
                log.info(
                        "Fila {} ya tiene {} metricas historicas; no se agrega seed",
                        fila.getId(),
                        historicoActual
                );
                return;
            }

            SeedResult historico = insertarMetricasHistoricas(fila, ahora);
            SeedResult reciente = insertarMetricasRecientes(fila, ahora);
            SeedResult total = historico.sumar(reciente);

            log.info(
                    "Seed de metricas para fila {}: {} inscripciones y {} llamados",
                    fila.getId(),
                    total.inscripciones(),
                    total.llamados()
            );
        } catch (RuntimeException ex) {
            log.warn("No se pudieron sembrar metricas demo en SQL para fila {}: {}", fila.getId(), ex.getMessage());
        }
    }

    private SeedResult insertarMetricasHistoricas(Fila fila, LocalDateTime ahora) {
        SeedResult total = SeedResult.vacio();
        LocalDate hoyNegocio = BusinessTime.storageToBusiness(ahora).toLocalDate();

        for (int diaOffset = 1; diaOffset <= DIAS_HISTORICOS; diaOffset++) {
            LocalDate fecha = hoyNegocio.minusDays(diaOffset);
            DayOfWeek diaSemana = fecha.getDayOfWeek();

            for (int hora = PRIMERA_FRANJA_HORARIA; hora <= ULTIMA_FRANJA_HORARIA; hora++) {
                int llegadas = calcularLlegadasEsperadas(fila, diaSemana, hora, diaOffset);
                double tiempoPorPersona = calcularTiempoAtencion(fila, diaSemana, hora, diaOffset);

                for (int indice = 0; indice < llegadas; indice++) {
                    LocalDateTime createdAtNegocio = fecha.atTime(hora, minutoDistribuido(indice, llegadas, diaOffset));
                    LocalDateTime createdAt = BusinessTime.businessToStorage(createdAtNegocio);
                    Long personasAdelante = calcularPersonasAdelante(llegadas, hora, diaOffset, indice);
                    int estimado = redondearHaciaArriba(personasAdelante * tiempoPorPersona);
                    int minimo = estimado <= 0 ? 0 : Math.max(1, redondearHaciaArriba(estimado * 0.85));
                    int maximo = estimado <= 0 ? 0 : Math.max(minimo, redondearHaciaArriba(estimado * 1.25));
                    int tiempoReal = Math.max(0, estimado + variacionMinutos(fila, diaOffset, hora, indice));
                    int error = Math.abs(tiempoReal - estimado);
                    Long turnoIdInscripcion = idSemilla(
                            BASE_ID_INSCRIPCION_HISTORICA,
                            fila,
                            diaOffset,
                            hora,
                            indice
                    );
                    Long turnoIdLlamado = idSemilla(
                            BASE_ID_LLAMADO_HISTORICO,
                            fila,
                            diaOffset,
                            hora,
                            indice
                    );

                    insertarInscripcion(
                            fila,
                            createdAt,
                            turnoIdInscripcion,
                            1 + Math.floorMod(indice + diaOffset, 3),
                            personasAdelante,
                            queueStatusPara(llegadas),
                            estimado,
                            minimo,
                            maximo
                    );

                    insertarLlamado(
                            fila,
                            createdAt.plusMinutes(tiempoReal),
                            turnoIdLlamado,
                            tiempoReal,
                            error
                    );

                    total = total.sumar(1, 1);
                }
            }
        }

        return total;
    }

    private SeedResult insertarMetricasRecientes(Fila fila, LocalDateTime ahora) {
        LocalDateTime desde = ahora.minusMinutes(VENTANA_RECIENTE_MINUTOS);
        if (contarInscripciones(fila.getId(), desde, ahora) > 0) {
            return SeedResult.vacio();
        }

        SeedResult total = SeedResult.vacio();
        int[] minutosAtras = {13, 10, 7, 4, 2};

        for (int indice = 0; indice < minutosAtras.length; indice++) {
            LocalDateTime createdAt = ahora.minusMinutes(minutosAtras[indice]);
            Long personasAdelante = (long) Math.max(0, indice - 1);
            int estimado = personasAdelante.intValue() * tiempoPromedio(fila);
            Long turnoId = idSemilla(BASE_ID_INSCRIPCION_RECIENTE, fila, 0, 0, indice);

            insertarInscripcion(
                    fila,
                    createdAt,
                    turnoId,
                    1,
                    personasAdelante,
                    QueueStatus.NORMAL,
                    estimado,
                    Math.max(0, estimado - 1),
                    estimado + 2
            );

            total = total.sumar(1, 0);
        }

        for (int indice = 0; indice < 4; indice++) {
            LocalDateTime calledAt = ahora.minusMinutes(12 - indice * 3L);
            int tiempoReal = Math.max(1, tiempoPromedio(fila) + Math.floorMod(indice, 2));
            Long turnoId = idSemilla(BASE_ID_LLAMADO_RECIENTE, fila, 0, 0, indice);

            insertarLlamado(
                    fila,
                    calledAt,
                    turnoId,
                    tiempoReal,
                    1
            );

            total = total.sumar(0, 1);
        }

        return total;
    }

    private int calcularLlegadasEsperadas(Fila fila, DayOfWeek diaSemana, int hora, int diaOffset) {
        int base;

        if (hora < 10) {
            base = 2;
        } else if (hora < 12) {
            base = 4;
        } else if (hora < 15) {
            base = 8;
        } else if (hora < 18) {
            base = 5;
        } else if (hora < 22) {
            base = 10;
        } else {
            base = 4;
        }

        if (esFinDeSemana(diaSemana)) {
            base += hora >= 19 && hora <= 22 ? 3 : 1;
        }

        int variacion = Math.floorMod(fila.getId().intValue() + diaOffset + hora * 3, 3) - 1;
        return Math.max(1, base + variacion);
    }

    private double calcularTiempoAtencion(Fila fila, DayOfWeek diaSemana, int hora, int diaOffset) {
        double base = tiempoPromedio(fila);
        double multiplicador = 1.0;

        if ((hora >= 12 && hora <= 14) || (hora >= 20 && hora <= 22)) {
            multiplicador += 0.20;
        } else if (hora < 10 || hora >= 23) {
            multiplicador -= 0.15;
        }

        if (esFinDeSemana(diaSemana) && hora >= 19) {
            multiplicador += 0.15;
        }

        double variacion = (Math.floorMod(diaOffset + hora, 3) - 1) * 0.20;
        return Math.max(1.0, base * multiplicador + variacion);
    }

    private Long calcularPersonasAdelante(int llegadas, int hora, int diaOffset, int indice) {
        int ciclo = Math.max(2, Math.min(8, llegadas));
        long personasAdelante = Math.floorMod(indice * 2 + diaOffset + hora, ciclo);

        if (llegadas >= 9) {
            personasAdelante += 2;
        } else if (llegadas >= 6) {
            personasAdelante += 1;
        }

        return personasAdelante;
    }

    private QueueStatus queueStatusPara(int llegadas) {
        if (llegadas >= 11) {
            return QueueStatus.PICO;
        }
        if (llegadas >= 7) {
            return QueueStatus.ALERTA;
        }
        return QueueStatus.NORMAL;
    }

    private int minutoDistribuido(int indice, int llegadas, int diaOffset) {
        int minutoBase = indice * 60 / Math.max(llegadas, 1);
        int variacion = Math.floorMod(diaOffset + indice * 7, 4);
        return Math.min(58, minutoBase + variacion);
    }

    private int variacionMinutos(Fila fila, int diaOffset, int hora, int indice) {
        return Math.floorMod(fila.getId().intValue() + diaOffset + hora + indice, 5) - 2;
    }

    private int tiempoPromedio(Fila fila) {
        Integer tiempoPromedio = fila.getTiempoPromedioAtencionMinutos();
        if (tiempoPromedio == null || tiempoPromedio <= 0) {
            return 3;
        }
        return tiempoPromedio;
    }

    private boolean esFinDeSemana(DayOfWeek diaSemana) {
        return diaSemana == DayOfWeek.SATURDAY || diaSemana == DayOfWeek.SUNDAY;
    }

    private long idSemilla(long base, Fila fila, int diaOffset, int hora, int indice) {
        return base
                - fila.getId() * 10_000_000L
                - diaOffset * 100_000L
                - hora * 1_000L
                - indice;
    }

    private void insertarInscripcion(
            Fila fila,
            LocalDateTime createdAt,
            Long turnoId,
            Integer cantidadIntegrantes,
            Long personasAdelante,
            QueueStatus queueStatus,
            Integer tiempoEstimado,
            Integer tiempoMinimo,
            Integer tiempoMaximo
    ) {
        Turno turno = Turno.builder()
                .id(turnoId)
                .fila(fila)
                .createdAt(createdAt)
                .cantidadIntegrantes(cantidadIntegrantes)
                .nombreCliente("Cliente demo")
                .tipoCliente(TipoCliente.ANONIMO)
                .personasAdelanteAlAnotarse(personasAdelante)
                .queueStatusAlAnotarse(queueStatus)
                .tiempoEstimadoInformadoMinutos(tiempoEstimado)
                .tiempoEstimadoMinimoMinutos(tiempoMinimo)
                .tiempoEstimadoMaximoMinutos(tiempoMaximo)
                .diaSemana(BusinessTime.storageToBusiness(createdAt).getDayOfWeek())
                .franjaHoraria(BusinessTime.storageToBusiness(createdAt).getHour())
                .build();

        metricasFilaService.registrarInscripcion(turno);
    }

    private void insertarLlamado(
            Fila fila,
            LocalDateTime calledAt,
            Long turnoId,
            Integer tiempoReal,
            Integer errorPrediccion
    ) {
        Turno turno = Turno.builder()
                .id(turnoId)
                .fila(fila)
                .createdAt(calledAt.minusMinutes(tiempoReal == null ? 0 : tiempoReal))
                .calledAt(calledAt)
                .tiempoRealEsperaMinutos(tiempoReal)
                .errorPrediccionMinutos(errorPrediccion)
                .build();

        metricasFilaService.registrarLlamado(turno);
    }

    private long contarInscripciones(Long filaId, LocalDateTime desde, LocalDateTime hasta) {
        return metricasFilaService.contarInscripcionesEntre(filaId, desde, hasta);
    }

    private int redondearHaciaArriba(double valor) {
        return (int) Math.ceil(valor);
    }

    private record SeedResult(int inscripciones, int llamados) {

        static SeedResult vacio() {
            return new SeedResult(0, 0);
        }

        SeedResult sumar(int inscripciones, int llamados) {
            return new SeedResult(
                    this.inscripciones + inscripciones,
                    this.llamados + llamados
            );
        }

        SeedResult sumar(SeedResult otro) {
            return sumar(otro.inscripciones, otro.llamados);
        }
    }
}
