package com.flowpay.routing.monitoring.distribution.domain.routing;

import com.flowpay.routing.monitoring.distribution.domain.model.Subject;

/**
 * Decides whether a subject belongs to a team. There is one strategy per team.
 *
 * This is the Open/Closed seam: to onboard a new team you add a new strategy and
 * leave every existing one untouched.
 */
public interface RoutingStrategy {

    /** Does this team handle the given subject? */
    boolean handles(Subject subject);

    /** The team that will receive the interaction (matches the team name stored in the DB). */
    String teamName();

    /** Lower runs first. The catch-all team uses the highest number so it only wins last. */
    int priority();
}
