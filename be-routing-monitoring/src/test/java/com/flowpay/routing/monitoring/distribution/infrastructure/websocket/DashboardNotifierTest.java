package com.flowpay.routing.monitoring.distribution.infrastructure.websocket;

import com.flowpay.routing.monitoring.distribution.domain.event.DomainEvent;
import com.flowpay.routing.monitoring.distribution.domain.event.InteractionEnded;
import org.junit.jupiter.api.Test;
import org.springframework.messaging.MessageDeliveryException;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class DashboardNotifierTest {

    private final SimpMessagingTemplate messaging = mock(SimpMessagingTemplate.class);
    private final DashboardNotifier notifier = new DashboardNotifier(messaging);
    private final DomainEvent event =
            InteractionEnded.now(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID());

    @Test
    void sendsTheEventToTheDashboardTopic() {
        notifier.onDomainEvent(event);
        verify(messaging).convertAndSend(eq("/topic/dashboard"), any(DashboardMessage.class));
    }

    @Test
    void swallowsDeliveryFailuresSoTheBusinessFlowIsUnaffected() {
        doThrow(new MessageDeliveryException("broker not active"))
                .when(messaging).convertAndSend(eq("/topic/dashboard"), any(DashboardMessage.class));

        // The commit already happened, so a broker hiccup must not propagate.
        notifier.onDomainEvent(event);

        verify(messaging).convertAndSend(eq("/topic/dashboard"), any(DashboardMessage.class));
    }
}
