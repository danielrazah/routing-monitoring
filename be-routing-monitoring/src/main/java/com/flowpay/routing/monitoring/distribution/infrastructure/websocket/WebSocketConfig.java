package com.flowpay.routing.monitoring.distribution.infrastructure.websocket;

import com.flowpay.routing.monitoring.distribution.infrastructure.config.RealtimeProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * STOMP over native WebSocket for the live dashboard. The browser connects to /ws and
 * subscribes to /topic/dashboard, where we push every distribution event as it happens.
 *
 * <p>The broker backing /topic is pluggable (see {@link RealtimeProperties}):
 * an in-memory broker for a single instance (default), or a relay to an external STOMP
 * broker (e.g. RabbitMQ) so several instances share the same topic and scale horizontally.
 * Either way the publishing side ({@code DashboardNotifier}, {@code SimpMessagingTemplate})
 * and the browser stay exactly the same.
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final RealtimeProperties realtime;

    public WebSocketConfig(RealtimeProperties realtime) {
        this.realtime = realtime;
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.setApplicationDestinationPrefixes("/app");

        if (realtime.getTransport() == RealtimeProperties.Transport.BROKER) {
            // Horizontal scale: hand /topic off to an external STOMP broker. Events sent from
            // any instance are fanned out by the broker to dashboards connected to every
            // instance. Needs a broker with the STOMP plugin (see docker-compose profile).
            RealtimeProperties.Relay relay = realtime.getRelay();
            registry.enableStompBrokerRelay("/topic")
                    .setRelayHost(relay.getHost())
                    .setRelayPort(relay.getPort())
                    // Without this the relay sends the relay host as the STOMP `host` header,
                    // which RabbitMQ reads as a (non-existent) virtual host and rejects.
                    .setVirtualHost(relay.getVirtualHost())
                    .setClientLogin(relay.getLogin())
                    .setClientPasscode(relay.getPasscode())
                    .setSystemLogin(relay.getLogin())
                    .setSystemPasscode(relay.getPasscode());
        } else {
            // Single instance: an in-memory broker is plenty and needs no extra infrastructure.
            registry.enableSimpleBroker("/topic");
        }
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Native WebSocket: a single upgrade to /ws that proxies cleanly through nginx.
        // Browsers that block ws:// (Safari on localhost) simply don't get the live push;
        // the dashboard falls back to short-interval polling of /api/dashboard on the client.
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*");
    }
}
