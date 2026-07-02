package com.flowpay.routing.monitoring.distribution.infrastructure.websocket;

import com.flowpay.routing.monitoring.distribution.domain.event.DomainEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Pushes distribution events to the dashboard. It listens after the transaction commits,
 * so the dashboard only ever hears about changes that actually stuck in the database.
 *
 * <p>The push is best-effort: it runs after the commit, so a broker that is down or still
 * connecting (e.g. the STOMP relay in {@code transport=broker}) must never break the business
 * operation that already succeeded. We swallow and log delivery failures; the dashboard falls
 * back to polling when the live stream is unavailable.
 */
@Component
public class DashboardNotifier {

    private static final Logger log = LoggerFactory.getLogger(DashboardNotifier.class);
    private static final String DASHBOARD_TOPIC = "/topic/dashboard";

    private final SimpMessagingTemplate messaging;

    public DashboardNotifier(SimpMessagingTemplate messaging) {
        this.messaging = messaging;
    }

    @TransactionalEventListener(fallbackExecution = true)
    public void onDomainEvent(DomainEvent event) {
        try {
            messaging.convertAndSend(DASHBOARD_TOPIC, DashboardMessage.from(event));
        } catch (RuntimeException e) {
            // The commit already happened; a realtime hiccup is not the caller's problem.
            log.warn("Could not push dashboard event {} (clients will catch up via polling): {}",
                    event.getClass().getSimpleName(), e.getMessage());
        }
    }
}
