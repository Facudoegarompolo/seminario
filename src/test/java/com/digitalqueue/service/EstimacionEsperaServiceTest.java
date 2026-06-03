package com.digitalqueue.service;

import com.digitalqueue.dto.EstimacionEspera;
import com.digitalqueue.model.Fila;
import com.digitalqueue.model.Local;
import com.digitalqueue.model.enums.EstadoFila;
import com.digitalqueue.model.enums.QueueStatus;
import com.digitalqueue.model.enums.TipoOperacionLocal;
import com.digitalqueue.repository.FilaRepository;
import com.digitalqueue.repository.LocalRepository;
import com.digitalqueue.service.metrics.MetricasFilaService;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

class EstimacionEsperaServiceTest {

    private final FilaRepository filaRepository = repositoryProxy(FilaRepository.class);
    private final LocalRepository localRepository = repositoryProxy(LocalRepository.class);
    private final MetricasFilaService metricasFilaService = new MetricasSinActividadService();
    private final EstimacionEsperaService service = new EstimacionEsperaService(
            filaRepository,
            localRepository,
            metricasFilaService
    );

    @Test
    void localConCapacidadDisponibleNoTieneEspera() {
        Fila fila = crearFila(20, 18, null, 10);

        EstimacionEspera estimacion = service.calcularEstimacion(fila, 0L, 1);

        assertEquals(QueueStatus.SIN_ESPERA, estimacion.getQueueStatus());
        assertEquals(0, estimacion.getTiempoEstimadoMinutos());
    }

    @Test
    void primerTurnoConLocalLlenoDescuentaElTiempoQueYaPasoDesdeQueSeLleno() {
        Fila fila = crearFila(TipoOperacionLocal.CONSUMO_EN_LOCAL, 20, 20, LocalDateTime.now().minusMinutes(5), 12);

        EstimacionEspera estimacion = service.calcularEstimacion(fila, 0L, 1);

        assertEquals(QueueStatus.NORMAL, estimacion.getQueueStatus());
        assertEquals(7, estimacion.getTiempoEstimadoMinutos());
    }

    @Test
    void atencionRapidaNoUsaCapacidadDelLocalParaDefinirEspera() {
        Fila fila = crearFila(TipoOperacionLocal.ATENCION_RAPIDA, 20, 20, LocalDateTime.now().minusMinutes(5), 12);

        EstimacionEspera estimacion = service.calcularEstimacion(fila, 0L, 1);

        assertEquals(QueueStatus.SIN_ESPERA, estimacion.getQueueStatus());
        assertEquals(0, estimacion.getTiempoEstimadoMinutos());
    }

    private Fila crearFila(
            Integer capacidadMaxima,
            Integer personasActuales,
            LocalDateTime llenoDesde,
            Integer tiempoPromedioAtencionMinutos
    ) {
        return crearFila(
                TipoOperacionLocal.ATENCION_RAPIDA,
                capacidadMaxima,
                personasActuales,
                llenoDesde,
                tiempoPromedioAtencionMinutos
        );
    }

    private Fila crearFila(
            TipoOperacionLocal tipoOperacion,
            Integer capacidadMaxima,
            Integer personasActuales,
            LocalDateTime llenoDesde,
            Integer tiempoPromedioAtencionMinutos
    ) {
        Local local = Local.builder()
                .id(1L)
                .nombre("Local test")
                .direccion("Direccion test")
                .activo(true)
                .tipoOperacion(tipoOperacion)
                .capacidadMaxima(capacidadMaxima)
                .personasActuales(personasActuales)
                .llenoDesde(llenoDesde)
                .build();

        return Fila.builder()
                .id(1L)
                .local(local)
                .nombre("Fila test")
                .estado(EstadoFila.ABIERTA)
                .queueStatus(QueueStatus.NORMAL)
                .tiempoPromedioAtencionMinutos(tiempoPromedioAtencionMinutos)
                .build();
    }

    @SuppressWarnings("unchecked")
    private <T> T repositoryProxy(Class<T> repositoryType) {
        return (T) Proxy.newProxyInstance(
                repositoryType.getClassLoader(),
                new Class<?>[]{repositoryType},
                (proxy, method, args) -> {
                    if ("save".equals(method.getName())) {
                        return args == null || args.length == 0 ? null : args[0];
                    }
                    return defaultReturn(method.getReturnType(), args);
                }
        );
    }

    private Object defaultReturn(Class<?> returnType, Object[] args) {
        if (returnType == void.class) {
            return null;
        }
        if (returnType == boolean.class) {
            return false;
        }
        if (returnType == long.class) {
            return 0L;
        }
        if (returnType == int.class || returnType == short.class || returnType == byte.class) {
            return 0;
        }
        if (returnType == double.class || returnType == float.class) {
            return 0.0;
        }
        if (Optional.class.isAssignableFrom(returnType)) {
            return Optional.empty();
        }
        if (List.class.isAssignableFrom(returnType)) {
            return List.of();
        }
        return args != null && args.length > 0 && returnType.isInstance(args[0]) ? args[0] : null;
    }

    private static class MetricasSinActividadService extends MetricasFilaService {

        MetricasSinActividadService() {
            super(null, null, null);
        }

        @Override
        public long contarInscripcionesEntre(Long filaId, LocalDateTime desde, LocalDateTime hasta) {
            return 0L;
        }

        @Override
        public long contarLlamadosEntre(Long filaId, LocalDateTime desde, LocalDateTime hasta) {
            return 0L;
        }

        @Override
        public long contarHistoricoPorDiaYFranja(
                Long filaId,
                LocalDateTime desde,
                LocalDateTime hasta,
                DayOfWeek diaSemana,
                Integer franjaHoraria
        ) {
            return 0L;
        }

        @Override
        public double promedioErrorPrediccionDesde(Long filaId, LocalDateTime desde, LocalDateTime hasta) {
            return 0.0;
        }
    }
}
