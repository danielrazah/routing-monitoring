# Como subir o projeto

Monorepo com três peças: banco, backend (Spring Boot) e frontend (React). Tudo sobe
com um comando via Docker Compose.

## Estrutura

```
.
├── be-routing-monitoring/   # backend — Spring Boot 4.1 / Java 21
├── fe-routing-monitoring/   # frontend — React + Vite + Tailwind
└── docker-compose.yml       # sobe banco + backend + frontend juntos
```

## Pré-requisitos

- Docker e Docker Compose.
- Só isso: o backend e o frontend são compilados dentro dos próprios containers.

## Subir tudo

Na raiz do projeto:

```bash
docker compose up --build
```

A primeira vez demora alguns minutos (baixa dependências dentro dos containers).

Por padrão, a stack sobe **pronta para escala horizontal**: o tempo real usa um broker STOMP
(**RabbitMQ**), de modo que várias instâncias do backend possam compartilhar os mesmos eventos.
Quem quiser o modo enxuto de uma instância só (broker em memória, sem RabbitMQ no caminho) usa o
**modo simple**, logo abaixo. O porquê está no `DECISOES.md`.

| Serviço          | URL                              |
|------------------|----------------------------------|
| Dashboard        | http://localhost:8090            |
| API              | http://localhost:8080/api        |
| Docs REST (Scalar) | http://localhost:8080/scalar   |
| Health           | http://localhost:8080/actuator/health |
| Postgres         | localhost:5432 (`routing` / `routing`) |
| RabbitMQ (broker + STOMP) | painel em http://localhost:15672 (`guest` / `guest`) |

## Acesso (login)

O dashboard exige login. Contas de demonstração:
OBS: Para facilitar a avaliação, há dois perfis para emular o uso de Roles do Spring Security.

| Usuário  | Senha      | Perfil                                           |
|----------|------------|--------------------------------------------------|
| `admin`  | `admin123` | ADMIN — vê **todos** os times                    |
| `ana`    | `agent123` | AGENT — time Cartões (atende a própria fila)     |
| `bruno`  | `agent123` | AGENT — time Cartões (atende a própria fila)     |
| `carla`  | `agent123` | AGENT — time Empréstimos (atende a própria fila) |
| `diego`  | `agent123` | AGENT — time Outros (atende a própria fila)      |

Um `AGENT` entra e vê **somente o time a que pertence** (definido no banco, `app_user.team_id`) e
pode **"Atender próximo"** na fila **daquele** time — mas **não** cria/encerra atendimentos nem
mexe em outros times. O `ADMIN` vê todos os times e opera tudo. Cada login `AGENT` também aponta
para o atendente real que representa (`app_user.agent_id`).

## Testar rapidamente

1. Abra o dashboard em http://localhost:8090 e entre como `admin`.
2. No formulário **Novo atendimento**, crie vários contatos do mesmo assunto
   (ex.: *Contratação de empréstimo*, que vai para o time Empréstimos com 1 atendente).
3. Os 3 primeiros ocupam o atendente; a partir do 4º entram na **fila**.
4. Clique em **Atender próximo** no card do time para liberar uma vaga e puxar o
   próximo da fila.

**Chat cliente↔agente:** abra http://localhost:8090/atendimento numa aba e entre na fila (ex.:
*Contratação de empréstimo*). Noutra aba, entre no dashboard como o agente daquele time (ex.:
`carla`/`agent123` para Empréstimos): no painel **Meus atendimentos** aparece o cliente. Troquem
mensagens — os dois veem a conversa em tempo real, com um diálogo por cliente atendido.

**Encerrar o atendimento (dos dois lados).** Na tela do cliente (`/atendimento`) aparece **quem
está atendendo** e um botão **Encerrar atendimento**; ao encerrar, o cliente sai da fila do agente
e a vaga é liberada (o próximo da fila entra). No dashboard, em **Meus atendimentos**, o agente
também encerra o atendimento selecionado pelo botão **Encerrar atendimento**. Se um lado encerra, o
outro percebe em tempo real.

