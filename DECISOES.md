# Decisões de projeto

Resumo curto do que foi escolhido e por quê. O sistema distribui atendimentos entre
times (Cartões, Empréstimos, Outros): cada atendente cuida de no máximo 3 ao mesmo
tempo e, quando o time lota, o cliente espera numa fila e entra assim que abre uma vaga.

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

- **Spring direto na infraestrutura.** Controllers, repositórios JPA e o endpoint de
  snapshot são Spring puro. O snapshot é só uma consulta (sem regra), então lê os
  repositórios direto, sem inventar um port.

- **Persistência separada do domínio.** Entidades JPA são classes próprias; mappers
  convertem para o domínio. A carga do atendente não é armazenada — é derivada dos
  atendimentos em andamento, então nunca fica dessincronizada.

- **API limpa.** DTOs são records; erros viram Problem Details (RFC 9457) — 404 para não
  encontrado, 409 para regra violada, 422 para roteamento, 400 para entrada inválida.
  Entidades JPA nunca aparecem na API.

- **Virtual threads (Java 21).** `spring.threads.virtual.enabled=true`: chamadas JDBC/fila
  bloqueantes ganham uma thread barata cada e escalam bem.

- **"Atender próximo".** Botão por time que libera uma vaga (encerra o atendimento mais
  antigo em andamento), o que reaproveita o `EndInteraction` e puxa o próximo da fila.

## Tempo real (e o caso do Safari)

O dashboard recebe eventos por **WebSocket nativo** (STOMP), que passa limpo pelo nginx.
O SockJS foi testado, mas seu fallback HTTP quebra atrás do proxy nesta versão do Spring
(o `xhr_send` responde 404). Então: WebSocket nativo onde o navegador aceita e, como o
Safari derruba `ws://localhost`, um **fallback por polling** do `/api/dashboard` a cada
2,5s — assim os contadores ficam sempre vivos em qualquer navegador.

## Frontend

React + Vite + Tailwind. Paleta em azul-marinho/índigo com acentos em teal/esmeralda,
passando tecnologia, segurança e conforto. O feed ao vivo resolve os IDs para nomes de
time/atendente e mostra frases legíveis em vez de UUIDs.

## Testes

- **Teste unitário puro** (sem Spring/banco) provando a regra dos 3 e o enfileiramento.
- **Teste de integração** com Testcontainers exercitando o `SKIP LOCKED` num Postgres real.
- **Smoke test** que sobe o contexto inteiro (garante que tudo se conecta).

## Compatibilidade Spring Boot 4.1

O Boot 4 é modular. Ajustes necessários descobertos rodando o build: usar o módulo
`spring-boot-flyway` (sem ele as migrations não rodam), fixar as versões do Testcontainers
pelo BOM, e usar Tomcat (Undertow foi removido).

## Git

Trabalho dividido em branches pequenas por assunto (`feat/…`, `fix/…`, `chore/…`, `docs/…`),
com Conventional Commits e um commit por camada, integradas na `main` via pull request.
