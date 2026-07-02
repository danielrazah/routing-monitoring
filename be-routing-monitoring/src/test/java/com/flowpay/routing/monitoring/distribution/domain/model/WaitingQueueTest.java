package com.flowpay.routing.monitoring.distribution.domain.model;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WaitingQueueTest {

    private Interaction newInteraction(String name) {
        return new Interaction(UUID.randomUUID(), name, Subject.OTHER);
    }

    @Test
    void servesInFirstInFirstOutOrder() {
        WaitingQueue queue = new WaitingQueue(UUID.randomUUID());
        Interaction first = newInteraction("First");
        Interaction second = newInteraction("Second");
        queue.enqueue(first);
        queue.enqueue(second);

        assertEquals(2, queue.size());
        assertEquals(first.id(), queue.dequeue().orElseThrow().id());
        assertEquals(second.id(), queue.dequeue().orElseThrow().id());
        assertTrue(queue.isEmpty());
    }

    @Test
    void dequeueOnAnEmptyQueueReturnsEmpty() {
        WaitingQueue queue = new WaitingQueue(UUID.randomUUID());
        assertTrue(queue.dequeue().isEmpty());
        assertFalse(queue.dequeue().isPresent());
    }

    @Test
    void remembersItsTeam() {
        UUID team = UUID.randomUUID();
        assertEquals(team, new WaitingQueue(team).teamId());
    }
}
