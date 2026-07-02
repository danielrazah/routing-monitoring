package com.flowpay.routing.monitoring.distribution.domain.routing;

import com.flowpay.routing.monitoring.distribution.domain.exception.NoTeamForSubjectException;
import com.flowpay.routing.monitoring.distribution.domain.model.Subject;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SubjectRouterTest {

    // Deliberately out of priority order: the router must sort them itself.
    private final SubjectRouter router = new SubjectRouter(List.of(
            new OtherSubjectsRouting(),
            new LoanContractingRouting(),
            new CardIssuesRouting()));

    @Test
    void routesEachSubjectToItsTeam() {
        assertEquals("Cards", router.routeToTeam(Subject.CARD_ISSUE));
        assertEquals("Loans", router.routeToTeam(Subject.LOAN_CONTRACTING));
        assertEquals("Others", router.routeToTeam(Subject.OTHER));
    }

    @Test
    void theCatchAllOnlyWinsWhenNothingElseMatches() {
        // A specialized subject still goes to its team even though Others accepts everything.
        assertEquals("Cards", router.routeToTeam(Subject.CARD_ISSUE));
    }

    @Test
    void throwsWhenNoStrategyHandlesTheSubject() {
        SubjectRouter cardsOnly = new SubjectRouter(List.of(new CardIssuesRouting()));
        assertThrows(NoTeamForSubjectException.class, () -> cardsOnly.routeToTeam(Subject.OTHER));
    }
}
