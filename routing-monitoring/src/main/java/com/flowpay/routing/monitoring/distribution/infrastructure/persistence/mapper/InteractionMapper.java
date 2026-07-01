package com.flowpay.routing.monitoring.distribution.infrastructure.persistence.mapper;

import com.flowpay.routing.monitoring.distribution.domain.model.Interaction;
import com.flowpay.routing.monitoring.distribution.domain.model.InteractionState;
import com.flowpay.routing.monitoring.distribution.domain.model.Subject;
import com.flowpay.routing.monitoring.distribution.infrastructure.persistence.entity.InteractionJpaEntity;

/** Translates between the domain Interaction and its stored form. */
public final class InteractionMapper {

    private InteractionMapper() {
    }

    public static InteractionJpaEntity toEntity(Interaction interaction) {
        return new InteractionJpaEntity(
                interaction.id(),
                interaction.customerName(),
                interaction.subject().name(),
                interaction.state().name(),
                interaction.assignedAgentId(),
                interaction.createdAt());
    }

    public static Interaction toDomain(InteractionJpaEntity entity) {
        return new Interaction(
                entity.getId(),
                entity.getCustomerName(),
                Subject.valueOf(entity.getSubject()),
                InteractionState.valueOf(entity.getState()),
                entity.getAssignedAgentId(),
                entity.getCreatedAt());
    }
}
