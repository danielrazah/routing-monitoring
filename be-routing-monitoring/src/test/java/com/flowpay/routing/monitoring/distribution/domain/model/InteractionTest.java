package com.flowpay.routing.monitoring.distribution.domain.model;

import com.flowpay.routing.monitoring.distribution.domain.exception.IllegalInteractionStateException;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class InteractionTest {

    private Interaction newInteraction() {
        return new Interaction(UUID.randomUUID(), "Customer", Subject.CARD_ISSUE);
    }

    @Test
    void startsOutWaiting() {
        Interaction interaction = newInteraction();
        assertEquals(InteractionState.WAITING, interaction.state());
        assertEquals(null, interaction.assignedAgentId());
    }

    @Test
    void followsTheLifecycleWaitingInServiceEnded() {
        Interaction interaction = newInteraction();
        UUID agentId = UUID.randomUUID();

        interaction.startWith(agentId); // package-private: exercised the same way Agent.assign does
        assertEquals(InteractionState.IN_SERVICE, interaction.state());
        assertEquals(agentId, interaction.assignedAgentId());

        interaction.end();
        assertEquals(InteractionState.ENDED, interaction.state());
    }

    @Test
    void cannotEndWhileStillWaiting() {
        Interaction interaction = newInteraction();
        assertThrows(IllegalInteractionStateException.class, interaction::end);
    }

    @Test
    void cannotEndTwice() {
        Interaction interaction = newInteraction();
        interaction.startWith(UUID.randomUUID());
        interaction.end();
        assertThrows(IllegalInteractionStateException.class, interaction::end);
    }

    @Test
    void rejectsABlankCustomerName() {
        assertThrows(IllegalArgumentException.class,
                () -> new Interaction(UUID.randomUUID(), "  ", Subject.OTHER));
    }
}
