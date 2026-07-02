package com.flowpay.routing.monitoring.distribution.infrastructure.config;

import com.flowpay.routing.monitoring.distribution.domain.model.Subject;
import com.flowpay.routing.monitoring.distribution.domain.port.in.CreateInteraction;
import com.flowpay.routing.monitoring.distribution.domain.port.in.CreateInteraction.NewInteractionCommand;
import com.flowpay.routing.monitoring.distribution.infrastructure.persistence.repository.InteractionJpaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Seeds a "busy operation" on first startup so the dashboard opens with every team already
 * over capacity — all agents full and a few customers waiting in each queue. It just calls
 * the real {@link CreateInteraction} use case, so the seeded state follows the same routing
 * and max-3 rules as anything created later.
 *
 * <p>Idempotent: it does nothing if interactions already exist, so restarts (with a persisted
 * database) don't pile up. Disable entirely with {@code distribution.demo.seed-on-startup=false}
 * (the test suite does this so it can assert against a clean slate).
 */
@Component
@ConditionalOnProperty(name = "distribution.demo.seed-on-startup", havingValue = "true", matchIfMissing = true)
public class DemoDataSeeder implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DemoDataSeeder.class);

    private final CreateInteraction createInteraction;
    private final InteractionJpaRepository interactions;

    public DemoDataSeeder(CreateInteraction createInteraction, InteractionJpaRepository interactions) {
        this.createInteraction = createInteraction;
        this.interactions = interactions;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (interactions.count() > 0) {
            return; // already seeded (or in real use) — never pile up on restarts
        }
        // Capacities: Cards 2 agents (6 slots), Loans 1 (3), Others 1 (3). Seeding a couple
        // above capacity leaves each team full with a small queue.
        seed(Subject.CARD_ISSUE, 8);
        seed(Subject.LOAN_CONTRACTING, 5);
        seed(Subject.OTHER, 5);
        log.info("Demo data seeded: every team starts at capacity with a waiting queue.");
    }

    private void seed(Subject subject, int count) {
        for (int i = 1; i <= count; i++) {
            createInteraction.handle(new NewInteractionCommand("Cliente %d · %s".formatted(i, subject.name()), subject));
        }
    }
}
