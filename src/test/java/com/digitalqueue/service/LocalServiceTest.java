package com.digitalqueue.service;

import com.digitalqueue.dto.ActualizarLogoLocalRequest;
import com.digitalqueue.dto.ActualizarLogoLocalResponse;
import com.digitalqueue.exception.RecursoNoEncontradoException;
import com.digitalqueue.model.Local;
import com.digitalqueue.repository.LocalRepository;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class LocalServiceTest {

    private final LocalRepository localRepository = mock(LocalRepository.class);
    private final LocalService service = new LocalService(localRepository);

    @Test
    void actualizaLogoDelLocal() {
        Local local = Local.builder()
                .id(10L)
                .nombre("Cafe Test")
                .direccion("Direccion Test")
                .linkImagenLogo("https://cdn.test/logo-viejo.png")
                .activo(true)
                .build();
        ActualizarLogoLocalRequest request = new ActualizarLogoLocalRequest();
        request.setLinkImagenLogo(" https://cdn.test/logo-nuevo.png ");

        when(localRepository.findById(10L)).thenReturn(Optional.of(local));
        when(localRepository.save(any(Local.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ActualizarLogoLocalResponse response = service.actualizarLogo(10L, request);

        assertTrue(response.getActualizado());
        assertEquals("Imagen del local actualizada correctamente.", response.getMensaje());
        assertEquals("https://cdn.test/logo-nuevo.png", local.getLinkImagenLogo());
        verify(localRepository).save(local);
    }

    @Test
    void permiteQuitarLogoConTextoVacio() {
        Local local = Local.builder()
                .id(10L)
                .nombre("Cafe Test")
                .direccion("Direccion Test")
                .linkImagenLogo("https://cdn.test/logo-viejo.png")
                .activo(true)
                .build();
        ActualizarLogoLocalRequest request = new ActualizarLogoLocalRequest();
        request.setLinkImagenLogo(" ");

        when(localRepository.findById(10L)).thenReturn(Optional.of(local));
        when(localRepository.save(any(Local.class))).thenAnswer(invocation -> invocation.getArgument(0));

        service.actualizarLogo(10L, request);

        assertNull(local.getLinkImagenLogo());
        verify(localRepository).save(local);
    }

    @Test
    void fallaSiElLocalNoExiste() {
        ActualizarLogoLocalRequest request = new ActualizarLogoLocalRequest();
        request.setLinkImagenLogo("https://cdn.test/logo.png");

        when(localRepository.findById(99L)).thenReturn(Optional.empty());

        RecursoNoEncontradoException exception = assertThrows(
                RecursoNoEncontradoException.class,
                () -> service.actualizarLogo(99L, request)
        );

        assertEquals("Local no encontrado.", exception.getMessage());
    }
}
