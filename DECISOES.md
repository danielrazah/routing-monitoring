# Decisões de projeto

Sistema de distribuição de atendimentos entre times (Cartões, Empréstimos, Outros).
Cada atendente atende no máximo 3 simultâneos; ao lotar, o cliente entra numa fila e
é puxado assim que abre vaga.

## Stack

- **Java 21 (LTS)** — virtual threads para chamadas bloqueantes; `record`/`sealed`/pattern
  matching para domínio enxuto.
- **Gradle (Kotlin DSL)** — build tipado, incremental, padrão do Spring Initializr.
- **Spring Boot 4.1 / Spring Framework 7** — starters modulares, virtual threads nativas,
  Problem Details (RFC 9457).

## Idioma

Código/comentários em inglês. UI em pt/en conforme idioma do navegador.

## Arquitetura — Clean Architecture pragmática

Isolamento só no núcleo de negócio; resto usa Spring direto. Módulo `distribution`,
Ports & Adapters:

```
domain/          Java puro (sem Spring/JPA)
  model/         Interaction, Agent, Team, Subject, WaitingQueue, InteractionState
  event/         sealed DomainEvent
  routing/       RoutingStrategy + SubjectRouter (Strategy)
  service/       DistributionService (aloca ou enfileira)
  port/in|out/   casos de uso / repositórios-gateways
application/     casos de uso, @Transactional
infrastructure/  web, JPA, websocket, wiring
```

## Decisões principais

- **Invariantes no modelo.** `Agent.assign` garante limite de 3; `Interaction` controla
  `WAITING → IN_SERVICE → ENDED`. `DistributionService` é lógica pura (menos ocupado ou fila).
- **Strategy por time (Open/Closed).** Time novo = classe nova + `@Bean`.
- **Fila = tabela Postgres, não broker.** `SELECT ... FOR UPDATE SKIP LOCKED` garante
  consumo concorrente seguro sem RabbitMQ/ActiveMQ dedicado.
- **Eventos de domínio via port `EventPublisher`.** Adapter Spring publica no event bus;
  listener converte para WebSocket após o commit. Domínio não conhece WebSocket.


- **Transporte real-time plugável, broker por padrão.** STOMP/WebSocket no `/topic`,
  propriedade `distribution.realtime.transport`:
  - `broker`: relay para RabbitMQ — N instâncias compartilham tópico (escala horizontal).
    Padrão em `docker compose up` e `application.properties`.
  - `simple`: broker em memória, sem infra extra; cada instância só alcança seus próprios
    clientes. Opt-out via `REALTIME_TRANSPORT=simple`, útil em dev local.
  - Entrega é best-effort pós-commit: broker ausente nunca quebra negócio
    (`DashboardNotifier` engole/loga falha; dashboard cai em polling). Testes fixam
    `simple` via `@TestPropertySource`. Fila de atendimento continua no Postgres em
    qualquer modo — broker é só transporte de notificação.


- **Chat cliente↔agente reusa o mesmo transporte.** Tabela `message` (V6), publicada em
  `/topic/chat/{interactionId}`. Cliente usa endpoints públicos; agente usa autenticados
  restritos ao próprio `agent_id`. Frontend: um único `ChatThread` para cliente/agente/admin.
- **Encerramento dos dois lados reusa `EndInteraction`.** Agente:
  `POST /api/agent/conversations/{id}/end` (checagem de posse). Cliente:
  `POST /api/public/interactions/{id}/end` (sem login). Lado oposto percebe via polling.
- **Indicador de nova mensagem** é estado só de frontend (`animate-blink` quando chega
  mensagem de conversa não selecionada).
- **Admin: monitor + reset**, sem regra de negócio, direto nos repositórios JPA:
  - `GET /api/admin/conversations` — lista tudo, `ChatThread` somente leitura por conversa.
  - `POST /api/admin/reset` — limpa filas e marca tudo `ENDED` numa transação; carga do
    atendente é derivada do estado, logo libera vagas automaticamente.
- **Infra em Spring puro** (controllers, repositórios JPA, snapshot) — sem abstração
  desnecessária.
