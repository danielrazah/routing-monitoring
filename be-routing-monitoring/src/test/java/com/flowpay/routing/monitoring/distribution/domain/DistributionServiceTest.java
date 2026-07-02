package com.flowpay.routing.monitoring.distribution.domain;

import com.flowpay.routing.monitoring.distribution.domain.exception.AgentAtCapacityException;
import com.flowpay.routing.monitoring.distribution.domain.model.Agent;
import com.flowpay.routing.monitoring.distribution.domain.model.Interaction;
import com.flowpay.routing.monitoring.distribution.domain.model.Subject;
import com.flowpay.routing.monitoring.distribution.domain.model.WaitingQueue;
import com.flowpay.routing.monitoring.distribution.domain.service.DistributionResult;
import com.flowpay.routing.monitoring.distribution.domain.service.DistributionService;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pure unit test (no Spring, no database) for the two rules that matter most:
 * an agent serves at most three people at once, and extra people wait in line.
 */
class DistributionServiceTest {

    private static final UUID TEAM_ID = UUID.randomUUID();

    private final DistributionService distribution = new DistributionService();

    private Interaction newInteraction() {
        return new Interaction(UUID.randomUUID(), "Customer", Subject.CARD_ISSUE);
    }

    @Test
    void assignsThreeToTheSameAgentThenQueuesTheFourth() {
        Agent agent = new Agent(UUID.randomUUID(), "Solo", TEAM_ID);
        WaitingQueue queue = new WaitingQueue(TEAM_ID);

        // First three go straight to the agent.
        for (int i = 0; i < Agent.DEFAULT_MAX_CONCURRENT; i++) {
            DistributionResult result = distribution.distribute(newInteraction(), List.of(agent));
            assertInstanceOf(DistributionResult.Assigned.class, result);
        }
        assertEquals(3, agent.currentLoad());
        assertFalse(agent.hasFreeSlot());

        // The fourth finds everyone busy and must wait in line.
        Interaction fourth = newInteraction();
        DistributionResult result = distribution.distribute(fourth, List.of(agent));
        assertInstanceOf(DistributionResult.Queued.class, result);

        queue.enqueue(fourth); // this is what the application layer persists
        assertEquals(1, queue.size());
    }

    @Test
    void freeingASlotLetsTheAgentTakeTheNextInLine() {
        Agent agent = new Agent(UUID.randomUUID(), "Solo", TEAM_ID);
        WaitingQueue queue = new WaitingQueue(TEAM_ID);

        Interaction first = newInteraction();
        distribution.distribute(first, List.of(agent));
        distribution.distribute(newInteraction(), List.of(agent));
        distribution.distribute(newInteraction(), List.of(agent));

        Interaction waiting = newInteraction();
        assertInstanceOf(DistributionResult.Queued.class, distribution.distribute(waiting, List.of(agent)));
        queue.enqueue(waiting);

        // The agent closes one case, which opens a slot...
        agent.release(first.id());
        assertTrue(agent.hasFreeSlot());

        // ...and the next person in line is served right away.
        Interaction next = queue.dequeue().orElseThrow();
        assertInstanceOf(DistributionResult.Assigned.class, distribution.distribute(next, List.of(agent)));
        assertEquals(3, agent.currentLoad());
        assertTrue(queue.isEmpty());
    }

    @Test
    void spreadsWorkToTheLeastLoadedAgent() {
        Agent busy = new Agent(UUID.randomUUID(), "Busy", TEAM_ID);
        Agent idle = new Agent(UUID.randomUUID(), "Idle", TEAM_ID);
        busy.assign(newInteraction()); // busy already has one case

        DistributionResult result = distribution.distribute(newInteraction(), List.of(busy, idle));

        Agent chosen = assertInstanceOf(DistributionResult.Assigned.class, result).agent();
        assertEquals(idle.id(), chosen.id());
    }

    @Test
    void agentRefusesAFourthAssignmentDirectly() {
        Agent agent = new Agent(UUID.randomUUID(), "Solo", TEAM_ID);
        agent.assign(newInteraction());
        agent.assign(newInteraction());
        agent.assign(newInteraction());

        assertThrows(AgentAtCapacityException.class, () -> agent.assign(newInteraction()));
    }
}
