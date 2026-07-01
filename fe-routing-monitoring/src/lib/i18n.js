// Tiny i18n: pick the browser language (pt or en), fall back to English.
// No dependency — just dictionaries and a couple of helpers.

const DICTS = {
  en: {
    'header.subtitle': 'Live view of how customer contacts are distributed',
    'status.connected': 'Live',
    'status.offline': 'Polling',
    'status.connecting': 'Connecting…',

    'teams.title': 'Teams',
    'teams.slots': '{used}/{total} slots in use',
    'teams.noAgents': 'No agents on this team.',
    'teams.waiting': '{count} waiting',
    'teams.noQueue': 'No queue',
    'teams.serveNext': 'Serve next',
    'teams.serveNextHint': 'Frees a slot and pulls the next customer in line',

    'form.title': 'New contact',
    'form.subtitle': 'Simulate a customer reaching out.',
    'form.name': 'Customer name',
    'form.namePlaceholder': 'e.g. John Carter',
    'form.subject': 'Subject',
    'form.submit': 'Route contact',
    'form.submitting': 'Routing…',
    'form.error': 'Could not route the contact. Try again.',

    'subject.CARD_ISSUE': 'Card issue',
    'subject.LOAN_CONTRACTING': 'Loan contracting',
    'subject.OTHER': 'Other',

    'team.Cards': 'Cards',
    'team.Loans': 'Loans',
    'team.Others': 'Others',

    'feed.title': 'Live feed',
    'feed.subtitle': 'Distribution events, newest first.',
    'feed.empty': 'Waiting for activity…',
    'feed.pollingHint': 'This browser is polling for counts; the event stream needs a WebSocket.',
    'feed.created': 'New contact',
    'feed.assigned': 'Assigned',
    'feed.queued': 'Queued',
    'feed.ended': 'Ended',
    'feed.createdDetail': '{subject} → {team}',
    'feed.assignedDetail': '→ {agent} · {team}',
    'feed.queuedDetail': 'waiting in {team}',
    'feed.endedDetail': '{agent} freed a slot · {team}',
    'feed.someAgent': 'an agent',
    'feed.someTeam': 'a team',
  },
  pt: {
    'header.subtitle': 'Visão em tempo real da distribuição de atendimentos',
    'status.connected': 'Ao vivo',
    'status.offline': 'Atualizando',
    'status.connecting': 'Conectando…',

    'teams.title': 'Times',
    'teams.slots': '{used}/{total} vagas em uso',
    'teams.noAgents': 'Nenhum atendente neste time.',
    'teams.waiting': '{count} na fila',
    'teams.noQueue': 'Sem fila',
    'teams.serveNext': 'Atender próximo',
    'teams.serveNextHint': 'Libera uma vaga e chama o próximo da fila',

    'form.title': 'Novo atendimento',
    'form.subtitle': 'Simule um cliente entrando em contato.',
    'form.name': 'Nome do cliente',
    'form.namePlaceholder': 'ex.: João Carter',
    'form.subject': 'Assunto',
    'form.submit': 'Distribuir',
    'form.submitting': 'Distribuindo…',
    'form.error': 'Não foi possível distribuir o atendimento. Tente novamente.',

    'subject.CARD_ISSUE': 'Problema com cartão',
    'subject.LOAN_CONTRACTING': 'Contratação de empréstimo',
    'subject.OTHER': 'Outros',

    'team.Cards': 'Cartões',
    'team.Loans': 'Empréstimos',
    'team.Others': 'Outros',

    'feed.title': 'Feed ao vivo',
    'feed.subtitle': 'Eventos de distribuição, mais recentes primeiro.',
    'feed.empty': 'Aguardando atividade…',
    'feed.pollingHint': 'Este navegador está usando polling para os contadores; o feed de eventos precisa de WebSocket.',
    'feed.created': 'Novo atendimento',
    'feed.assigned': 'Alocado',
    'feed.queued': 'Na fila',
    'feed.ended': 'Encerrado',
    'feed.createdDetail': '{subject} → {team}',
    'feed.assignedDetail': '→ {agent} · {team}',
    'feed.queuedDetail': 'aguardando em {team}',
    'feed.endedDetail': '{agent} liberou uma vaga · {team}',
    'feed.someAgent': 'um atendente',
    'feed.someTeam': 'um time',
  },
}

function detectLocale() {
  const lang = (navigator.language || 'en').toLowerCase()
  return lang.startsWith('pt') ? 'pt' : 'en'
}

export const locale = detectLocale()

// Reflect the language on the document for accessibility.
if (typeof document !== 'undefined') {
  document.documentElement.lang = locale
}

const dict = DICTS[locale]

/** Translate a key, replacing {placeholders} with params. */
export function t(key, params) {
  const template = dict[key] ?? DICTS.en[key] ?? key
  if (!params) return template
  return template.replace(/\{(\w+)\}/g, (_, name) => (params[name] ?? ''))
}

/** Translate a dynamic value (team/subject) by prefix, falling back to the raw value. */
export function tv(prefix, value) {
  const key = `${prefix}.${value}`
  return dict[key] ?? DICTS.en[key] ?? value
}

/** Locale-aware clock time. */
export function formatTime(iso) {
  if (!iso) return ''
  return new Date(iso).toLocaleTimeString(locale)
}
