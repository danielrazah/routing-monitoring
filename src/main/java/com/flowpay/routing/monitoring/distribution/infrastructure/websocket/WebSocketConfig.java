package com.flowpay.routing.monitoring.distribution.infrastructure.websocket;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * STOMP over native WebSocket for the live dashboard. The browser connects to /ws and
 * subscribes to /topic/dashboard, where we push every distribution event as it happens.
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // A simple in-memory broker is plenty for broadcasting to dashboards.
        registry.enableSimpleBroker("/topic");
        registry.setApplicationDestinationPrefixes("/app");
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
