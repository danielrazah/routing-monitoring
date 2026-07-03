package com.flowpay.routing.monitoring.distribution.infrastructure.web.dto;

import java.util.UUID;

/**
 * One live conversation as the ADMIN monitor sees it: which customer, which agent and which
 * team, so an admin can watch every ongoing customer↔agent dialog across all teams.
 */
public record AdminConversation(
        UUID interactionId,
        String customerName,
        UUID agentId,
        String agentName,
        UUID teamId,
        String teamName) {
}
