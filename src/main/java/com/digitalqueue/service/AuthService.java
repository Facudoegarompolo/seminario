package com.digitalqueue.service;

import com.digitalqueue.dto.login.LoginRequest;
import com.digitalqueue.dto.login.LoginResponse;
import com.digitalqueue.exception.CredencialesInvalidasException;
import com.digitalqueue.model.UsuarioAdmin;
import com.digitalqueue.repository.UsuarioAdminRepository;
import com.digitalqueue.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UsuarioAdminRepository usuarioAdminRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public LoginResponse login(LoginRequest request) {
        UsuarioAdmin usuario = usuarioAdminRepository.findByEmail(request.getEmail())
                .filter(u -> Boolean.TRUE.equals(u.getActivo()))
                .orElseThrow(() -> new CredencialesInvalidasException("Credenciales incorrectas"));

        if (!passwordEncoder.matches(request.getPassword(), usuario.getPassword())) {
            throw new CredencialesInvalidasException("Credenciales incorrectas");
        }

        return new LoginResponse(
                jwtService.generateToken(usuario),
                usuario.getId(),
                usuario.getLocal().getId(),
                usuario.getNombre(),
                usuario.getEmail(),
                usuario.getRol()
        );
    }
}
