package com.flowpay.routing.monitoring.distribution.infrastructure.web;

import com.flowpay.routing.monitoring.distribution.domain.exception.InteractionNotFoundException;
import com.flowpay.routing.monitoring.distribution.domain.exception.NoTeamForSubjectException;
import com.flowpay.routing.monitoring.distribution.domain.exception.QueueAdvanceNotPossibleException;
import com.flowpay.routing.monitoring.distribution.domain.model.Subject;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.security.authentication.BadCredentialsException;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ApiExceptionHandlerTest {

    private final ApiExceptionHandler handler = new ApiExceptionHandler();

    @Test
    void mapsNotFoundTo404() {
        ProblemDetail problem = handler.handleNotFound(new InteractionNotFoundException(UUID.randomUUID()));
        assertEquals(HttpStatus.NOT_FOUND.value(), problem.getStatus());
    }

    @Test
    void mapsBusinessRuleViolationsTo409() {
        ProblemDetail problem = handler.handleConflict(new QueueAdvanceNotPossibleException(UUID.randomUUID()));
        assertEquals(HttpStatus.CONFLICT.value(), problem.getStatus());
    }

    @Test
    void mapsRoutingFailuresTo422() {
        ProblemDetail problem = handler.handleUnprocessable(new NoTeamForSubjectException(Subject.OTHER));
        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY.value(), problem.getStatus());
    }

    @Test
    void mapsAuthenticationFailuresTo401() {
        ProblemDetail problem = handler.handleAuthentication(new BadCredentialsException("bad"));
        assertEquals(HttpStatus.UNAUTHORIZED.value(), problem.getStatus());
    }
}
