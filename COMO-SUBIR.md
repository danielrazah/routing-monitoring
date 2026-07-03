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

- **Docker** e **Docker Compose** instalados. É o único requisito: o backend e o frontend são
  compilados dentro dos próprios containers.
- No **macOS** e no **Windows**, o Docker Compose já vem incluído no **Docker Desktop**. No
  **Linux**, ele vem junto ao instalar o **Docker Engine** (plugin `docker compose`) ou o Docker Desktop.

| Sistema  | Instalação                                                                 |
|----------|----------------------------------------------------------------------------|
| macOS    | Docker Desktop — https://docs.docker.com/desktop/setup/install/mac-install/     |
| Windows  | Docker Desktop — https://docs.docker.com/desktop/setup/install/windows-install/ |
| Linux    | Docker Engine + Compose — https://docs.docker.com/engine/install/ (ou Docker Desktop: https://docs.docker.com/desktop/setup/install/linux/) |

Confira a instalação (Compose v2 é o comando `docker compose`, com espaço):

```bash
docker --version
docker compose version
```

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

Abra o documento `COMO-TESTAR.md`

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

## Rodar os testes e relatório de cobertura

**Backend** (precisa de JDK 21):

```bash
cd be-routing-monitoring
./gradlew test           # testes (o de integração usa Testcontainers + Docker)
                         # gera também o relatório de cobertura (JaCoCo) em
                         # build/reports/jacoco/test/html/index.html
```

**Frontend** (precisa de Node 20):

```bash
cd fe-routing-monitoring
npm test            # roda os testes uma vez (Vitest + jsdom)
npm run coverage    # idem, com relatório de cobertura em coverage/index.html
```

Os testes ficam em `src/test/` espelhando a árvore de `src/`. Cobertura de linhas ~100%
no backend e ~99% no frontend.