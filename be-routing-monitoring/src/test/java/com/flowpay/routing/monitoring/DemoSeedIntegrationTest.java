package com.flowpay.routing.monitoring;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Boots with the demo seed explicitly enabled and checks the dashboard opens with every
 * team already over capacity: all agents full and a non-empty queue. This also exercises the
 * DemoDataSeeder and the create/assign/queue path running at startup.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@TestPropertySource(properties = {
        "distribution.demo.seed-on-startup=true", // the app default is off; this test asserts the seeded state
        "distribution.realtime.transport=simple"}) // no external broker in tests
class DemoSeedIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @DynamicPropertySource
    static void datasource(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Value("${local.server.port}")
    int port;

    private final HttpClient http = HttpClient.newHttpClient();
    private final ObjectMapper json = new ObjectMapper();

    @Test
    void dashboardStartsWithEveryTeamOverCapacity() throws Exception {
        String loginBody = http.send(HttpRequest.newBuilder()
                        .uri(URI.create("http://localhost:" + port + "/api/auth/login"))
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(
                                "{\"username\":\"admin\",\"password\":\"admin123\"}"))
                        .build(), HttpResponse.BodyHandlers.ofString())
                .body();
        String token = json.readTree(loginBody).get("token").asText();

        HttpResponse<String> res = http.send(HttpRequest.newBuilder()
                        .uri(URI.create("http://localhost:" + port + "/api/dashboard"))
                        .header("Authorization", "Bearer " + token)
                        .GET().build(), HttpResponse.BodyHandlers.ofString());
        assertEquals(200, res.statusCode());

        JsonNode teams = json.readTree(res.body()).get("teams");
        assertEquals(3, teams.size());
        for (JsonNode team : teams) {
            assertTrue(team.get("waiting").asInt() > 0,
                    "team should start with a queue: " + team.get("name").asText());
            for (JsonNode agent : team.get("agents")) {
                assertEquals(agent.get("maxConcurrent").asInt(), agent.get("currentLoad").asInt(),
                        "agent should start at capacity: " + agent.get("name").asText());
            }
        }
    }
}
