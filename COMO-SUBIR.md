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

| Serviço          | URL                              |
|------------------|----------------------------------|
| Dashboard        | http://localhost:8090            |
| API              | http://localhost:8080/api        |
| Docs REST (Scalar) | http://localhost:8080/scalar   |
| Health           | http://localhost:8080/actuator/health |
| Postgres         | localhost:5432 (`routing` / `routing`) |

## Acesso (login)

O dashboard exige login. Contas de demonstração:
OBS: Para facilitar a vida de quem vai avaliar, coloquei dois perfis para emular o uso de Roles do spring security.

| Usuário  | Senha      | Perfil                                                         |
|----------|------------|----------------------------------------------------------------|
| `admin`  | `admin123` | ADMIN — cria/encerra atendimentos, atende fila e emula cliente |
| `viewer` | `viewer123`| VIEWER — só visualiza o dashboard                              |

O `viewer` entra e vê tudo, mas não vê os controles de escrita (formulário e botão
"Atender próximo").

## Testar rapidamente

1. Abra o dashboard em http://localhost:8090 e entre como `admin`.
2. No formulário **Novo atendimento**, crie vários contatos do mesmo assunto
   (ex.: *Contratação de empréstimo*, que vai para o time Empréstimos com 1 atendente).
3. Os 3 primeiros ocupam o atendente; a partir do 4º entram na **fila**.
4. Clique em **Atender próximo** no card do time para liberar uma vaga e puxar o
   próximo da fila.

Pelo terminal, o mesmo fluxo:

```bash
# criar um atendimento
curl -X POST http://localhost:8080/api/interactions \
  -H 'Content-Type: application/json' \
  -d '{"customerName":"Maria","subject":"LOAN_CONTRACTING"}'

# adiantar a fila de um time (libera uma vaga)
curl -X POST http://localhost:8080/api/teams/{teamId}/advance-queue
```

> O idioma do dashboard segue o idioma do navegador (pt ou en).

## Parar

```bash
docker compose down       # para os containers
docker compose down -v    # também apaga os dados do Postgres
```

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