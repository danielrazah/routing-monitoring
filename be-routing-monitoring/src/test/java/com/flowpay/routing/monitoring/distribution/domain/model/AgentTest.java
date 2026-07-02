package com.flowpay.routing.monitoring.distribution.domain.model;

import com.flowpay.routing.monitoring.distribution.domain.exception.AgentAtCapacityException;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AgentTest {

    private static final UUID TEAM = UUID.randomUUID();

    private Interaction newInteraction() {
        return new Interaction(UUID.randomUUID(), "Customer", Subject.OTHER);
    }

    @Test
    void defaultsToThreeConcurrent() {
        Agent agent = new Agent(UUID.randomUUID(), "Ana", TEAM);
        assertEquals(Agent.DEFAULT_MAX_CONCURRENT, agent.maxConcurrent());
        assertEquals(3, agent.maxConcurrent());
    }

    @Test
    void honoursAConfiguredLimit() {
        Agent agent = new Agent(UUID.randomUUID(), "Ana", TEAM, 1, Set.of());
        agent.assign(newInteraction());
        assertFalse(agent.hasFreeSlot());
        assertThrows(AgentAtCapacityException.class, () -> agent.assign(newInteraction()));
    }

    @Test
    void assignMovesTheInteractionIntoService() {
        Agent agent = new Agent(UUID.randomUUID(), "Ana", TEAM);
        Interaction interaction = newInteraction();
        agent.assign(interaction);
        assertEquals(InteractionState.IN_SERVICE, interaction.state());
        assertEquals(agent.id(), interaction.assignedAgentId());
        assertEquals(1, agent.currentLoad());
    }

    @Test
    void releaseFreesASlot() {
        Agent agent = new Agent(UUID.randomUUID(), "Ana", TEAM, 1, Set.of());
        Interaction interaction = newInteraction();
        agent.assign(interaction);
        assertFalse(agent.hasFreeSlot());

        agent.release(interaction.id());
        assertTrue(agent.hasFreeSlot());
        assertEquals(0, agent.currentLoad());
    }

    @Test
    void rejectsALimitBelowOne() {
        assertThrows(IllegalArgumentException.class,
                () -> new Agent(UUID.randomUUID(), "Ana", TEAM, 0, Set.of()));
    }
}
