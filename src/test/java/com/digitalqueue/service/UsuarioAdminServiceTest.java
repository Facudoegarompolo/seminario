package com.digitalqueue.service;

import com.digitalqueue.dto.CrearAdminRequest;
import com.digitalqueue.dto.CrearAdminResponse;
import com.digitalqueue.model.Local;
import com.digitalqueue.model.UsuarioAdmin;
import com.digitalqueue.model.enums.RolAdmin;
import com.digitalqueue.repository.LocalRepository;
import com.digitalqueue.repository.UsuarioAdminRepository;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;

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
    private final PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
    private final UsuarioAdminService service = new UsuarioAdminService(
            usuarioAdminRepository,
            localRepository,
            passwordEncoder
    );

    @Test
    void devuelveEmailExistenteCuandoYaHayAdminConEseMail() {
        CrearAdminRequest request = request(" Admin@Test.com ", "password123");
        when(usuarioAdminRepository.existsByEmailIgnoreCase("admin@test.com")).thenReturn(true);

        CrearAdminResponse response = service.crearCuentaAdministrador(request);

        assertFalse(response.getCreado());
        assertTrue(response.getEmailExistente());
        assertEquals("admin@test.com", response.getEmail());
        verify(usuarioAdminRepository, never()).save(any());
    }

    @Test
    void creaAdminCuandoElMailNoExiste() {
        CrearAdminRequest request = request("nuevo@test.com", "password123");
        Local local = Local.builder()
                .id(10L)
                .nombre("Local test")
                .direccion("Direccion test")
                .activo(true)
                .build();

        when(usuarioAdminRepository.existsByEmailIgnoreCase("nuevo@test.com")).thenReturn(false);
        when(localRepository.findAll()).thenReturn(List.of(local));
        when(passwordEncoder.encode("password123")).thenReturn("hash-password");
        when(usuarioAdminRepository.save(any(UsuarioAdmin.class))).thenAnswer(invocation -> {
            UsuarioAdmin usuario = invocation.getArgument(0);
            usuario.setId(20L);
            return usuario;
        });

        CrearAdminResponse response = service.crearCuentaAdministrador(request);

        assertTrue(response.getCreado());
        assertFalse(response.getEmailExistente());
        assertEquals(20L, response.getUsuarioId());
        assertEquals(10L, response.getLocalId());
        assertEquals("nuevo@test.com", response.getEmail());
        assertEquals(RolAdmin.ADMIN_LOCAL, response.getRol());
        verify(passwordEncoder).encode(eq("password123"));
    }

    private CrearAdminRequest request(String email, String password) {
        CrearAdminRequest request = new CrearAdminRequest();
        request.setEmail(email);
        request.setPassword(password);
        return request;
    }
}
