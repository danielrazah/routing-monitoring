package com.flowpay.routing.monitoring.distribution.infrastructure.persistence;

import com.flowpay.routing.monitoring.distribution.domain.model.InteractionState;
import com.flowpay.routing.monitoring.distribution.domain.model.Subject;
import com.flowpay.routing.monitoring.distribution.domain.port.out.WaitingQueuePort;
import com.flowpay.routing.monitoring.distribution.infrastructure.persistence.adapter.WaitingQueueAdapter;
import com.flowpay.routing.monitoring.distribution.infrastructure.persistence.entity.InteractionJpaEntity;
import com.flowpay.routing.monitoring.distribution.infrastructure.persistence.entity.QueueItemJpaEntity;
import com.flowpay.routing.monitoring.distribution.infrastructure.persistence.repository.InteractionJpaRepository;
import com.flowpay.routing.monitoring.distribution.infrastructure.persistence.repository.QueueItemJpaRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Exercises the real waiting-line query against a real Postgres. The point is to prove
 * FIFO order and that a consumed item disappears — the FOR UPDATE SKIP LOCKED behaviour
 * only exists on a real database, which is why we spin one up with Testcontainers.
 *
 * We boot only the persistence slice (JPA + Flyway + the queue adapter) via a small
 * config, so the test stays focused and doesn't need the rest of the application wired.
 */
@SpringBootTest(classes = WaitingQueuePersistenceIT.PersistenceTestConfig.class)
@Testcontainers
class WaitingQueuePersistenceIT {

    @Configuration
    @EnableAutoConfiguration
    @EntityScan(basePackageClasses = InteractionJpaEntity.class)
    @EnableJpaRepositories(basePackageClasses = QueueItemJpaRepository.class)
    @Import(WaitingQueueAdapter.class)
    static class PersistenceTestConfig {
    }

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @DynamicPropertySource
    static void datasource(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    // The Cards team is seeded by the Flyway migration.
    private static final UUID CARDS_TEAM = UUID.fromString("11111111-1111-1111-1111-111111111111");

    @Autowired
    WaitingQueuePort queue;

    @Autowired
    InteractionJpaRepository interactions;

    @Autowired
    QueueItemJpaRepository queueItems;

    @Test
    void drainsTheLineOldestFirstAndRemovesConsumedItems() {
        Instant now = Instant.now();
        UUID first = enqueueAt("Older customer", now.minus(2, ChronoUnit.MINUTES));
        UUID second = enqueueAt("Newer customer", now.minus(1, ChronoUnit.MINUTES));

        // Whoever waited longest comes out first.
        assertThat(queue.pollNext(CARDS_TEAM)).contains(first);
        assertThat(queue.pollNext(CARDS_TEAM)).contains(second);
        // Line is empty now.
        assertThat(queue.pollNext(CARDS_TEAM)).isEmpty();
    }

    /** Insert a waiting interaction and its queue row with a controlled enqueue time. */
    private UUID enqueueAt(String customerName, Instant enqueuedAt) {
        UUID interactionId = UUID.randomUUID();
        interactions.saveAndFlush(new InteractionJpaEntity(
                interactionId, customerName, Subject.CARD_ISSUE.name(),
                InteractionState.WAITING.name(), null, Instant.now()));
        queueItems.saveAndFlush(new QueueItemJpaEntity(
                UUID.randomUUID(), CARDS_TEAM, interactionId, enqueuedAt));
        return interactionId;
    }
}
