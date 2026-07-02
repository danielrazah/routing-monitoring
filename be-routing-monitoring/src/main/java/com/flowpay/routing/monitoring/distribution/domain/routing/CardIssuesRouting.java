package com.flowpay.routing.monitoring.distribution.domain.routing;

import com.flowpay.routing.monitoring.distribution.domain.model.Subject;

/** Card problems go to the Cards team. */
public class CardIssuesRouting implements RoutingStrategy {

    @Override
    public boolean handles(Subject subject) {
        return subject == Subject.CARD_ISSUE;
    }

    @Override
    public String teamName() {
        return "Cards";
    }

    @Override
    public int priority() {
        return 10;
    }
}
