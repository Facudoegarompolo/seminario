package com.digitalqueue.service;

import com.digitalqueue.dto.PushSubscriptionRequest;
import com.digitalqueue.exception.RecursoNoEncontradoException;
import com.digitalqueue.model.NotificacionPush;
import com.digitalqueue.model.PushSubscription;
import com.digitalqueue.model.Turno;
import com.digitalqueue.model.enums.EstadoNotificacion;
import com.digitalqueue.model.enums.TipoNotificacion;
import com.digitalqueue.repository.NotificacionPushRepository;
import com.digitalqueue.repository.PushSubscriptionRepository;
import com.digitalqueue.repository.TurnoRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import nl.martijndwars.webpush.Notification;
import nl.martijndwars.webpush.PushService;
import org.apache.http.HttpResponse;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.jose4j.lang.JoseException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.Security;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import org.apache.http.util.EntityUtils;

@Service
@RequiredArgsConstructor
public class PushNotificationService {

    private final TurnoRepository turnoRepository;
    private final PushSubscriptionRepository pushSubscriptionRepository;
    private final NotificacionPushRepository notificacionPushRepository;
    private final JsonMapper jsonMapper;

    @Value("${app.push.vapid.public-key:}")
    private String vapidPublicKey;

    @Value("${app.push.vapid.private-key:}")
    private String vapidPrivateKey;

    @Value("${app.push.vapid.subject:mailto:admin@digitalqueue.local}")
    private String vapidSubject;

