import { useEffect, useRef, useState } from 'react'
import { fetchConversations, endConversation } from '../../shared/api/chat.js'
import { connectChat } from '../../shared/api/realtime.js'
import ChatThread from '../chat/ChatThread.jsx'
import { t } from '../../shared/i18n/i18n.js'

const POLL_INTERVAL_MS = 3000

/**
 * The agent's chat panel: one dialog per customer they're serving right now. The list
 * refreshes on a short interval (new customers get assigned as the queue advances); the
 * selected conversation opens as a live thread.
 *
 * Two extras on top of the list: the agent can END a conversation (freeing the slot and
 * pulling the next in line), and a customer's name BLINKS while it has an unread message —
 * we subscribe to every open thread and flag the ones the agent isn't currently looking at.
 */
export default function AgentConversations() {
  const [conversations, setConversations] = useState([])
  const [selected, setSelected] = useState(null)
  const [unread, setUnread] = useState(() => new Set())
  const [ending, setEnding] = useState(false)

  // The live message handlers close over these, so keep refs to avoid stale reads.
  const selectedRef = useRef(selected)
  useEffect(() => {
    selectedRef.current = selected
  }, [selected])

  const load = () => fetchConversations().catch(() => null)

  useEffect(() => {
    let active = true
    const refresh = () =>
      load().then((c) => {
        if (active && c) setConversations(c)
      })
    refresh()
    const poll = setInterval(refresh, POLL_INTERVAL_MS)
    return () => {
      active = false
      clearInterval(poll)
    }
  }, [])

  // Keep a valid selection as conversations come and go; drop unread flags for gone ones.
  useEffect(() => {
    const ids = new Set(conversations.map((c) => c.interactionId))
    if (conversations.length === 0) {
      setSelected(null)
    } else if (!ids.has(selected)) {
      setSelected(conversations[0].interactionId)
    }
    setUnread((prev) => {
      const next = new Set([...prev].filter((id) => ids.has(id)))
      return next.size === prev.size ? prev : next
    })
  }, [conversations, selected])

  // Opening a conversation clears its "new message" flag.
  useEffect(() => {
    if (!selected) return
    setUnread((prev) => {
      if (!prev.has(selected)) return prev
      const next = new Set(prev)
      next.delete(selected)
      return next
    })
  }, [selected])

  // Subscribe to every open thread: a customer message on one we're not looking at blinks it.
  // Keyed on the (stable) set of ids, so a 3s poll that returns the same customers doesn't
  // tear down and re-open the STOMP subscriptions every tick.
  const idsKey = conversations.map((c) => c.interactionId).join(',')
  useEffect(() => {
    const ids = idsKey ? idsKey.split(',') : []
    const disconnects = ids.map((id) =>
      connectChat(id, (msg) => {
        if (msg.sender === 'CUSTOMER' && id !== selectedRef.current) {
          setUnread((prev) => (prev.has(id) ? prev : new Set(prev).add(id)))
        }
      }),
    )
    return () => disconnects.forEach((d) => d())
  }, [idsKey])

  async function end() {
    if (!selected) return
    setEnding(true)
    try {
      await endConversation(selected)
      const c = (await load()) ?? []
      setConversations(c)
    } catch {
      /* the poll will reconcile */
    } finally {
      setEnding(false)
    }
  }

  return (
    <div className="rounded-2xl bg-slate-900/70 p-5 ring-1 ring-slate-800 backdrop-blur">
      <h3 className="text-lg font-semibold">{t('chat.title')}</h3>
      <p className="mb-4 text-xs text-slate-400">{t('chat.subtitle')}</p>

      {conversations.length === 0 ? (
        <p className="text-xs text-slate-500">{t('chat.noConversations')}</p>
      ) : (
        <>
          <div className="mb-3 flex flex-wrap gap-2">
            {conversations.map((c) => {
              const isSelected = selected === c.interactionId
              const isUnread = unread.has(c.interactionId)
              return (
                <button
                  key={c.interactionId}
                  onClick={() => setSelected(c.interactionId)}
                  aria-label={isUnread ? t('chat.newMessageFor', { name: c.customerName }) : c.customerName}
                  className={`flex items-center gap-1.5 rounded-full px-3 py-1 text-xs ring-1 transition ${
                    isSelected
                      ? 'bg-teal-500/20 text-teal-200 ring-teal-500/40'
                      : isUnread
                        ? 'animate-blink bg-amber-500/20 text-amber-100 ring-amber-400/50'
                        : 'bg-slate-800/70 text-slate-300 ring-slate-700 hover:text-slate-100'
                  }`}
                >
                  {isUnread && <span className="h-1.5 w-1.5 rounded-full bg-amber-400" />}
                  {c.customerName}
                </button>
              )
            })}
          </div>
          {selected && (
            <>
              <ChatThread key={selected} interactionId={selected} variant="agent" />
              <button
                onClick={end}
                disabled={ending}
                className="mt-3 w-full rounded-xl bg-rose-500/15 px-4 py-2 text-xs font-medium text-rose-200 ring-1 ring-rose-500/30 transition hover:bg-rose-500/25 disabled:opacity-50"
              >
                {ending ? t('chat.ending') : t('chat.endConversation')}
              </button>
            </>
          )}
        </>
      )}
    </div>
  )
}
