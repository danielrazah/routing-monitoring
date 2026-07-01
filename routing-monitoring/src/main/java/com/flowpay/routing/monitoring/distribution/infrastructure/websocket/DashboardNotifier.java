package com.flowpay.routing.monitoring.distribution.infrastructure.websocket;

import com.flowpay.routing.monitoring.distribution.domain.event.DomainEvent;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Pushes distribution events to the dashboard. It listens after the transaction commits,
 * so the dashboard only ever hears about changes that actually stuck in the database.
 */
@Component
public class DashboardNotifier {

    private static final String DASHBOARD_TOPIC = "/topic/dashboard";

    private final SimpMessagingTemplate messaging;

    public DashboardNotifier(SimpMessagingTemplate messaging) {
        this.messaging = messaging;
    }

    @TransactionalEventListener(fallbackExecution = true)
    public void onDomainEvent(DomainEvent event) {
        messaging.convertAndSend(DASHBOARD_TOPIC, DashboardMessage.from(event));
    }
}
