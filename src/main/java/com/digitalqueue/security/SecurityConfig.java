package com.digitalqueue.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Como es API REST y todavía no usamos sesiones ni formularios, lo desactivamos
                .csrf(AbstractHttpConfigurer::disable)

                // No queremos login HTML de Spring
                .formLogin(AbstractHttpConfigurer::disable)

                // No queremos Basic Auth por ahora
                .httpBasic(AbstractHttpConfigurer::disable)

                // Más adelante con JWT la API va a ser stateless
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                .authorizeHttpRequests(auth -> auth

                        // Endpoints públicos para usuarios comunes
                        .requestMatchers("/api/public/**").permitAll()

                        // WebSocket, lo dejamos permitido por ahora
                        .requestMatchers("/ws/**").permitAll()

                        // TEMPORAL: admin permitido para poder probar el MVP
                        // Más adelante esto cambia a authenticated()
                        .requestMatchers("/api/admin/**").permitAll()

                        // TEMPORAL MVP/testing: no exigimos JWT en ningun endpoint.
                        // La configuracion de seguridad queda lista para reactivar authenticated().
                        .anyRequest().permitAll()
                );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
