package com.flowpay.routing.monitoring.distribution.infrastructure.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

/** Issues signed JWTs that carry the authenticated user's roles. */
@Service
public class TokenService {

    private final JwtEncoder encoder;
    private final long ttlSeconds;

    public TokenService(JwtEncoder encoder, @Value("${security.jwt.ttl-seconds}") long ttlSeconds) {
        this.encoder = encoder;
        this.ttlSeconds = ttlSeconds;
    }

    public String issue(String username, List<String> roles, String teamId) {
        Instant now = Instant.now();
        JwtClaimsSet.Builder claims = JwtClaimsSet.builder()
                .issuer("routing-monitoring")
                .issuedAt(now)
                .expiresAt(now.plusSeconds(ttlSeconds))
                .subject(username)
                .claim("roles", roles);
        // AGENT tokens carry the team they may see; the dashboard reads this to scope the view.
        if (teamId != null) {
            claims.claim("teamId", teamId);
        }
        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        return encoder.encode(JwtEncoderParameters.from(header, claims.build())).getTokenValue();
    }

    public long ttlSeconds() {
        return ttlSeconds;
    }
}
