package com.flowpay.routing.monitoring.distribution.infrastructure.web;

import com.flowpay.routing.monitoring.distribution.domain.port.in.AdvanceQueue;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@Tag(name = "Teams", description = "Operations on teams and their queues")
@RestController
@RequestMapping("/api/teams")
public class TeamController {

    private final AdvanceQueue advanceQueue;

    public TeamController(AdvanceQueue advanceQueue) {
        this.advanceQueue = advanceQueue;
    }

    /** Free one slot on the team so the next customer in line is served. */
    @Operation(summary = "Advance a team's queue",
            description = "Frees one slot (ends the oldest ongoing interaction) so the next in line is served. "
                    + "ADMIN may advance any team; an AGENT only its own.")
    @PostMapping("/{teamId}/advance-queue")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void advanceQueue(@PathVariable UUID teamId, @AuthenticationPrincipal Jwt jwt) {
        // AGENT tokens carry a teamId; they may only advance their own team's queue.
        // ADMIN has no such claim and may advance any team.
        String scopedTeam = jwt != null ? jwt.getClaimAsString("teamId") : null;
        if (scopedTeam != null && !scopedTeam.equals(teamId.toString())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "An agent can only advance its own team's queue");
        }
        advanceQueue.handle(teamId);
    }
}
