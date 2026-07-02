package com.flowpay.routing.monitoring.distribution.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/** Tunable distribution rules, bound from the {@code distribution.*} properties. */
@ConfigurationProperties(prefix = "distribution")
public class DistributionProperties {

    /** How many customers a single agent can serve at the same time. */
    private int maxConcurrentPerAgent = 3;

    public int getMaxConcurrentPerAgent() {
        return maxConcurrentPerAgent;
    }

    public void setMaxConcurrentPerAgent(int maxConcurrentPerAgent) {
        this.maxConcurrentPerAgent = maxConcurrentPerAgent;
    }
}
