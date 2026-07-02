package com.flowpay.routing.monitoring.distribution.domain.routing;

import com.flowpay.routing.monitoring.distribution.domain.exception.NoTeamForSubjectException;
import com.flowpay.routing.monitoring.distribution.domain.model.Subject;

import java.util.Comparator;
import java.util.List;

/**
 * Finds the team responsible for a subject by asking each strategy in priority order.
 * The first one that accepts wins; the catch-all guarantees there is always an answer.
 */
public class SubjectRouter {

    private final List<RoutingStrategy> strategies;

    public SubjectRouter(List<RoutingStrategy> strategies) {
        // Sort once so routing is a simple ordered scan afterwards.
        this.strategies = strategies.stream()
                .sorted(Comparator.comparingInt(RoutingStrategy::priority))
                .toList();
    }

    public String routeToTeam(Subject subject) {
        return strategies.stream()
                .filter(strategy -> strategy.handles(subject))
                .findFirst()
                .map(RoutingStrategy::teamName)
                .orElseThrow(() -> new NoTeamForSubjectException(subject));
    }
}
