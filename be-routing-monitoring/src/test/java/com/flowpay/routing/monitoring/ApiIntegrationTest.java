package com.flowpay.routing.monitoring;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Drives the whole HTTP surface against a real server and a real Postgres: auth, roles,
 * the interaction lifecycle, the queue and the error handling. This covers the controllers,
 * security, the exception handler, the application services and the persistence adapters
 * end to end.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
class ApiIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @DynamicPropertySource
    static void datasource(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    private static final UUID LOANS = UUID.fromString("22222222-2222-2222-2222-222222222222");

    @Value("${local.server.port}")
    int port;

    private final HttpClient http = HttpClient.newBuilder().followRedirects(HttpClient.Redirect.NORMAL).build();
    private final ObjectMapper json = new ObjectMapper();

    private record Resp(int status, String body) {
    }

    private Resp send(String method, String path, String token, String body) throws Exception {
        HttpRequest.Builder req = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + port + path))
                .method(method, body == null
                        ? HttpRequest.BodyPublishers.noBody()
                        : HttpRequest.BodyPublishers.ofString(body));
        if (body != null) {
            req.header("Content-Type", "application/json");
        }
        if (token != null) {
            req.header("Authorization", "Bearer " + token);
        }
        HttpResponse<String> res = http.send(req.build(), HttpResponse.BodyHandlers.ofString());
        return new Resp(res.statusCode(), res.body());
    }

    private String login(String user, String pass) throws Exception {
        Resp res = send("POST", "/api/auth/login", null,
                "{\"username\":\"%s\",\"password\":\"%s\"}".formatted(user, pass));
        assertEquals(200, res.status());
        return json.readTree(res.body()).get("token").asText();
    }

    @Test
    void docsAndSpecAreOpen() throws Exception {
        assertEquals(200, send("GET", "/v3/api-docs", null, null).status());
        assertEquals(200, send("GET", "/scalar", null, null).status());
    }

    @Test
    void dashboardRequiresAuthentication() throws Exception {
        assertEquals(401, send("GET", "/api/dashboard", null, null).status());
    }

    @Test
    void rejectsBadCredentialsAndUnknownUsers() throws Exception {
        assertEquals(401, send("POST", "/api/auth/login", null,
                "{\"username\":\"admin\",\"password\":\"wrong\"}").status());
        assertEquals(401, send("POST", "/api/auth/login", null,
                "{\"username\":\"ghost\",\"password\":\"x\"}").status());
    }

    @Test
    void adminRunsTheFullLifecycle() throws Exception {
        String token = login("admin", "admin123");

        assertEquals(200, send("GET", "/api/dashboard", token, null).status());

        // Create -> assigned to a free agent.
        Resp created = send("POST", "/api/interactions", token,
                "{\"customerName\":\"Maria\",\"subject\":\"CARD_ISSUE\"}");
        assertEquals(201, created.status());
        JsonNode node = json.readTree(created.body());
        assertEquals("IN_SERVICE", node.get("state").asText());
        String id = node.get("id").asText();

        // End it.
        assertEquals(204, send("POST", "/api/interactions/" + id + "/end", token, null).status());
    }

    @Test
    void advancingAQueueServesTheNextInLine() throws Exception {
        String token = login("admin", "admin123");

        // Loans has a single agent (3 slots); the 4th+ contacts queue up.
        for (int i = 0; i < 5; i++) {
            assertEquals(201, send("POST", "/api/interactions", token,
                    "{\"customerName\":\"L%d\",\"subject\":\"LOAN_CONTRACTING\"}".formatted(i)).status());
        }
        assertEquals(204, send("POST", "/api/teams/" + LOANS + "/advance-queue", token, null).status());
    }

    @Test
    void viewerCannotMutate() throws Exception {
        String token = login("viewer", "viewer123");
        assertEquals(200, send("GET", "/api/dashboard", token, null).status());
        assertEquals(403, send("POST", "/api/interactions", token,
                "{\"customerName\":\"Maria\",\"subject\":\"OTHER\"}").status());
    }

    @Test
    void rejectsInvalidInputWith400() throws Exception {
        String token = login("admin", "admin123");
        Resp res = send("POST", "/api/interactions", token,
                "{\"customerName\":\"\",\"subject\":\"OTHER\"}");
        assertEquals(400, res.status());
        assertTrue(res.body().contains("customerName"));
    }

    @Test
    void returns404WhenEndingAnUnknownInteraction() throws Exception {
        String token = login("admin", "admin123");
        assertEquals(404, send("POST", "/api/interactions/" + UUID.randomUUID() + "/end", token, null).status());
    }

    @Test
    void returns409WhenThereIsNothingToAdvance() throws Exception {
        String token = login("admin", "admin123");
        // Cards starts idle in this fresh container: nothing in service to free.
        assertEquals(409, send("POST",
                "/api/teams/11111111-1111-1111-1111-111111111111/advance-queue", token, null).status());
    }
}
