package com.flowpay.routing.monitoring.distribution.infrastructure.persistence.mapper;

import com.flowpay.routing.monitoring.distribution.domain.model.Interaction;
import com.flowpay.routing.monitoring.distribution.domain.model.InteractionState;
import com.flowpay.routing.monitoring.distribution.domain.model.Subject;
import com.flowpay.routing.monitoring.distribution.infrastructure.persistence.entity.InteractionJpaEntity;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class InteractionMapperTest {

    @Test
    void mapsDomainToEntityAndBack() {
        UUID id = UUID.randomUUID();
        UUID agentId = UUID.randomUUID();
        Instant createdAt = Instant.parse("2026-01-01T10:15:30Z");
        Interaction domain = new Interaction(id, "Maria", Subject.LOAN_CONTRACTING,
                InteractionState.IN_SERVICE, agentId, createdAt);

        InteractionJpaEntity entity = InteractionMapper.toEntity(domain);
        assertEquals(id, entity.getId());
        assertEquals("Maria", entity.getCustomerName());
        assertEquals("LOAN_CONTRACTING", entity.getSubject());
        assertEquals("IN_SERVICE", entity.getState());
        assertEquals(agentId, entity.getAssignedAgentId());
        assertEquals(createdAt, entity.getCreatedAt());

        Interaction back = InteractionMapper.toDomain(entity);
        assertEquals(id, back.id());
        assertEquals("Maria", back.customerName());
        assertEquals(Subject.LOAN_CONTRACTING, back.subject());
        assertEquals(InteractionState.IN_SERVICE, back.state());
        assertEquals(agentId, back.assignedAgentId());
        assertEquals(createdAt, back.createdAt());
    }
}
