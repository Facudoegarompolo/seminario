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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PushNotificationService {

    private final TurnoRepository turnoRepository;
    private final PushSubscriptionRepository pushSubscriptionRepository;
    private final NotificacionPushRepository notificacionPushRepository;

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
        if (!pushSubscriptionRepository.existsByTurnoIdAndActivoTrue(turno.getId())) {
            return;
        }

        NotificacionPush notificacion = NotificacionPush.builder()
                .turno(turno)
                .tipo(tipo)
                .estado(EstadoNotificacion.PENDIENTE)
                .titulo(tituloPara(tipo))
                .mensaje(mensajePara(tipo, turno))
                .build();

        notificacionPushRepository.save(notificacion);
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
