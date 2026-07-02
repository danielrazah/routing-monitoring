package com.flowpay.routing.monitoring.distribution.infrastructure.security;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TokenServiceTest {

    // 40 bytes, comfortably above the 32-byte minimum for HS256.
    private static final SecretKey KEY =
            new SecretKeySpec("test-secret-test-secret-test-secret-1234".getBytes(StandardCharsets.UTF_8), "HmacSHA256");

    private final JwtEncoder encoder = new NimbusJwtEncoder(new ImmutableSecret<>(KEY));
    private final JwtDecoder decoder = NimbusJwtDecoder.withSecretKey(KEY).macAlgorithm(MacAlgorithm.HS256).build();
    private final TokenService tokenService = new TokenService(encoder, 3600);

    @Test
    void issuesATokenCarryingSubjectAndRoles() {
        String token = tokenService.issue("admin", List.of("ADMIN"), null);

        Jwt jwt = decoder.decode(token); // also proves the signature validates
        assertEquals("admin", jwt.getSubject());
        assertEquals(List.of("ADMIN"), jwt.getClaimAsStringList("roles"));
        assertEquals("routing-monitoring", jwt.getClaimAsString("iss"));
        assertNotNull(jwt.getExpiresAt());
        assertTrue(jwt.getExpiresAt().isAfter(jwt.getIssuedAt()));
        assertNull(jwt.getClaimAsString("teamId")); // ADMIN is not scoped to a team
    }

    @Test
    void anAgentTokenCarriesItsTeamId() {
        String teamId = "22222222-2222-2222-2222-222222222222";
        String token = tokenService.issue("carla", List.of("AGENT"), teamId);

        Jwt jwt = decoder.decode(token);
        assertEquals(List.of("AGENT"), jwt.getClaimAsStringList("roles"));
        assertEquals(teamId, jwt.getClaimAsString("teamId"));
    }

    @Test
    void exposesItsConfiguredTtl() {
        assertEquals(3600, tokenService.ttlSeconds());
    }
}
