package com.flowpay.routing.monitoring.distribution.infrastructure.security;

import com.flowpay.routing.monitoring.distribution.infrastructure.persistence.repository.AppUserJpaRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/** Exchanges username/password for a JWT that carries the user's roles. */
@Tag(name = "Auth", description = "Login and token issuance")
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final TokenService tokenService;
    private final AppUserJpaRepository users;

    public AuthController(AuthenticationManager authenticationManager,
                          TokenService tokenService,
                          AppUserJpaRepository users) {
        this.authenticationManager = authenticationManager;
        this.tokenService = tokenService;
        this.users = users;
    }

    @Operation(summary = "Log in", description = "Returns a JWT to send as 'Authorization: Bearer <token>'.")
    @PostMapping("/login")
    public LoginResponse login(@RequestBody LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.username(), request.password()));

        // Keep only real roles (Spring Security 7 also adds factor authorities like FACTOR_PASSWORD).
        List<String> roles = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(a -> a.startsWith("ROLE_"))
                .map(a -> a.substring("ROLE_".length()))
                .toList();

        // An AGENT is tied to a team; carry it in the token so the dashboard scopes to it.
        UUID teamId = users.findByUsername(authentication.getName())
                .map(user -> user.getTeamId())
                .orElse(null);
        String teamIdClaim = teamId != null ? teamId.toString() : null;

        String token = tokenService.issue(authentication.getName(), roles, teamIdClaim);
        return new LoginResponse(token, authentication.getName(), roles, teamIdClaim, tokenService.ttlSeconds());
    }

    public record LoginRequest(@NotBlank String username, @NotBlank String password) {
    }

    public record LoginResponse(String token, String username, List<String> roles, String teamId,
                                long expiresInSeconds) {
    }
}
