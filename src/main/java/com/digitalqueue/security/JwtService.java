package com.digitalqueue.security;

import com.digitalqueue.model.UsuarioAdmin;
import com.digitalqueue.model.enums.RolAdmin;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import lombok.extern.slf4j.Slf4j;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.security.SecureRandom;

@Service
@Slf4j
public class JwtService {

    private final SecretKey secretKey;
    private final long expirationHours;

    public JwtService(
            @Value("${app.jwt.secret}") String secret,
            @Value("${app.jwt.expiration-hours}") long expirationHours
    ) {
        this.secretKey = crearClave(secret);
        this.expirationHours = expirationHours;
    }

    private SecretKey crearClave(String secret) {
        if (StringUtils.hasText(secret)) {
            return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        }

        byte[] randomKey = new byte[32];
        new SecureRandom().nextBytes(randomKey);
        log.warn("APP_JWT_SECRET no está configurada; los inicios de sesión vencerán al reiniciar el servidor");
        return Keys.hmacShaKeyFor(randomKey);
    }

    public String generateToken(UsuarioAdmin usuario) {
        Instant now = Instant.now();
        Instant expiry = now.plus(expirationHours, ChronoUnit.HOURS);

        return Jwts.builder()
                .subject(usuario.getEmail())
                .claim("usuarioId", usuario.getId())
                .claim("localId", usuario.getLocal().getId())
                .claim("rol", usuario.getRol().name())
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiry))
                .signWith(secretKey)
                .compact();
    }

    public Long extractUsuarioId(String token) {
        return numeroComoLong(getClaims(token).get("usuarioId"));
    }

    public Long extractLocalId(String token) {
        return numeroComoLong(getClaims(token).get("localId"));
    }

    public RolAdmin extractRol(String token) {
        return RolAdmin.valueOf(getClaims(token).get("rol", String.class));
    }

    public AdminPrincipal parsePrincipal(String token) {
        Claims claims = getClaims(token);
        return new AdminPrincipal(
                numeroComoLong(claims.get("usuarioId")),
                numeroComoLong(claims.get("localId")),
                claims.getSubject(),
                RolAdmin.valueOf(claims.get("rol", String.class))
        );
    }

    private Long numeroComoLong(Object value) {
        if (value instanceof Number number) {
            return number.longValue();
        }
        throw new IllegalArgumentException("El token no contiene los identificadores requeridos");
    }

    private Claims getClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
