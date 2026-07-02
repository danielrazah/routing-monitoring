package com.flowpay.routing.monitoring.distribution.domain.model;

import java.util.EnumSet;
import java.util.Set;

/**
 * The life of an interaction: it waits in line, an agent takes it, and finally it ends.
 * We spell out which moves are allowed so a finished case can never be reopened and a
 * waiting one can never be closed without being served first.
 */
public enum InteractionState {

    WAITING {
        @Override
        Set<InteractionState> allowedNext() {
            return EnumSet.of(IN_SERVICE);
        }
    },

    IN_SERVICE {
        @Override
        Set<InteractionState> allowedNext() {
            return EnumSet.of(ENDED);
        }
    },

    ENDED {
        @Override
        Set<InteractionState> allowedNext() {
            return EnumSet.noneOf(InteractionState.class);
        }
    };

    abstract Set<InteractionState> allowedNext();

    boolean canTransitionTo(InteractionState next) {
        return allowedNext().contains(next);
    }
}