- **Persistência separada do domínio.** Entidades JPA próprias + mappers. Carga do
  atendente é derivada (nunca dessincroniza).
- **API limpa.** DTOs = records; erros = Problem Details (404/409/422/400). Entidades JPA
  nunca vazam na API.
- **Docs via springdoc + Scalar** em `/scalar` (mais enxuto que Swagger UI); spec em
  `/v3/api-docs`.
- **Virtual threads ligadas** (`spring.threads.virtual.enabled=true`).
- **"Atender próximo"** por time: encerra o mais antigo em atendimento, reusando
  `EndInteraction` e puxando o próximo da fila.

## Observabilidade

- **`TraceIdFilter`** (prioridade máxima): reaproveita `X-Trace-Id` recebido ou gera um
  novo; vai pro MDC (`traceId`) e volta no header de resposta; limpo no `finally`.
- **Log de auditoria em INFO**: criação/alocação/enfileiramento/encerramento, mensagens de
  chat, reset admin — materializa os eventos de domínio já existentes.
- **Sem infra nova.** Correlação leve (MDC + header) em vez de OTel/Tempo/Jaeger; caminho
  de evolução aditivo (Micrometer Tracing + OTLP). Actuator expõe `health`/`info`/`metrics`.

> Nenhum recurso deste ciclo exigiu mudança no `docker-compose` — tudo roda sobre a infra
> já existente (Postgres, broker STOMP, mesmo backend).

## Segurança

- **`ADMIN`**: cria/encerra atendimentos, vê e opera todos os times, endpoints `/api/admin/**`.
- **`AGENT`**: dashboard restrito ao próprio time (`app_user.team_id`), só "Atender próximo"
  do próprio time.
- `advance-queue` aceita `ADMIN` ou `AGENT`; agente só encerra atendimento próprio
  (checagem de posse no controller). `/api/public/**` fica aberto (sem login) para o
  cliente. Login, Scalar e health são públicos. Token `Bearer`, redirect ao login em 401/403.
- **Escopo por time via JWT claim `teamId`** (só para AGENT): `/api/dashboard` filtra por
  esse time; `advance-queue` recusa (403) se o `teamId` do path não bater. ADMIN sem claim
  vê/opera tudo.
- **`app_user.agent_id → agent.id`** liga login ao atendente real (vínculo por nome no `V4`,
  já que ids de `agent` são aleatórios). Hover na linha do atendente mostra seus clientes
  (`AgentSnapshot.serving`).
- **JWT stateless** (Spring Security resource server, HMAC via Nimbus). Usuários no Postgres
  (`app_user`, senha BCrypt, `team_id`/`agent_id` opcionais), seed via Flyway
  (`V2` admin, `V3` agentes por time, `V4` vínculo agente↔`agent` + `bruno` em Cartões).
  Provedor trocável via `UserDetailsService`.

## Frontend

React + Vite + Tailwind, paleta azul-marinho/índigo com acentos teal/esmeralda. Feed ao
vivo resolve IDs para nomes legíveis.

Estado global em **Zustand** (`authStore` com token em localStorage, `dashboardStore` para
times/eventos/status). Organização por feature (`features/auth`, `features/dashboard`)
sobre `shared/` (cliente HTTP com `Bearer` + tratamento 401/403, realtime, i18n,
constantes). WebSocket + polling encapsulados em `useDashboardLive`.

## Testes

**Backend** (JUnit 5, Mockito, Testcontainers): unitários puros de domínio (regra dos 3,
ciclo, roteamento, fila) e casos de uso; integração com Testcontainers cobrindo
`SKIP LOCKED` em Postgres real e toda a superfície HTTP (auth, roles, ciclo, fila,
erros 400/401/403/404/409). Cobertura JaCoCo ~100% (exclui config/bootstrap/entidades JPA).

**Frontend** (Vitest + Testing Library + jsdom): stores, i18n, cliente HTTP, realtime,
hook, componentes. Cobertura ~99%.

## Git

Branches pequenas por assunto (`feat/`, `fix/`, `chore/`, `docs/`), Conventional Commits,
um commit por camada, merge na `main` via PR.