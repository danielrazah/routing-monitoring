import { useEffect, useState } from 'react'
import { fetchConversations } from '../../shared/api/chat.js'
import ChatThread from '../chat/ChatThread.jsx'
import { t } from '../../shared/i18n/i18n.js'

const POLL_INTERVAL_MS = 3000

/**
 * The agent's chat panel: one dialog per customer they're serving right now. The list
 * refreshes on a short interval (new customers get assigned as the queue advances); the
 * selected conversation opens as a live thread.
 */
export default function AgentConversations() {
  const [conversations, setConversations] = useState([])
  const [selected, setSelected] = useState(null)

  useEffect(() => {
    let active = true
    const load = () =>
      fetchConversations().then((c) => active && setConversations(c)).catch(() => {})
    load()
    const poll = setInterval(load, POLL_INTERVAL_MS)
    return () => {
      active = false
      clearInterval(poll)
    }
  }, [])

  // Keep a valid selection as conversations come and go.
  useEffect(() => {
    if (conversations.length === 0) {
      setSelected(null)
    } else if (!conversations.some((c) => c.interactionId === selected)) {
      setSelected(conversations[0].interactionId)
    }
  }, [conversations, selected])

  return (
    <div className="rounded-2xl bg-slate-900/70 p-5 ring-1 ring-slate-800 backdrop-blur">
      <h3 className="text-lg font-semibold">{t('chat.title')}</h3>
      <p className="mb-4 text-xs text-slate-400">{t('chat.subtitle')}</p>

      {conversations.length === 0 ? (
        <p className="text-xs text-slate-500">{t('chat.noConversations')}</p>
      ) : (
        <>
          <div className="mb-3 flex flex-wrap gap-2">
            {conversations.map((c) => (
              <button
                key={c.interactionId}
                onClick={() => setSelected(c.interactionId)}
                className={`rounded-full px-3 py-1 text-xs ring-1 transition ${
                  selected === c.interactionId
                    ? 'bg-teal-500/20 text-teal-200 ring-teal-500/40'
                    : 'bg-slate-800/70 text-slate-300 ring-slate-700 hover:text-slate-100'
                }`}
              >
                {c.customerName}
              </button>
            ))}
          </div>
          {selected && <ChatThread key={selected} interactionId={selected} variant="agent" />}
        </>
      )}
    </div>
  )
}
