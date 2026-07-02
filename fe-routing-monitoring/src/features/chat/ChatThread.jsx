import { useEffect, useRef, useState } from 'react'
import { connectChat } from '../../shared/api/realtime.js'
import {
  fetchPublicMessages,
  sendPublicMessage,
  fetchMessages,
  sendMessage,
} from '../../shared/api/chat.js'
import { t } from '../../shared/i18n/i18n.js'

const POLL_INTERVAL_MS = 3000

// One component for both sides. 'public' = the customer; 'agent' = the dashboard agent.
const VARIANTS = {
  public: { load: fetchPublicMessages, send: sendPublicMessage, me: 'CUSTOMER' },
  agent: { load: fetchMessages, send: sendMessage, me: 'AGENT' },
}

export default function ChatThread({ interactionId, variant = 'public' }) {
  const api = VARIANTS[variant]
  const [messages, setMessages] = useState([])
  const [text, setText] = useState('')
  const [busy, setBusy] = useState(false)
  const endRef = useRef(null)

  // Live push + polling fallback both feed here; dedupe by id so nothing shows twice.
  function merge(incoming) {
    setMessages((prev) => {
      const byId = new Map(prev.map((m) => [m.id, m]))
      for (const m of incoming) byId.set(m.id, m)
      return [...byId.values()].sort((a, b) => new Date(a.createdAt) - new Date(b.createdAt))
    })
  }

  useEffect(() => {
    if (!interactionId) return
    let active = true
    setMessages([])
    const load = async () => {
      try {
        const loaded = await api.load(interactionId)
        if (active && Array.isArray(loaded)) merge(loaded)
      } catch {
        /* transient: the next poll retries */
      }
    }
    load()
    const disconnect = connectChat(interactionId, (msg) => merge([msg]))
    const poll = setInterval(load, POLL_INTERVAL_MS)
    return () => {
      active = false
      disconnect()
      clearInterval(poll)
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [interactionId, variant])

  useEffect(() => {
    try {
      endRef.current?.scrollIntoView({ block: 'end' })
    } catch {
      /* not implemented in jsdom */
    }
  }, [messages])

  async function submit(event) {
    event.preventDefault()
    const body = text.trim()
    if (!body) return
    setBusy(true)
    try {
      merge([await api.send(interactionId, body)])
      setText('')
    } catch {
      /* the poll will reconcile */
    } finally {
      setBusy(false)
    }
  }

  return (
    <div className="flex flex-col">
      <div className="max-h-72 flex-1 space-y-2 overflow-y-auto pr-1">
        {messages.length === 0 ? (
          <p className="text-xs text-slate-500">{t('chat.empty')}</p>
        ) : (
          messages.map((m) => <ChatBubble key={m.id} mine={m.sender === api.me} body={m.body} />)
        )}
        <div ref={endRef} />
      </div>

      <form onSubmit={submit} className="mt-3 flex gap-2">
        <input
          value={text}
          onChange={(e) => setText(e.target.value)}
          placeholder={t('chat.placeholder')}
          className="min-w-0 flex-1 rounded-xl bg-slate-950/60 px-3 py-2 text-sm ring-1 ring-slate-800 outline-none focus:ring-2 focus:ring-teal-400/60"
        />
        <button
          type="submit"
          disabled={busy || !text.trim()}
          className="rounded-xl bg-gradient-to-r from-indigo-500 to-teal-400 px-4 py-2 text-sm font-semibold text-slate-950 transition hover:opacity-90 disabled:opacity-50"
        >
          {t('chat.send')}
        </button>
      </form>
    </div>
  )
}

function ChatBubble({ mine, body }) {
  return (
    <div className={`flex ${mine ? 'justify-end' : 'justify-start'}`}>
      <span
        className={`max-w-[80%] whitespace-pre-wrap break-words rounded-2xl px-3 py-1.5 text-sm ${
          mine
            ? 'bg-gradient-to-r from-indigo-500 to-teal-400 text-slate-950'
            : 'bg-slate-800 text-slate-100 ring-1 ring-slate-700'
        }`}
      >
        {body}
      </span>
    </div>
  )
}
