package com.flowpay.routing.monitoring.distribution.domain.model;

/**
 * What the customer needs help with. The subject is what decides which team
 * gets the interaction. OTHER is the catch-all for anything we don't route
 * to a specialized team yet.
 */
public enum Subject {
    CARD_ISSUE,
    LOAN_CONTRACTING,
    OTHER
}
