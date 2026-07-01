package com.flowpay.routing.monitoring.distribution.infrastructure.web.dto;

import com.flowpay.routing.monitoring.distribution.domain.model.Subject;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/** Payload to open a new customer contact. */
public record CreateInteractionRequest(
        @NotBlank String customerName,
        @NotNull Subject subject) {
}
