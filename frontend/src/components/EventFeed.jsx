import { EVENT_STYLES } from '../lib/subjects.js'

export default function EventFeed({ events, live }) {
  return (
    <div className="rounded-2xl bg-slate-900/70 p-5 ring-1 ring-slate-800 backdrop-blur">
      <h3 className="text-lg font-semibold">Live feed</h3>
      <p className="mb-4 text-xs text-slate-400">Distribution events, newest first.</p>

      {events.length === 0 ? (
        <p className="text-sm text-slate-500">
          {live
            ? 'Waiting for activity…'
            : 'This browser is polling for counts; the event stream needs a WebSocket.'}
        </p>
      ) : (
        <ul className="space-y-2">
          {events.map((event, i) => (
            <li key={i} className="flex items-center justify-between gap-3 rounded-xl bg-slate-950/40 px-3 py-2">
              <span className={`rounded-full px-2.5 py-0.5 text-[11px] font-semibold ring-1 ${EVENT_STYLES[event.type] ?? ''}`}>
                {event.type}
              </span>
              <span className="flex-1 truncate text-xs text-slate-400">
                {event.subject ? `${event.subject} · ` : ''}
                {shortId(event.interactionId)}
              </span>
              <span className="text-[11px] tabular-nums text-slate-500">{time(event.occurredAt)}</span>
            </li>
          ))}
        </ul>
      )}
    </div>
  )
}

function shortId(id) {
  return id ? id.slice(0, 8) : ''
}

function time(iso) {
  if (!iso) return ''
  return new Date(iso).toLocaleTimeString()
}
