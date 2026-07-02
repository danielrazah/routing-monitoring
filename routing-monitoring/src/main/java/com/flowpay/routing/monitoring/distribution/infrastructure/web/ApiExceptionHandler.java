package com.flowpay.routing.monitoring.distribution.infrastructure.web;

import com.flowpay.routing.monitoring.distribution.domain.exception.AgentAtCapacityException;
import com.flowpay.routing.monitoring.distribution.domain.exception.AgentNotFoundException;
import com.flowpay.routing.monitoring.distribution.domain.exception.IllegalInteractionStateException;
import com.flowpay.routing.monitoring.distribution.domain.exception.InteractionNotFoundException;
import com.flowpay.routing.monitoring.distribution.domain.exception.NoTeamForSubjectException;
import com.flowpay.routing.monitoring.distribution.domain.exception.QueueAdvanceNotPossibleException;
import com.flowpay.routing.monitoring.distribution.domain.exception.TeamNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

/**
 * Turns domain failures into RFC 9457 Problem Details, so clients always get a
 * structured error instead of a stack trace. The HTTP status reflects the reason:
 * missing thing -> 404, business rule broken -> 409, bad routing -> 422, bad input -> 400.
 */
@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler({InteractionNotFoundException.class, AgentNotFoundException.class, TeamNotFoundException.class})
    public ProblemDetail handleNotFound(RuntimeException ex) {
        return problem(HttpStatus.NOT_FOUND, "Not found", ex.getMessage());
    }

    @ExceptionHandler({AgentAtCapacityException.class, IllegalInteractionStateException.class,
            QueueAdvanceNotPossibleException.class})
    public ProblemDetail handleConflict(RuntimeException ex) {
        return problem(HttpStatus.CONFLICT, "Business rule violated", ex.getMessage());
    }

    @ExceptionHandler(NoTeamForSubjectException.class)
    public ProblemDetail handleUnprocessable(RuntimeException ex) {
        return problem(HttpStatus.UNPROCESSABLE_ENTITY, "Cannot route interaction", ex.getMessage());
    }

    @ExceptionHandler(AuthenticationException.class)
    public ProblemDetail handleAuthentication(AuthenticationException ex) {
        return problem(HttpStatus.UNAUTHORIZED, "Authentication failed", "Invalid username or password");
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidation(MethodArgumentNotValidException ex) {
        String detail = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + " " + error.getDefaultMessage())
                .collect(Collectors.joining("; "));
        return problem(HttpStatus.BAD_REQUEST, "Invalid request", detail);
    }

    private ProblemDetail problem(HttpStatus status, String title, String detail) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detail);
        problem.setTitle(title);
        return problem;
    }
}
