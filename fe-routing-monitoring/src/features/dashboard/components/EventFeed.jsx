import { EVENT_STYLES } from '../../../shared/constants/subjects.js'
import { t, tv, formatTime } from '../../../shared/i18n/i18n.js'

export default function EventFeed({ events, teams, live }) {
  // Resolve ids to human names using the current snapshot.
  const teamName = (id) => {
    const team = teams.find((x) => x.id === id)
    return team ? tv('team', team.name) : t('feed.someTeam')
  }
  const agentName = (id) => {
    for (const team of teams) {
      const agent = team.agents.find((a) => a.id === id)
      if (agent) return agent.name
    }
    return t('feed.someAgent')
  }

  return (
    <div className="rounded-2xl bg-slate-900/70 p-5 ring-1 ring-slate-800 backdrop-blur">
      <h3 className="text-lg font-semibold">{t('feed.title')}</h3>
      <p className="mb-4 text-xs text-slate-400">{t('feed.subtitle')}</p>

      {events.length === 0 ? (
        <p className="text-sm text-slate-500">{live ? t('feed.empty') : t('feed.pollingHint')}</p>
      ) : (
        <ul className="space-y-2">
          {events.map((event, i) => (
            <li key={i} className="flex items-start gap-3 rounded-xl bg-slate-950/40 px-3 py-2">
              <span
                className={`mt-0.5 shrink-0 rounded-full px-2.5 py-0.5 text-[11px] font-semibold ring-1 ${EVENT_STYLES[event.type] ?? ''}`}
              >
                {label(event.type)}
              </span>
              <div className="min-w-0 flex-1">
                <p className="truncate text-xs text-slate-200">{describe(event, teamName, agentName)}</p>
                <p className="text-[11px] text-slate-500">
                  <span className="font-mono">#{shortId(event.interactionId)}</span>
                  <span className="mx-1.5">·</span>
                  <span className="tabular-nums">{formatTime(event.occurredAt)}</span>
                </p>
              </div>
            </li>
          ))}
        </ul>
      )}
    </div>
  )
}

function label(type) {
  const key = { CREATED: 'created', ASSIGNED: 'assigned', QUEUED: 'queued', ENDED: 'ended' }[type]
  return key ? t(`feed.${key}`) : type
}

function describe(event, teamName, agentName) {
  const team = teamName(event.teamId)
  switch (event.type) {
    case 'CREATED':
      return t('feed.createdDetail', { subject: tv('subject', event.subject), team })
    case 'ASSIGNED':
      return t('feed.assignedDetail', { agent: agentName(event.agentId), team })
    case 'QUEUED':
      return t('feed.queuedDetail', { team })
    case 'ENDED':
      return t('feed.endedDetail', { agent: agentName(event.agentId), team })
    default:
      return ''
  }
}

function shortId(id) {
  return id ? id.slice(0, 8) : ''
}
