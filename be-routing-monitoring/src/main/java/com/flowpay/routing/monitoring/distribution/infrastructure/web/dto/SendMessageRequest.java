package com.flowpay.routing.monitoring.distribution.infrastructure.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Payload to post a chat message. */
public record SendMessageRequest(@NotBlank @Size(max = 2000) String body) {
}
