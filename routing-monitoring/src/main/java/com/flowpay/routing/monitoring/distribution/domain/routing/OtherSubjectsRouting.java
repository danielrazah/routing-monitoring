package com.flowpay.routing.monitoring.distribution.domain.routing;

import com.flowpay.routing.monitoring.distribution.domain.model.Subject;

/**
 * The catch-all: everything that no specialized team claimed goes to the Others team.
 * Its high priority number means it is only chosen when nothing else matches.
 */
public class OtherSubjectsRouting implements RoutingStrategy {

    @Override
    public boolean handles(Subject subject) {
        return true;
    }

    @Override
    public String teamName() {
        return "Others";
    }

    @Override
    public int priority() {
        return Integer.MAX_VALUE;
    }
}
