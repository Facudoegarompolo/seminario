package com.digitalqueue.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

//agregue imports
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import java.util.List;
import org.springframework.http.HttpMethod;
import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
public class SecurityConfig {

        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
                http
                                // Como es API REST y todavía no usamos sesiones ni formularios, lo desactivamos
                                .csrf(AbstractHttpConfigurer::disable)

                                // agrego esta linea de abajo para Habilitar CORS (la seguridad del anvegador)
                                // para que el frontend
                                // React/Vite pueda consumir esta API.
                                // Sin esto, el navegador bloquea las peticiones desde localhost:5173 hacia
                                // localhost:8080. ES NEUVA
                                .cors(withDefaults())
                                // No queremos login HTML de Spring
                                .formLogin(AbstractHttpConfigurer::disable)

                                // No queremos Basic Auth por ahora
                                .httpBasic(AbstractHttpConfigurer::disable)

                                // Más adelante con JWT la API va a ser stateless
                                .sessionManagement(session -> session
                                                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                                .authorizeHttpRequests(auth -> auth

                                                // Permitimos requests OPTIONS para evitar bloqueo CORS
                                                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                                                // Endpoints públicos para usuarios comunes
                                                .requestMatchers("/api/public/**").permitAll()

                                                // WebSocket permitido temporalmente
                                                .requestMatchers("/ws/**").permitAll()

                                                // Admin permitido temporalmente para pruebas MVP
                                                .requestMatchers("/api/admin/**").permitAll()

                                                // Cualquier otra ruta requiere autenticación
                                                .anyRequest().authenticated());

                return http.build();
        }

        /*
         * Configuración CORS para desarrollo.
         *
         * Permite que el frontend React/Vite, que corre en http://localhost:5173,
         * pueda realizar peticiones HTTP al backend Spring Boot, que corre en
         * http://localhost:8080.
         *
         * Se permiten los métodos necesarios para consumir la API REST pública.
         */

        @Bean
        public CorsConfigurationSource corsConfigurationSource() {

                CorsConfiguration configuration = new CorsConfiguration();

                configuration.setAllowedOriginPatterns(List.of("*")); // permite requests desde cualquier origen
                                                                      // (origin)
                configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
                configuration.setAllowedHeaders(List.of("*"));

                UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();

                source.registerCorsConfiguration("/**", configuration);

                return source;
        }
}