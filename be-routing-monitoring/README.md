# routing-monitoring

Contact-center routing service for **FlowPay**. It distributes incoming customer
interactions across specialized teams and shows what's happening on a live dashboard.

## The problem, in one paragraph

Customers reach out with a subject. Card problems go to the **Cards** team, loan requests
to **Loans**, and everything else to **Others**. Each agent handles at most **3**
interactions at once. If a whole team is busy, the customer waits in that team's line and
is served the moment a slot frees up. The design leaves room to add new teams without
touching the existing ones.

## Running it

Everything comes up with one command:

```bash
docker compose up --build
```

- Dashboard: http://localhost:8090
- API: http://localhost:8080
- Postgres: localhost:5432 (`routing` / `routing`)

Try it: open the dashboard, create a few contacts with the same subject, and watch them
fill agents (max 3 each) and then queue. End one and the next in line is picked up live.

### API

| Method | Path                          | Role  | What it does                          |
|--------|-------------------------------|-------|---------------------------------------|
| POST   | `/api/interactions`           | ADMIN | Open a contact (`customerName`, `subject`) |
| POST   | `/api/interactions/{id}/end`  | ADMIN | Close a contact and pull the next in line |
| POST   | `/api/teams/{teamId}/advance-queue` | ADMIN/AGENT | Free a slot so the next in line is served |
| GET    | `/api/dashboard`              | ADMIN/AGENT | Current snapshot of teams/agents/queues |
| GET    | `/api/agent/conversations`    | AGENT | The customers this agent is serving now |
| POST   | `/api/agent/conversations/{id}/end` | AGENT | Agent ends one of **its own** conversations |
| GET/POST | `/api/interactions/{id}/messages` | AGENT | Read/reply on an owned conversation |
| GET    | `/api/admin/conversations`    | ADMIN | Every live customer↔agent conversation |
| POST   | `/api/admin/reset`            | ADMIN | End all interactions and clear all queues |
| POST   | `/api/public/interactions`    | —     | Customer joins the queue (no login) |
| GET    | `/api/public/interactions/{id}` | —   | Customer status + who is serving them |
| POST   | `/api/public/interactions/{id}/end` | — | Customer ends its own conversation |
| GET/POST | `/api/public/interactions/{id}/messages` | — | Customer chat thread |
| WS     | `/ws` → `/topic/dashboard`, `/topic/chat.{id}` | — | Live distribution + chat events (STOMP) |

`subject` is one of `CARD_ISSUE`, `LOAN_CONTRACTING`, `OTHER`. Every response carries an
`X-Trace-Id` header (see **Traceability** below).

Interactive API docs (Scalar): http://localhost:8080/scalar
(OpenAPI spec at `/v3/api-docs`).

## Architecture

Pragmatic Clean Architecture: **strict isolation only in the business core**, plain Spring
everywhere else. Everything lives in one `distribution` module built as Ports & Adapters.

```
distribution/
├── domain/            pure Java — no Spring, no JPA
│   ├── model/         Interaction, Agent, Team, Subject, WaitingQueue, InteractionState
│   ├── event/         domain events (sealed DomainEvent)
│   ├── routing/       RoutingStrategy + SubjectRouter (Strategy pattern)
│   ├── service/       DistributionService (assign-or-queue)
│   └── port/in|out/   use-case and repository/gateway interfaces
├── application/       use-case implementations, @Transactional
└── infrastructure/    Spring, straight up
    ├── web/           controllers, DTO records, RFC 9457 Problem Details
    ├── persistence/   JPA entities (separate from domain) + mappers + adapters
    ├── websocket/     STOMP config + event listener
    └── config/        bean wiring
```

### Design decisions worth calling out

- **The rules live in the model.** `Agent` enforces the max-3 limit inside `assign`, and
  `Interaction` guards its own `WAITING → IN_SERVICE → ENDED` transitions. They can't be
  bypassed from the outside. `DistributionService` is pure logic: pick the least-loaded
  free agent, or say "queue it".

- **Strategy for routing (Open/Closed).** Each team is a `RoutingStrategy`. Onboarding a
  new team is a new strategy class plus one `@Bean` line — no existing code changes.

- **The queue is a Postgres table, not a message broker.** The waiting line is a
  *business* concept: ordered, persistent, per-team, auditable. We drain it with
  `SELECT ... FOR UPDATE SKIP LOCKED`, which gives safe concurrent consumption (two agents
  finishing at once never grab the same customer) without running RabbitMQ/ActiveMQ just
  for this. A broker would add infrastructure without solving the problem better.

- **Domain events decouple the edges.** The core publishes events through an
  `EventPublisher` **out-port**; a Spring adapter forwards them to the app event bus, and a
  listener turns them into STOMP messages **after the transaction commits** — so the
  dashboard only ever hears about changes that actually persisted. The domain has no idea
  WebSocket exists.

- **Why Spring is direct in the infrastructure.** Controllers, JPA repositories and the
  dashboard read-endpoint are plain Spring. The dashboard snapshot is a pure query with no
  business rules, so it reads the repositories directly instead of inventing a port.

- **Java 21 virtual threads** (`spring.threads.virtual.enabled=true`) so the blocking
  JDBC/queue calls scale cheaply.

## Traceability

Every request gets a correlation id. A `TraceIdFilter` (highest precedence) reuses an incoming
`X-Trace-Id` header or mints a short one, puts it on the SLF4J MDC as `traceId`, and echoes it back
on the response. The log pattern prints it (`%5p [%X{traceId:-}]`), so a single customer action —
create → route → assign/queue → chat → end — can be followed end to end across the logs, even under
concurrency. Business audit lines (interaction created/assigned/queued/ended, each chat message, the
admin reset) are logged at `INFO`. Actuator exposes `health`, `info` and `metrics` at `/actuator`.

## Testing

- **Pure unit test** (`DistributionServiceTest`) — no Spring, no DB — proves the max-3 rule,
  FIFO queueing and that freeing a slot pulls the next customer.
- **Testcontainers IT** (`WaitingQueuePersistenceIT`) — runs the real `SKIP LOCKED` query
  against a real Postgres.

```bash
./gradlew test   # requires JDK 21 and Docker (for the integration test)
```

Running the tests also writes a **JaCoCo coverage report** to
`build/reports/jacoco/test/html/index.html`.

## Notes on the repo

Work is split into small, focused branches with Conventional Commits (`feat/…`, `chore/…`,
`docs/…`), one concern per branch, each merged into `main` via pull request.
