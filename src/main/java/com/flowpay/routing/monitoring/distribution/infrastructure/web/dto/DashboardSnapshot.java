package com.flowpay.routing.monitoring.distribution.infrastructure.web.dto;

import java.util.List;
import java.util.UUID;

/**
 * A read-only picture of the whole operation, so a dashboard can render current state
 * on load (live updates then arrive over WebSocket). This is a plain query with no
 * business rules, so it's built straight from the JPA repositories.
 */
public record DashboardSnapshot(List<TeamSnapshot> teams) {

    public record TeamSnapshot(UUID id, String name, long waiting, List<AgentSnapshot> agents) {
    }

    public record AgentSnapshot(UUID id, String name, long currentLoad, int maxConcurrent) {
    }
}
