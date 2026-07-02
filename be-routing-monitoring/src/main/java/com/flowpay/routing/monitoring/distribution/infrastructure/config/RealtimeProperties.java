package com.flowpay.routing.monitoring.distribution.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * How the dashboard's real-time push is transported, bound from {@code distribution.realtime.*}.
 *
 * <p>Two modes, chosen without touching any publishing code (see {@code WebSocketConfig}):
 * <ul>
 *   <li>{@code SIMPLE} — an in-memory STOMP broker. Zero extra infrastructure, but each
 *       backend instance only reaches the dashboards connected to itself. This is the
 *       default and matches the single-instance deployment.</li>
 *   <li>{@code BROKER} — relay {@code /topic} to an external STOMP broker (e.g. RabbitMQ).
 *       Every instance shares the same topic, so a dashboard connected to any instance sees
 *       events produced on all of them. This is what makes horizontal scaling possible.</li>
 * </ul>
 */
@ConfigurationProperties(prefix = "distribution.realtime")
public class RealtimeProperties {

    public enum Transport {
        /** In-memory broker; single instance. */
        SIMPLE,
        /** Relay to an external STOMP broker; scales horizontally. */
        BROKER
    }

    /** Which transport backs the live dashboard. Defaults to the in-memory broker. */
    private Transport transport = Transport.SIMPLE;

    /** Connection to the external STOMP broker, used only when {@code transport=BROKER}. */
    private Relay relay = new Relay();

    public Transport getTransport() {
        return transport;
    }

    public void setTransport(Transport transport) {
        this.transport = transport;
    }

    public Relay getRelay() {
        return relay;
    }

    public void setRelay(Relay relay) {
        this.relay = relay;
    }

    /** Where the STOMP broker relay connects. Defaults target a local RabbitMQ with the STOMP plugin. */
    public static class Relay {

        private String host = "localhost";
        private int port = 61613;
        private String login = "guest";
        private String passcode = "guest";
        /**
         * STOMP {@code host} header sent on CONNECT, which the broker reads as the virtual host.
         * Defaults to RabbitMQ's default vhost {@code /}. If left unset, the relay would send the
         * relay host name as the vhost and RabbitMQ would reject the connection.
         */
        private String virtualHost = "/";

        public String getHost() {
            return host;
        }

        public void setHost(String host) {
            this.host = host;
        }

        public String getVirtualHost() {
            return virtualHost;
        }

        public void setVirtualHost(String virtualHost) {
            this.virtualHost = virtualHost;
        }

        public int getPort() {
            return port;
        }

        public void setPort(int port) {
            this.port = port;
        }

        public String getLogin() {
            return login;
        }

        public void setLogin(String login) {
            this.login = login;
        }

        public String getPasscode() {
            return passcode;
        }

        public void setPasscode(String passcode) {
            this.passcode = passcode;
        }
    }
}