**Indicador de nova mensagem.** Em **Meus atendimentos**, quando chega uma mensagem nova de um
cliente cujo diálogo **não** está aberto, o nome dele **pisca** (com um ponto âmbar) até o agente
abrir a conversa.

**Perfil admin.** Além de tudo do agente, o `admin` tem dois recursos no dashboard:
- **Conversas ao vivo** — vê, em tempo real, **todas** as conversas cliente↔atendente de todos os
  times (somente leitura), uma ao lado da outra.
- **Encerrar tudo** — um botão que **encerra todos os atendimentos e esvazia todas as filas**,
  zerando o quadro para facilitar os testes (pede confirmação).

Pelo terminal, os mesmos fluxos:

```bash
# criar um atendimento (ADMIN)
curl -X POST http://localhost:8080/api/interactions \
  -H "Authorization: Bearer $TOKEN" -H 'Content-Type: application/json' \
  -d '{"customerName":"Maria","subject":"LOAN_CONTRACTING"}'

# adiantar a fila de um time (libera uma vaga)
curl -X POST http://localhost:8080/api/teams/{teamId}/advance-queue -H "Authorization: Bearer $TOKEN"

# o agente encerra um atendimento dele
curl -X POST http://localhost:8080/api/agent/conversations/{id}/end -H "Authorization: Bearer $TOKEN"

# o cliente encerra o próprio atendimento (sem login)
curl -X POST http://localhost:8080/api/public/interactions/{id}/end

# admin: ver todas as conversas ao vivo / zerar o quadro
curl http://localhost:8080/api/admin/conversations -H "Authorization: Bearer $TOKEN"
curl -X POST http://localhost:8080/api/admin/reset -H "Authorization: Bearer $TOKEN"
```

> **Rastreabilidade.** Toda resposta HTTP carrega um cabeçalho `X-Trace-Id` (reaproveitado se você
> mandar o seu, ou gerado quando falta). Esse id aparece em cada linha de log do backend, então dá
> para seguir uma ação — criar, distribuir, enfileirar, atender, encerrar, cada mensagem de chat —
> de ponta a ponta. Veja *Rastreabilidade* no `DECISOES.md`.

> O idioma do dashboard segue o idioma do navegador (pt ou en).

## Parar

```bash
docker compose down       # para os containers
docker compose down -v    # também apaga os dados do Postgres
```

## Modo simple (uma instância, opcional)

O padrão é o transporte por **broker** (RabbitMQ), pensado para escala horizontal. Se você quer
só rodar local com uma instância, sem depender do broker no caminho, use o **modo simple** — um
broker STOMP **em memória**:

```bash
REALTIME_TRANSPORT=simple docker compose up --build
```

- Nesse modo o backend ignora o RabbitMQ (o container ainda sobe, mas fica ocioso).
- Sem a variável, o `docker compose up` padrão usa o broker (escala horizontal).

> **Sobre escalar de fato:** subir várias réplicas (`--scale backend=N`) pede um load balancer na
> frente e remover o mapeamento fixo da porta 8080 — fora do escopo aqui. O ponto é que, com o
> broker como padrão, o **transporte de eventos já suporta** várias instâncias.

## Rodar sem Docker (opcional)

**Backend** (precisa de JDK 21 e um Postgres em `localhost:5432`):

```bash
cd be-routing-monitoring
./gradlew bootRun        # sobe a API
./gradlew test           # testes (o de integração usa Testcontainers + Docker)
                         # gera também o relatório de cobertura (JaCoCo) em
                         # build/reports/jacoco/test/html/index.html
```

**Frontend** (precisa de Node 20):

```bash
cd fe-routing-monitoring
npm install
npm run dev         # dev server em http://localhost:8090, com proxy para o backend
npm test            # roda os testes uma vez (Vitest + jsdom)
npm run coverage    # idem, com relatório de cobertura em coverage/index.html
```

Os testes ficam em `src/test/` espelhando a árvore de `src/`. Cobertura de linhas ~100%
no backend e ~99% no frontend.