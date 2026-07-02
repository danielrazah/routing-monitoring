package com.flowpay.routing.monitoring.distribution.application;

import com.flowpay.routing.monitoring.distribution.domain.exception.QueueAdvanceNotPossibleException;
import com.flowpay.routing.monitoring.distribution.domain.model.Interaction;
import com.flowpay.routing.monitoring.distribution.domain.model.InteractionState;
import com.flowpay.routing.monitoring.distribution.domain.model.Subject;
import com.flowpay.routing.monitoring.distribution.domain.port.in.EndInteraction;
import com.flowpay.routing.monitoring.distribution.domain.port.out.InteractionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdvanceQueueServiceTest {

    private static final UUID TEAM = UUID.randomUUID();

    @Mock InteractionRepository interactions;
    @Mock EndInteraction endInteraction;

    private AdvanceQueueService service;

    @BeforeEach
    void setUp() {
        service = new AdvanceQueueService(interactions, endInteraction);
    }

    @Test
    void advancesByEndingTheOldestOngoingInteraction() {
        UUID oldestId = UUID.randomUUID();
        Interaction oldest = new Interaction(oldestId, "Customer", Subject.OTHER,
                InteractionState.IN_SERVICE, UUID.randomUUID(), Instant.now());
        when(interactions.findOldestInServiceByTeam(TEAM)).thenReturn(Optional.of(oldest));

        service.handle(TEAM);

        verify(endInteraction).handle(oldestId);
    }

    @Test
    void failsWhenThereIsNothingToFree() {
        when(interactions.findOldestInServiceByTeam(TEAM)).thenReturn(Optional.empty());

        assertThrows(QueueAdvanceNotPossibleException.class, () -> service.handle(TEAM));
        verify(endInteraction, never()).handle(org.mockito.ArgumentMatchers.any());
    }
}
