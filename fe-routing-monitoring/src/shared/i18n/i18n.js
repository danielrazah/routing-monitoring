// Tiny i18n: pick the browser language (pt or en), fall back to English.
// No dependency — just dictionaries and a couple of helpers.

const DICTS = {
  en: {
    'nav.home': 'Home',

    'landing.tagline': 'Real-time customer routing',
    'landing.title': 'How can we help you today?',
    'landing.subtitle': 'Choose how you’d like to continue.',
    'landing.customer.title': 'I’m a customer',
    'landing.customer.desc': 'Join the queue and talk to an agent — no login needed.',
    'landing.customer.cta': 'Get served',
    'landing.team.title': 'I’m on the team',
    'landing.team.descIn': 'Open the live routing dashboard.',
    'landing.team.descOut': 'Sign in to the routing dashboard.',
    'landing.team.ctaIn': 'Go to dashboard',
    'landing.team.ctaOut': 'Team sign in',

    'header.subtitle': 'Live view of how customer contacts are distributed',
    'header.logout': 'Log out',
    'login.title': 'Sign in',
    'login.subtitle': 'Access the routing dashboard',
    'login.username': 'Username',
    'login.password': 'Password',
    'login.submit': 'Sign in',
    'login.submitting': 'Signing in…',
    'login.error': 'Invalid username or password',
    'login.demo': 'Demo — admin / admin123 (all teams) · ana · carla · diego / agent123 (agent, own team)',
    'login.customerCta': 'Are you a customer? Join the queue',
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
    'teams.inService': 'In service',
    'teams.inQueue': 'In queue',
    'teams.none': 'nobody',
    'teams.servingBy': '{name} is serving',

    'form.title': 'New contact',
    'form.subtitle': 'Simulate a customer reaching out.',
    'form.name': 'Customer name',
    'form.namePlaceholder': 'e.g. John Carter',
    'form.subject': 'Subject',
    'form.submit': 'Route contact',
    'form.submitting': 'Routing…',
    'form.error': 'Could not route the contact. Try again.',

    'customer.title': 'Talk to us',
    'customer.subtitle': 'Join the queue — no login needed.',
    'customer.name': 'Your name',
    'customer.namePlaceholder': 'e.g. John Carter',
    'customer.subject': 'Subject',
    'customer.join': 'Join the queue',
    'customer.joining': 'Joining…',
    'customer.error': 'Could not join the queue. Try again.',
    'customer.waitingTitle': "You're in line",
    'customer.waitingBody': "As soon as an agent is free, we'll let you know right here.",
    'customer.servedTitle': 'You will be served now',
    'customer.servedBody': 'An agent is ready to talk to you.',
    'customer.chatHint': 'Chat with your agent below.',
    'customer.welcome': 'Welcome',
    'customer.newContact': 'New contact',

    'chat.title': 'My conversations',
    'chat.subtitle': 'One dialog per customer you are serving.',
    'chat.noConversations': 'No one in service right now.',
    'chat.empty': 'No messages yet.',
    'chat.placeholder': 'Type a message…',
    'chat.send': 'Send',

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
    'nav.home': 'Início',

    'landing.tagline': 'Distribuição de atendimentos em tempo real',
    'landing.title': 'Como podemos te ajudar hoje?',
    'landing.subtitle': 'Escolha como você quer continuar.',
    'landing.customer.title': 'Sou cliente',
    'landing.customer.desc': 'Entre na fila e fale com um atendente — sem precisar de login.',
    'landing.customer.cta': 'Ser atendido',
    'landing.team.title': 'Sou da equipe',
    'landing.team.descIn': 'Abrir o painel de distribuição ao vivo.',
    'landing.team.descOut': 'Entrar no painel de distribuição.',
    'landing.team.ctaIn': 'Ir para o dashboard',
    'landing.team.ctaOut': 'Entrar (equipe)',

    'header.subtitle': 'Visão em tempo real da distribuição de atendimentos',
    'header.logout': 'Sair',
    'login.title': 'Entrar',
    'login.subtitle': 'Acesse o painel de distribuição',
    'login.username': 'Usuário',
    'login.password': 'Senha',
    'login.submit': 'Entrar',
    'login.submitting': 'Entrando…',
    'login.error': 'Usuário ou senha inválidos',
    'login.demo': 'Demo — admin / admin123 (todos os times) · ana · carla · diego / agent123 (agente, só o seu time)',
    'login.customerCta': 'É cliente? Entre na fila',
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
    'teams.inService': 'Em atendimento',
    'teams.inQueue': 'Na fila',
    'teams.none': 'ninguém',
    'teams.servingBy': '{name} está atendendo',

    'form.title': 'Novo atendimento',
    'form.subtitle': 'Simule um cliente entrando em contato.',
    'form.name': 'Nome do cliente',
    'form.namePlaceholder': 'ex.: João Carter',
    'form.subject': 'Assunto',
    'form.submit': 'Distribuir',
    'form.submitting': 'Distribuindo…',
    'form.error': 'Não foi possível distribuir o atendimento. Tente novamente.',

    'customer.title': 'Fale com a gente',
    'customer.subtitle': 'Entre na fila — sem precisar de login.',
    'customer.name': 'Seu nome',
    'customer.namePlaceholder': 'ex.: João Carter',
    'customer.subject': 'Assunto',
    'customer.join': 'Entrar na fila',
    'customer.joining': 'Entrando…',
    'customer.error': 'Não foi possível entrar na fila. Tente novamente.',
    'customer.waitingTitle': 'Você está na fila',
    'customer.waitingBody': 'Assim que um atendente ficar livre, avisamos aqui.',
    'customer.servedTitle': 'Você será atendido agora',
    'customer.servedBody': 'Um atendente está pronto para falar com você.',
    'customer.chatHint': 'Converse com seu atendente abaixo.',
    'customer.welcome': 'Boas-vindas',
    'customer.newContact': 'Novo atendimento',

    'chat.title': 'Meus atendimentos',
    'chat.subtitle': 'Um diálogo para cada cliente que você atende.',
    'chat.noConversations': 'Ninguém em atendimento agora.',
    'chat.empty': 'Ainda sem mensagens.',
    'chat.placeholder': 'Escreva uma mensagem…',
    'chat.send': 'Enviar',

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
