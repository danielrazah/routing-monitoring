package com.flowpay.routing.monitoring.distribution.infrastructure.web.dto;

import java.util.UUID;

/** One of the agent's open conversations: an in-service interaction and its customer. */
public record ConversationSummary(UUID interactionId, String customerName) {
}
