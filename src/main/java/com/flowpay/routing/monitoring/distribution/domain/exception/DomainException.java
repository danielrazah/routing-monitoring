package com.flowpay.routing.monitoring.distribution.domain.exception;

/** Base type for anything the business rules refuse to do. */
public abstract class DomainException extends RuntimeException {
    protected DomainException(String message) {
        super(message);
    }
}
