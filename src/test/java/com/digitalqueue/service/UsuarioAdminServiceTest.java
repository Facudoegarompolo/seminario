package com.digitalqueue.service;

import com.digitalqueue.dto.CrearAdminRequest;
import com.digitalqueue.dto.CrearAdminResponse;
import com.digitalqueue.model.Fila;
import com.digitalqueue.model.Local;
import com.digitalqueue.model.PuntoAcceso;
import com.digitalqueue.model.UsuarioAdmin;
import com.digitalqueue.repository.FilaRepository;
import com.digitalqueue.repository.LocalRepository;
import com.digitalqueue.repository.PuntoAccesoRepository;
import com.digitalqueue.repository.UsuarioAdminRepository;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UsuarioAdminServiceTest {

    private final UsuarioAdminRepository usuarioAdminRepository = mock(UsuarioAdminRepository.class);
    private final LocalRepository localRepository = mock(LocalRepository.class);
    private final FilaRepository filaRepository = mock(FilaRepository.class);
    private final PuntoAccesoRepository puntoAccesoRepository = mock(PuntoAccesoRepository.class);
    private final PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
    private final UsuarioAdminService service = new UsuarioAdminService(
            usuarioAdminRepository,
            localRepository,
            filaRepository,
            puntoAccesoRepository,
            passwordEncoder
    );

    @Test
    void devuelveEmailExistenteCuandoYaHayAdminConEseMail() {
        CrearAdminRequest request = request(" Facundo ", " Admin@Test.com ", "password123");
        when(usuarioAdminRepository.existsByEmailIgnoreCase("admin@test.com")).thenReturn(true);

        CrearAdminResponse response = service.crearCuentaAdministrador(request);

        assertFalse(response.getCreado());
        assertEquals("Ya existe una cuenta administradora con ese email.", response.getMensaje());
        verify(localRepository, never()).save(any());
        verify(filaRepository, never()).save(any());
        verify(puntoAccesoRepository, never()).save(any());
        verify(usuarioAdminRepository, never()).save(any());
    }

    @Test
    void creaAdminCuandoElMailNoExiste() {
        CrearAdminRequest request = request(" Nuevo Admin ", "nuevo@test.com", "password123");
        Local local = Local.builder()
                .id(10L)
                .nombre("Cafe Test")
                .direccion("Direccion Test")
                .linkImagenLogo("https://cdn.test/logo.png")
                .activo(true)
                .capacidadMaxima(20)
                .build();

        when(usuarioAdminRepository.existsByEmailIgnoreCase("nuevo@test.com")).thenReturn(false);
        when(localRepository.save(any(Local.class))).thenReturn(local);
        when(filaRepository.save(any(Fila.class))).thenAnswer(invocation -> {
            Fila fila = invocation.getArgument(0);
            fila.setId(30L);
            return fila;
        });
        when(puntoAccesoRepository.save(any(PuntoAcceso.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(passwordEncoder.encode("password123")).thenReturn("hash-password");
        when(usuarioAdminRepository.save(any(UsuarioAdmin.class))).thenAnswer(invocation -> {
            UsuarioAdmin usuario = invocation.getArgument(0);
            usuario.setId(20L);
            return usuario;
        });

        CrearAdminResponse response = service.crearCuentaAdministrador(request);

        assertTrue(response.getCreado());
        assertEquals("Cuenta administradora creada correctamente.", response.getMensaje());
        verify(passwordEncoder).encode(eq("password123"));
    }

    private CrearAdminRequest request(String nombre, String email, String password) {
        CrearAdminRequest request = new CrearAdminRequest();
        request.setNombre(nombre);
        request.setEmail(email);
        request.setPassword(password);
        request.setNombreLocal(" Cafe Test ");
        request.setDireccionLocal(" Direccion Test ");
        request.setLinkImagenLogoLocal(" https://cdn.test/logo.png ");
        return request;
    }
}
