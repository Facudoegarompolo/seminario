package com.digitalqueue.service;

import com.digitalqueue.dto.CrearAdminRequest;
import com.digitalqueue.dto.CrearAdminResponse;
import com.digitalqueue.exception.RecursoNoEncontradoException;
import com.digitalqueue.model.Local;
import com.digitalqueue.model.UsuarioAdmin;
import com.digitalqueue.model.enums.RolAdmin;
import com.digitalqueue.repository.LocalRepository;
import com.digitalqueue.repository.UsuarioAdminRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
@RequiredArgsConstructor
public class UsuarioAdminService {

    private final UsuarioAdminRepository usuarioAdminRepository;
    private final LocalRepository localRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public CrearAdminResponse crearCuentaAdministrador(CrearAdminRequest request) {
        String email = normalizarEmail(request.getEmail());

        if (usuarioAdminRepository.existsByEmailIgnoreCase(email)) {
            return CrearAdminResponse.builder()
                    .creado(false)
                    .emailExistente(true)
                    .mensaje("Ya existe una cuenta administradora con ese email.")
                    .email(email)
                    .build();
        }

        Local local = localRepository.findAll()
                .stream()
                .findFirst()
                .orElseThrow(() -> new RecursoNoEncontradoException("No hay locales disponibles para asociar la cuenta"));

        UsuarioAdmin usuarioAdmin = UsuarioAdmin.builder()
                .local(local)
                .nombre(nombreDesdeEmail(email))
                .email(email)
                .password(passwordEncoder.encode(request.getPassword()))
                .rol(RolAdmin.ADMIN_LOCAL)
                .activo(true)
                .build();

        UsuarioAdmin usuarioGuardado = usuarioAdminRepository.save(usuarioAdmin);

        return CrearAdminResponse.builder()
                .creado(true)
                .emailExistente(false)
                .mensaje("Cuenta administradora creada correctamente.")
                .usuarioId(usuarioGuardado.getId())
                .localId(local.getId())
                .email(usuarioGuardado.getEmail())
                .rol(usuarioGuardado.getRol())
                .build();
    }

    private String normalizarEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private String nombreDesdeEmail(String email) {
        int arroba = email.indexOf('@');
        if (arroba <= 0) {
            return email;
        }
        return email.substring(0, arroba);
    }
}
