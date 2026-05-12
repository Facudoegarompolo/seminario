package com.digitalqueue.config;

import com.digitalqueue.model.Fila;
import com.digitalqueue.model.Local;
import com.digitalqueue.model.PuntoAcceso;
import com.digitalqueue.model.enums.EstadoFila;
import com.digitalqueue.model.enums.QueueStatus;
import com.digitalqueue.model.enums.TipoAcceso;
import com.digitalqueue.model.enums.TipoDia;
import com.digitalqueue.repository.FilaRepository;
import com.digitalqueue.repository.LocalRepository;
import com.digitalqueue.repository.PuntoAccesoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataLoader implements CommandLineRunner {

    private final LocalRepository localRepository;
    private final FilaRepository filaRepository;
    private final PuntoAccesoRepository puntoAccesoRepository;

    @Override
    public void run(String... args) {
        if (localRepository.count() > 0) {
            return;
        }

        Local local = Local.builder()
                .nombre("Starbucks UADE")
                .direccion("Lima 775")
                .activo(true)
                .capacidadMaxima(20)
                .personasActuales(0)
                .build();

        Local localGuardado = localRepository.save(local);

        Fila fila = Fila.builder()
                .local(localGuardado)
                .nombre("Caja principal")
                .estado(EstadoFila.ABIERTA)
                .queueStatus(QueueStatus.NORMAL)
                .tipoDia(TipoDia.NORMAL)
                .tiempoPromedioAtencionMinutos(3)
                .build();

        Fila filaGuardada = filaRepository.save(fila);

        PuntoAcceso puntoAcceso = PuntoAcceso.builder()
                .fila(filaGuardada)
                .codigoPublico("starbucks-uade")
                .tipoAcceso(TipoAcceso.QR)
                .activo(true)
                .build();

        puntoAccesoRepository.save(puntoAcceso);
    }
}
