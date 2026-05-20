package com.digitalqueue.config;

import com.digitalqueue.model.Fila;
import com.digitalqueue.model.Local;
import com.digitalqueue.model.PuntoAcceso;
import com.digitalqueue.model.UsuarioAdmin;
import com.digitalqueue.model.enums.EstadoFila;
import com.digitalqueue.model.enums.QueueStatus;
import com.digitalqueue.model.enums.RolAdmin;
import com.digitalqueue.model.enums.TipoAcceso;
import com.digitalqueue.model.enums.TipoDia;
import com.digitalqueue.repository.FilaRepository;
import com.digitalqueue.repository.LocalRepository;
import com.digitalqueue.repository.PuntoAccesoRepository;
import com.digitalqueue.repository.UsuarioAdminRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.core.annotation.Order;

@Component
@RequiredArgsConstructor
@Order(1)
public class DataLoader implements CommandLineRunner {

    private static final String ADMIN_EMAIL = "admin@digitalqueue.com";
    private static final String ADMIN_PASSWORD = "admin123";

    private final LocalRepository localRepository;
    private final FilaRepository filaRepository;
    private final PuntoAccesoRepository puntoAccesoRepository;
    private final UsuarioAdminRepository usuarioAdminRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (localRepository.count() > 0) {
            crearAdminSiNoExiste();
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

        crearAdmin(localGuardado);
    }

    private void crearAdminSiNoExiste() {
        if (usuarioAdminRepository.existsByEmail(ADMIN_EMAIL)) {
            return;
        }

        Local local = localRepository.findAll().stream()
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No hay locales cargados para crear el admin"));

        crearAdmin(local);
    }

    private void crearAdmin(Local local) {
        UsuarioAdmin admin = UsuarioAdmin.builder()
                .local(local)
                .nombre("Admin")
                .email(ADMIN_EMAIL)
                .password(passwordEncoder.encode(ADMIN_PASSWORD))
                .rol(RolAdmin.ADMIN_LOCAL)
                .activo(true)
                .build();

        usuarioAdminRepository.save(admin);
    }
}
