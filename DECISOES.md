# Decisões de projeto

Resumo curto do que foi escolhido e por quê. O sistema distribui atendimentos entre
times (Cartões, Empréstimos, Outros): cada atendente cuida de no máximo 3 ao mesmo
tempo e, quando o time lota, o cliente espera numa fila e entra assim que abre uma vaga.

## Stack: Java 21, Gradle e Spring Boot 4.1

- **Java 21 (LTS).** É a versão de suporte longo atual. Traz as *virtual threads* que usamos
  para escalar chamadas bloqueantes de forma barata, e recursos como `record`, `sealed` e
  *pattern matching* em `switch` — que deixam o domínio (eventos, resultados) enxuto e
  seguro, sem código repetitivo.

- **Gradle em vez de Maven.** Build em Kotlin DSL (tipado, com autocomplete na IDE), mais
  conciso que o XML do Maven e mais rápido no dia a dia (build incremental e cache). É
  também o padrão do Spring Initializr.

- **Spring Boot 4.1.** Versão mais recente, sobre o Spring Framework 7. É modular — cada
  recurso é um *starter* próprio, então o app só carrega o que usa — e tem suporte nativo a
  virtual threads e a Problem Details (RFC 9457), ambos aproveitados aqui. Por ser novo,
  exigiu alguns ajustes de dependência (ver *Compatibilidade Spring Boot 4.1*, no fim).

## Idioma

- **Código e comentários em inglês** (padrão de mercado, facilita colaboração).
- **Interface em pt/en**, escolhida automaticamente pelo idioma do navegador.

## Arquitetura — Clean Architecture pragmática

Isolamento rigoroso **só no núcleo de negócio**; no resto, Spring direto (sem cerimônia).
Tudo vive num módulo `distribution` no estilo Ports & Adapters:

```
domain/          Java puro, sem Spring e sem JPA
  model/         Interaction, Agent, Team, Subject, WaitingQueue, InteractionState
  event/         eventos de domínio (sealed DomainEvent)
  routing/       RoutingStrategy + SubjectRouter (Strategy)
  service/       DistributionService (aloca ou enfileira)
  port/in|out/   interfaces dos casos de uso e dos repositórios/gateways
application/     implementação dos casos de uso, @Transactional
infrastructure/  Spring: web, persistência (JPA), websocket, wiring
```

**Por quê:** as regras que não podem quebrar (limite de 3, ciclo do atendimento) ficam
protegidas e testáveis sem framework; o resto usa Spring sem abstrações desnecessárias.

## Decisões principais

- **Regras dentro do modelo.** O `Agent` garante o limite de 3 dentro do `assign`; o
  `Interaction` controla seu ciclo `WAITING → IN_SERVICE → ENDED`. Não dá para burlar de
  fora. O `DistributionService` é lógica pura: escolhe o atendente menos ocupado ou manda
  para a fila.



- **Strategy para roteamento (Open/Closed).** Cada time é uma `RoutingStrategy`. Um time
  novo = uma classe nova + uma linha de `@Bean`; nada existente muda.




- **A fila é uma tabela no Postgres, não um broker.** A fila é regra de negócio: ordenada,
  persistente, por time. Consumimos com `SELECT ... FOR UPDATE SKIP LOCKED`, o que dá
  consumo concorrente seguro (dois atendentes terminando juntos nunca pegam o mesmo
  cliente) sem precisar subir RabbitMQ/ActiveMQ só para isso.




- **Eventos de domínio desacoplam as pontas.** O núcleo publica por um port `EventPublisher`;
  um adapter Spring joga no event bus e um listener converte em mensagem WebSocket **depois
  do commit**. O domínio não sabe que WebSocket existe.



- **Transporte do tempo real é plugável, com o broker como padrão (pensando em escala
  horizontal).** O push para o dashboard é STOMP sobre WebSocket, e o *broker* que serve o
  `/topic` é escolhido por propriedade `distribution.realtime.transport`:
  - `broker`: faz **relay do `/topic` para um broker STOMP externo** (RabbitMQ). Assim
    **N instâncias do backend compartilham o mesmo tópico** — um evento gerado em qualquer
    instância chega aos dashboards conectados a *todas* elas. É isso que viabiliza rodar o
    backend atrás de um load balancer com várias réplicas (**escala horizontal**). É o
    **padrão ao subir com Docker** (`docker compose up` já sobe o RabbitMQ e liga o relay).
    É o **padrão da aplicação** (`application.properties`: `distribution.realtime.transport=broker`),
    logo `docker compose up` e `gradlew bootRun` já sobem em modo broker.
  - `simple`: broker **em memória**. Zero infra extra, ideal para uma instância; cada
    instância só alcança os dashboards conectados a ela mesma. Fica como **opção** de opt-out
    (`REALTIME_TRANSPORT=simple`), útil no desenvolvimento local sem um broker.

  **Default é `broker` em todo lugar.** Como a entrega de eventos é **best-effort e acontece
  depois do commit**, um broker ausente ou ainda conectando **nunca quebra a operação de
  negócio**: o `DashboardNotifier` engole e loga a falha de envio, e o dashboard cai no
  *polling*. Por isso o broker pode ser o padrão sem risco mesmo rodando local sem RabbitMQ.
  A **suíte de testes** fixa `transport=simple` (via `@TestPropertySource`), para não exigir
  um broker externo no CI.

  O ponto-chave é que **nada** no código que publica muda (`DashboardNotifier` só ganhou o
  tratamento best-effort; `SimpMessagingTemplate` é o mesmo) e o **frontend também não muda** —
  só a configuração do broker. A fila de atendimento **continua no Postgres** (`SKIP LOCKED`):
  o broker aqui é só o transporte de notificações em tempo real, não a linha de espera (ver a
  decisão acima sobre por que a fila é uma tabela, e não um broker). Ver *Modo simple (opcional)*
  no `COMO-SUBIR.md`.



