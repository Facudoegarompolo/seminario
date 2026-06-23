package com.digitalqueue.security;

import com.digitalqueue.model.UsuarioAdmin;
import com.digitalqueue.repository.UsuarioAdminRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AdminSecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private UsuarioAdminRepository usuarioAdminRepository;

    @Test
    void rechazaApiAdministrativaSinToken() throws Exception {
        mockMvc.perform(get("/api/admin/fila/turnos"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void permiteApiAdministrativaConTokenValido() throws Exception {
        UsuarioAdmin admin = usuarioAdminRepository.findByEmail("admin@digitalqueue.com")
                .orElseThrow();
        String token = jwtService.generateToken(admin);

        mockMvc.perform(get("/api/admin/fila/turnos")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    void mantienePublicasLasRutasDeClientes() throws Exception {
        mockMvc.perform(get("/api/public/filas/starbucks-uade/estado"))
                .andExpect(status().isOk());
    }
}
