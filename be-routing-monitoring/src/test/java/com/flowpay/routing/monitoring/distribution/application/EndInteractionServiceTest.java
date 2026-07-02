package com.flowpay.routing.monitoring.distribution.application;

import com.flowpay.routing.monitoring.distribution.domain.exception.AgentNotFoundException;
import com.flowpay.routing.monitoring.distribution.domain.exception.InteractionNotFoundException;
import com.flowpay.routing.monitoring.distribution.domain.model.Agent;
import com.flowpay.routing.monitoring.distribution.domain.model.Interaction;
import com.flowpay.routing.monitoring.distribution.domain.model.InteractionState;
import com.flowpay.routing.monitoring.distribution.domain.model.Subject;
import com.flowpay.routing.monitoring.distribution.domain.port.out.AgentRepository;
import com.flowpay.routing.monitoring.distribution.domain.port.out.EventPublisher;
import com.flowpay.routing.monitoring.distribution.domain.port.out.InteractionRepository;
import com.flowpay.routing.monitoring.distribution.domain.port.out.WaitingQueuePort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EndInteractionServiceTest {

    private static final UUID TEAM = UUID.randomUUID();

    @Mock InteractionRepository interactions;
    @Mock AgentRepository agents;
    @Mock WaitingQueuePort queue;
    @Mock EventPublisher events;

    private EndInteractionService service;

    @BeforeEach
    void setUp() {
        service = new EndInteractionService(interactions, agents, queue, events);
        lenient().when(interactions.save(any())).thenAnswer(inv -> inv.getArgument(0));
    }

    private Interaction inService(UUID id, UUID agentId) {
        return new Interaction(id, "Customer", Subject.OTHER, InteractionState.IN_SERVICE, agentId, Instant.now());
    }

    @Test
    void endsTheCaseAndPullsTheNextInLine() {
        UUID currentId = UUID.randomUUID();
        UUID agentId = UUID.randomUUID();
        Interaction current = inService(currentId, agentId);
        Agent agent = new Agent(agentId, "Ana", TEAM, 3, Set.of(currentId));

        UUID nextId = UUID.randomUUID();
        Interaction next = new Interaction(nextId, "Next", Subject.OTHER);

        when(interactions.findById(currentId)).thenReturn(Optional.of(current));
        when(agents.findById(agentId)).thenReturn(Optional.of(agent));
        when(queue.pollNext(TEAM)).thenReturn(Optional.of(nextId));
        when(interactions.findById(nextId)).thenReturn(Optional.of(next));

        service.handle(currentId);

        assertEquals(InteractionState.ENDED, current.state());
        assertEquals(InteractionState.IN_SERVICE, next.state()); // freed slot pulled the next customer
        assertEquals(agentId, next.assignedAgentId());
    }

    @Test
    void endsTheCaseWhenTheQueueIsEmpty() {
        UUID currentId = UUID.randomUUID();
        UUID agentId = UUID.randomUUID();
        Interaction current = inService(currentId, agentId);
        Agent agent = new Agent(agentId, "Ana", TEAM, 3, Set.of(currentId));

        when(interactions.findById(currentId)).thenReturn(Optional.of(current));
        when(agents.findById(agentId)).thenReturn(Optional.of(agent));
        when(queue.pollNext(TEAM)).thenReturn(Optional.empty());

        service.handle(currentId);

        assertEquals(InteractionState.ENDED, current.state());
    }

    @Test
    void failsWhenTheInteractionIsNotFound() {
        UUID id = UUID.randomUUID();
        when(interactions.findById(id)).thenReturn(Optional.empty());
        assertThrows(InteractionNotFoundException.class, () -> service.handle(id));
    }

    @Test
    void failsWhenTheAssignedAgentIsMissing() {
        UUID currentId = UUID.randomUUID();
        UUID agentId = UUID.randomUUID();
        when(interactions.findById(currentId)).thenReturn(Optional.of(inService(currentId, agentId)));
        when(agents.findById(agentId)).thenReturn(Optional.empty());

        assertThrows(AgentNotFoundException.class, () -> service.handle(currentId));
    }
}
