package com.flowpay.routing.monitoring.distribution.application;

import com.flowpay.routing.monitoring.distribution.domain.exception.TeamNotFoundException;
import com.flowpay.routing.monitoring.distribution.domain.model.Agent;
import com.flowpay.routing.monitoring.distribution.domain.model.InteractionState;
import com.flowpay.routing.monitoring.distribution.domain.model.Subject;
import com.flowpay.routing.monitoring.distribution.domain.model.Team;
import com.flowpay.routing.monitoring.distribution.domain.port.in.CreateInteraction.NewInteractionCommand;
import com.flowpay.routing.monitoring.distribution.domain.port.in.InteractionView;
import com.flowpay.routing.monitoring.distribution.domain.port.out.AgentRepository;
import com.flowpay.routing.monitoring.distribution.domain.port.out.EventPublisher;
import com.flowpay.routing.monitoring.distribution.domain.port.out.InteractionRepository;
import com.flowpay.routing.monitoring.distribution.domain.port.out.TeamRepository;
import com.flowpay.routing.monitoring.distribution.domain.port.out.WaitingQueuePort;
import com.flowpay.routing.monitoring.distribution.domain.routing.CardIssuesRouting;
import com.flowpay.routing.monitoring.distribution.domain.routing.OtherSubjectsRouting;
import com.flowpay.routing.monitoring.distribution.domain.routing.SubjectRouter;
import com.flowpay.routing.monitoring.distribution.domain.service.DistributionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreateInteractionServiceTest {

    private static final UUID CARDS_ID = UUID.randomUUID();

    private final SubjectRouter router = new SubjectRouter(List.of(new CardIssuesRouting(), new OtherSubjectsRouting()));
    private final DistributionService distribution = new DistributionService();

    @Mock TeamRepository teams;
    @Mock AgentRepository agents;
    @Mock InteractionRepository interactions;
    @Mock WaitingQueuePort queue;
    @Mock EventPublisher events;

    private CreateInteractionService service;

    @BeforeEach
    void setUp() {
        service = new CreateInteractionService(router, distribution, teams, agents, interactions, queue, events);
        lenient().when(interactions.save(any())).thenAnswer(inv -> inv.getArgument(0));
    }

    @Test
    void assignsToAFreeAgent() {
        when(teams.findByName("Cards")).thenReturn(Optional.of(new Team(CARDS_ID, "Cards")));
        Agent free = new Agent(UUID.randomUUID(), "Ana", CARDS_ID);
        when(agents.findByTeamName("Cards")).thenReturn(List.of(free));

        InteractionView view = service.handle(new NewInteractionCommand("Cliente", Subject.CARD_ISSUE));

        assertEquals(InteractionState.IN_SERVICE, view.state());
        assertEquals(free.id(), view.assignedAgentId());
        verify(queue, never()).enqueue(any(), any());
    }

    @Test
    void enqueuesWhenEveryAgentIsBusy() {
        when(teams.findByName("Cards")).thenReturn(Optional.of(new Team(CARDS_ID, "Cards")));
        Agent busy = new Agent(UUID.randomUUID(), "Ana", CARDS_ID, 1, Set.of(UUID.randomUUID()));
        when(agents.findByTeamName("Cards")).thenReturn(List.of(busy));

        InteractionView view = service.handle(new NewInteractionCommand("Cliente", Subject.CARD_ISSUE));

        assertEquals(InteractionState.WAITING, view.state());
        verify(queue).enqueue(eq(CARDS_ID), eq(view.id()));
    }

    @Test
    void failsWhenTheRoutedTeamDoesNotExist() {
        when(teams.findByName("Cards")).thenReturn(Optional.empty());
        assertThrows(TeamNotFoundException.class,
                () -> service.handle(new NewInteractionCommand("Cliente", Subject.CARD_ISSUE)));
    }
}