    @PostConstruct
    public void configurarProveedorCriptografico() {
        if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
            Security.addProvider(new BouncyCastleProvider());
        }
    }

    public String obtenerClavePublica() {
        System.out.println("VAPID PUBLIC KEY: " + vapidPublicKey);
        return vapidPublicKey;
    }

    @PostConstruct
    public void debugVapid() {
        System.out.println("PUBLIC KEY = " + vapidPublicKey);
        System.out.println("PRIVATE KEY = " + vapidPrivateKey);
        System.out.println("SUBJECT = " + vapidSubject);
    }

    @Transactional
    public void registrarSuscripcion(String tokenPublico, PushSubscriptionRequest request) {
        Turno turno = turnoRepository.findByTokenPublico(tokenPublico)
                .orElseThrow(() -> new RecursoNoEncontradoException("Turno no encontrado"));

        PushSubscription subscription = pushSubscriptionRepository
                .findByTurnoIdAndEndpoint(turno.getId(), request.getEndpoint())
                .orElseGet(() -> PushSubscription.builder()
                        .turno(turno)
                        .endpoint(request.getEndpoint())
                        .build());

        subscription.setP256dh(request.getP256dh());
        subscription.setAuth(request.getAuth());
        subscription.setActivo(true);

        pushSubscriptionRepository.save(subscription);
    }

    @Transactional
    public void registrarNotificacionPendiente(Turno turno, TipoNotificacion tipo) {
        if (turno == null || turno.getId() == null) {
            return;
        }

        List<PushSubscription> subscriptions = pushSubscriptionRepository.findByTurnoIdAndActivoTrue(turno.getId());
        if (subscriptions.isEmpty()) {
            return;
        }

        PushService pushService = null;
        String errorConfiguracion = null;
        if (pushConfigurado()) {
            try {
                pushService = new PushService(vapidPublicKey, vapidPrivateKey, vapidSubject);
            } catch (GeneralSecurityException ex) {
                errorConfiguracion = "Configuracion VAPID invalida: " + ex.getMessage();
            }
        }

        for (PushSubscription subscription : subscriptions) {
            NotificacionPush notificacion = NotificacionPush.builder()
                    .turno(turno)
                    .pushSubscription(subscription)
                    .tipo(tipo)
                    .estado(EstadoNotificacion.PENDIENTE)
                    .titulo(tituloPara(tipo))
                    .mensaje(mensajePara(tipo, turno))
                    .build();

            notificacionPushRepository.save(notificacion);

            if (pushService == null) {
                if (errorConfiguracion != null) {
                    marcarError(notificacion, errorConfiguracion);
                }
                continue;
            }

            enviarNotificacion(pushService, subscription, notificacion, turno);
        }
    }

    private boolean pushConfigurado() {
        return StringUtils.hasText(vapidPublicKey) && StringUtils.hasText(vapidPrivateKey);
    }

    private void enviarNotificacion(
            PushService pushService,
            PushSubscription subscription,
            NotificacionPush notificacion,
            Turno turno) {
        try {
            System.out.println("ENDPOINT: " + subscription.getEndpoint());
            System.out.println("P256DH: " + subscription.getP256dh());
            System.out.println("AUTH: " + subscription.getAuth());
            System.out.println("PUBLIC KEY USADA: " + vapidPublicKey);
            Notification notification = new Notification(
                    subscription.getEndpoint(),
                    subscription.getP256dh(),
                    subscription.getAuth(),
                    crearPayload(notificacion, turno));
            HttpResponse response = pushService.send(notification);

            System.out.println(
                    "PUSH RESPONSE: " +
                            response.getStatusLine());

            if (response.getEntity() != null) {
                System.out.println(
                        "PUSH BODY: " +
                                EntityUtils.toString(response.getEntity()));
            }

            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode >= 200 && statusCode < 300) {
                marcarEnviada(notificacion);
                return;
            }

            if (statusCode == 404 || statusCode == 410) {
                subscription.setActivo(false);
            }
            marcarError(notificacion, "El push service respondio HTTP " + statusCode);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            marcarError(notificacion, errorPara(ex));
        } catch (GeneralSecurityException | IOException | JoseException | ExecutionException ex) {
            marcarError(notificacion, errorPara(ex));
        }
    }

    private String crearPayload(NotificacionPush notificacion, Turno turno) throws JacksonException {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("title", notificacion.getTitulo());
        payload.put("body", notificacion.getMensaje());
        payload.put("tipo", notificacion.getTipo().name());
        payload.put("turnoId", turno.getId());
        payload.put("numeroTurno", turno.getNumeroTurno());
        payload.put("tokenPublico", turno.getTokenPublico());

        return jsonMapper.writeValueAsString(payload);
    }

    private void marcarEnviada(NotificacionPush notificacion) {
        System.out.println("PUSH ENVIADA");
        notificacion.setEstado(EstadoNotificacion.ENVIADA);
        notificacion.setSentAt(LocalDateTime.now());
        notificacion.setErrorEnvio(null);
    }

    private void marcarError(NotificacionPush notificacion, String error) {
        System.out.println("PUSH ERROR: " + error);
        notificacion.setEstado(EstadoNotificacion.ERROR);
        notificacion.setErrorEnvio(error);
    }

    private String errorPara(Exception ex) {
        String message = ex.getMessage();
        if (!StringUtils.hasText(message)) {
            return ex.getClass().getSimpleName();
        }
        return ex.getClass().getSimpleName() + ": " + message;
    }

    private String tituloPara(TipoNotificacion tipo) {
        return switch (tipo) {
            case TURNO_PROXIMO -> "Tu turno se acerca";
            case TURNO_LLAMADO -> "Es tu turno";
            case TURNO_CANCELADO -> "Turno cancelado";
            case NO_PRESENTADO -> "Turno no presentado";
            case TURNO_EXPIRADO -> "Turno expirado";
            case POSICION_ACTUALIZADA -> "Tu posicion cambio";
        };
    }

    private String mensajePara(TipoNotificacion tipo, Turno turno) {
        String numeroTurno = turno.getNumeroTurno() == null ? "" : " #" + turno.getNumeroTurno();

        return switch (tipo) {
            case TURNO_PROXIMO -> "Estate atento: tu turno" + numeroTurno + " esta proximo.";
            case TURNO_LLAMADO -> "Presentate ahora: llamaron tu turno" + numeroTurno + ".";
            case TURNO_CANCELADO -> "Tu turno" + numeroTurno + " fue cancelado.";
            case NO_PRESENTADO -> "Tu turno" + numeroTurno + " fue marcado como no presentado.";
            case TURNO_EXPIRADO -> "Tu turno" + numeroTurno + " expiro.";
            case POSICION_ACTUALIZADA -> "Actualizamos tu posicion en la fila.";
        };
    }

}