- **Spring direto na infraestrutura.** Controllers, repositórios JPA e o endpoint de
  snapshot são Spring puro. O snapshot é só uma consulta (sem regra), então lê os
  repositórios direto, sem inventar um port.



- **Persistência separada do domínio.** Entidades JPA são classes próprias; mappers
  convertem para o domínio. A carga do atendente não é armazenada — é derivada dos
  atendimentos em andamento, então nunca fica dessincronizada.



- **API limpa.** DTOs são records; erros viram Problem Details (RFC 9457) — 404 para não
  encontrado, 409 para regra violada, 422 para roteamento, 400 para entrada inválida.
  Entidades JPA nunca aparecem na API.



- **Documentação da API.** O spec OpenAPI é gerado pelo springdoc e renderizado pelo
  **Scalar** (uma UI de referência moderna) em `/scalar`, em tema escuro por padrão. O JSON
  do spec fica em `/v3/api-docs`. Preferi o Scalar ao Swagger UI por ser mais enxuto e
  legível.



- **Virtual threads (Java 21).** `spring.threads.virtual.enabled=true`: chamadas JDBC/fila
  bloqueantes ganham uma thread barata cada e escalam bem.



- **"Atender próximo".** Botão por time que libera uma vaga (encerra o atendimento mais
  antigo em andamento), o que reaproveita o `EndInteraction` e puxa o próximo da fila.



## Segurança

Há dois perfis de acesso por **role**:
- **`ADMIN`**: pode criar e encerrar atendimentos, além de operar a fila.
- **`VIEWER`**: possui acesso somente para visualização do dashboard.
As rotas de escrita exigem perfil **`ADMIN`**, enquanto login, documentação (Scalar) e health check permanecem públicos.
No frontend, o token é armazenado e enviado como `Bearer`, com redirecionamento para a tela de login em respostas **401** ou **403**.
A autenticação usa **JWT stateless** (Spring Security como *resource server*; token HMAC assinado/validado com o Nimbus). Os usuários ficam **persistidos no Postgres** (tabela `app_user`, com senha em hash BCrypt), semeados via Flyway (`V2`) com as contas de demonstração `admin`/`viewer`. Um `UserDetailsService` lê o usuário e sua role do banco; trocar por outro provedor é só reimplementar essa interface.

## Frontend

React + Vite + Tailwind. Paleta em azul-marinho/índigo com acentos em teal/esmeralda,
passando tecnologia, segurança e conforto. O feed ao vivo resolve os IDs para nomes de
time/atendente e mostra frases legíveis em vez de UUIDs.

O estado global fica em **Zustand** — `authStore` (token persistido em localStorage) e
`dashboardStore` (times/eventos/status + ações). O código é organizado **por feature**
(`features/auth`, `features/dashboard`) sobre um `shared/` comum (cliente HTTP que injeta o
`Bearer` e trata 401/403, realtime, i18n e constantes); o ciclo de WebSocket + polling vive
num hook (`useDashboardLive`), deixando as páginas e componentes enxutos.

## Testes

**Backend** (JUnit 5, Mockito, Testcontainers):
- **Unitários puros** (sem Spring/banco) cobrindo domínio (regra dos 3, ciclo, roteamento,
  fila) e os casos de uso.
- **Integração** com Testcontainers: o `SKIP LOCKED` num Postgres real e um teste que sobe o
  servidor (porta aleatória) exercitando toda a superfície HTTP — auth, roles, ciclo do
  atendimento, fila e erros (400/401/403/404/409).
- Cobertura por **JaCoCo** (`./gradlew test` gera o relatório em
  `build/reports/jacoco/test/html/`): **~100% de linhas** (config, bootstrap e entidades JPA
  ficam de fora da métrica, por serem "boilerplate").

**Frontend** (Vitest + Testing Library + jsdom):
- Testes de stores, i18n, cliente HTTP, realtime, hook e componentes, em `src/test/`
  espelhando `src/`. Cobertura **~99% de linhas** (`npm run coverage`, relatório em
  `coverage/`).

## Git

Trabalho dividido em branches pequenas por assunto (`feat/…`, `fix/…`, `chore/…`, `docs/…`),
com Conventional Commits e um commit por camada, integradas na `main` via pull request.
