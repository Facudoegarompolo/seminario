package com.digitalqueue.service;

import com.digitalqueue.model.PushSubscription;
import com.digitalqueue.model.Turno;
import com.digitalqueue.repository.NotificacionPushRepository;
import com.digitalqueue.repository.PushSubscriptionRepository;
import com.digitalqueue.repository.TurnoRepository;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PushNotificationServiceTest {

    @Test
    void recuperaElTurnoActivoAsociadoAlDispositivo() {
        PushSubscriptionRepository subscriptionRepository = mock(PushSubscriptionRepository.class);
        PushNotificationService service = new PushNotificationService(
                mock(TurnoRepository.class),
                subscriptionRepository,
                mock(NotificacionPushRepository.class),
                mock(JsonMapper.class)
        );
        Turno turno = Turno.builder().tokenPublico("turno-iphone").build();
        PushSubscription subscription = PushSubscription.builder().turno(turno).build();

        when(subscriptionRepository
                .findFirstByEndpointAndActivoTrueAndTurnoEstadoInOrderByUpdatedAtDesc(
                        eq("https://push.example/device"),
                        anyList()
                ))
                .thenReturn(Optional.of(subscription));

        String token = service.recuperarTokenTurnoActivo("https://push.example/device");

        assertEquals("turno-iphone", token);
    }
}
