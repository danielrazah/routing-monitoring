# fe-routing-monitoring

Real-time dashboard for the FlowPay **routing-monitoring** service. It shows how customer
interactions are distributed across teams and lets an operator drive the flow.

## Stack

- **React 18 + Vite** (build/dev server)
- **Tailwind CSS** for styling (dark, navy/indigo + teal palette)
- **Zustand** for state management (with `persist` for the auth token)
- **@stomp/stompjs** for live updates over native WebSocket
- **Vitest + Testing Library** for tests and coverage

## Architecture (feature-based)

```
src/
├── App.jsx                      # pathname routing: landing / customer / dashboard
├── features/
│   ├── auth/                    # authStore (token/roles + persist) + LoginPage
│   ├── landing/                 # LandingPage (customer vs. team entry)
│   ├── customer/                # CustomerPage: join queue, see who serves you, chat, END
│   ├── chat/                    # ChatThread: one component for customer / agent / admin (read-only)
│   └── dashboard/
│       ├── dashboardStore.js    # teams / events / status + refresh, serveNext
│       ├── useDashboardLive.js  # WebSocket + polling lifecycle
│       ├── api.js               # dashboard/interaction/team endpoints
│       ├── DashboardPage.jsx
│       ├── AgentConversations.jsx   # agent's dialogs: END + blink on new message
│       ├── AdminConversations.jsx   # admin monitor of all live chats + "End all" reset
│       └── components/          # Header, TeamCard, EventFeed, NewInteractionForm, CapacityMeter
└── shared/
    ├── api/http.js              # fetch wrapper: adds Bearer, on 401/403 logs out
    ├── api/public.js            # customer queue (join / status / end), no auth
    ├── api/chat.js              # agent + customer chat, agent END
    ├── api/admin.js             # admin monitor + reset
    ├── api/realtime.js          # STOMP connection (dashboard + per-chat topics)
    ├── i18n/i18n.js             # tiny pt/en dictionary picked by browser language
    └── constants/subjects.js
```

Pages/containers read the stores via selectors and pass data to presentational components;
the network lives in `shared/api` and the feature `api.js` files.

## Running

Usually you run the whole stack with Docker from the repo root (`docker compose up --build`)
— this app is then served by nginx on **http://localhost:8090**.

Standalone dev (needs Node 20 and the backend on `localhost:8080`):

```bash
npm install
npm run dev        # http://localhost:8090, proxies /api and /ws to the backend
npm run build      # production bundle
```

## Auth & roles

The dashboard requires login. The token (JWT) is stored by `authStore` and sent as
`Authorization: Bearer …` on every API call; a 401/403 clears it and returns to the login
screen. Two demo roles:

- **ADMIN** — creates interactions, advances any queue, sees every team, **monitors every live
  conversation** and has an **End all** button that ends all interactions and clears every queue
  (a testing convenience).
- **AGENT** — scoped to its own team (the backend returns only that team's data); can "Serve next"
  on that team's queue, chat with its customers, **end its own conversations**, and sees a customer's
  name **blink** when a new message arrives on a dialog it isn't looking at.

The **customer** screen (`/atendimento`) needs no login: it shows **who is serving you** and lets
you **end the conversation**. Every backend response carries an `X-Trace-Id` header for correlation.

## Internationalization

UI language follows the browser (`navigator.language`): Portuguese for `pt*`, English
otherwise. Strings live in `shared/i18n/i18n.js`.

## Tests & coverage

```bash
npm test            # run the unit/component tests once (Vitest + jsdom)
npm run coverage    # same, plus a coverage report in coverage/index.html
```

Tests live under `src/test/`, mirroring the `src/` tree, and import source via the `@`
alias. They cover the stores, i18n, the HTTP client, the realtime + live hooks, the pages
and the components — line coverage is ~99%. The v8 report is written to `coverage/`.
