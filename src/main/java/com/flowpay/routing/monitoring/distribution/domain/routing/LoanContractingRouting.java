package com.flowpay.routing.monitoring.distribution.domain.routing;

import com.flowpay.routing.monitoring.distribution.domain.model.Subject;

/** Loan requests go to the Loans team. */
public class LoanContractingRouting implements RoutingStrategy {

    @Override
    public boolean handles(Subject subject) {
        return subject == Subject.LOAN_CONTRACTING;
    }

    @Override
    public String teamName() {
        return "Loans";
    }

    @Override
    public int priority() {
        return 20;
    }
}
