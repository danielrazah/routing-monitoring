package com.flowpay.routing.monitoring.distribution.infrastructure.web.dto;

import com.flowpay.routing.monitoring.distribution.infrastructure.persistence.entity.MessageJpaEntity;

import java.time.Instant;
import java.util.UUID;

/** A chat message on the wire. {@code sender} is CUSTOMER or AGENT. */
public record MessageResponse(UUID id, UUID interactionId, String sender, String body, Instant createdAt) {

    public static MessageResponse from(MessageJpaEntity entity) {
        return new MessageResponse(entity.getId(), entity.getInteractionId(), entity.getSender(),
                entity.getBody(), entity.getCreatedAt());
    }
}
