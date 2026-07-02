package com.flowpay.routing.monitoring.distribution.infrastructure.websocket;

import com.flowpay.routing.monitoring.distribution.infrastructure.config.RealtimeProperties;
import org.junit.jupiter.api.Test;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.config.StompBrokerRelayRegistration;

import static org.mockito.Mockito.RETURNS_SELF;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class WebSocketConfigTest {

    @Test
    void simpleTransport_enablesInMemoryBroker() {
        RealtimeProperties props = new RealtimeProperties(); // defaults to SIMPLE
        MessageBrokerRegistry registry = mock(MessageBrokerRegistry.class);

        new WebSocketConfig(props).configureMessageBroker(registry);

        verify(registry).setApplicationDestinationPrefixes("/app");
        verify(registry).enableSimpleBroker("/topic");
        verify(registry, never()).enableStompBrokerRelay(any());
    }

    @Test
    void brokerTransport_relaysToExternalBrokerWithConfiguredConnection() {
        RealtimeProperties props = new RealtimeProperties();
        props.setTransport(RealtimeProperties.Transport.BROKER);
        props.getRelay().setHost("rabbitmq");
        props.getRelay().setPort(61613);
        props.getRelay().setVirtualHost("/");
        props.getRelay().setLogin("app");
        props.getRelay().setPasscode("secret");

        // RETURNS_SELF so the fluent setters keep returning the same registration.
        StompBrokerRelayRegistration relayRegistration =
                mock(StompBrokerRelayRegistration.class, RETURNS_SELF);
        MessageBrokerRegistry registry = mock(MessageBrokerRegistry.class);
        when(registry.enableStompBrokerRelay("/topic")).thenReturn(relayRegistration);

        new WebSocketConfig(props).configureMessageBroker(registry);

        verify(registry).setApplicationDestinationPrefixes("/app");
        verify(registry).enableStompBrokerRelay("/topic");
        verify(registry, never()).enableSimpleBroker(any());
        verify(relayRegistration).setRelayHost("rabbitmq");
        verify(relayRegistration).setRelayPort(61613);
        verify(relayRegistration).setVirtualHost("/");
        verify(relayRegistration).setClientLogin("app");
        verify(relayRegistration).setClientPasscode("secret");
        verify(relayRegistration).setSystemLogin("app");
        verify(relayRegistration).setSystemPasscode("secret");
    }
}
