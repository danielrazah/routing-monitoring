import { useEffect, useState } from 'react'
import { fetchAdminConversations, resetBoard } from '../../shared/api/admin.js'
import { useDashboardStore } from './dashboardStore.js'
import ChatThread from '../chat/ChatThread.jsx'
import { t, tv } from '../../shared/i18n/i18n.js'

const POLL_INTERVAL_MS = 3000

/**
 * The admin's cross-team monitor: every live customer↔agent conversation at once, each
 * openable as a read-only live thread. Plus the "reset" button that ends all interactions
 * and clears every queue — a convenience for testing.
 */
export default function AdminConversations() {
  const [conversations, setConversations] = useState([])
  const [selected, setSelected] = useState(null)
  const [resetting, setResetting] = useState(false)
  const refreshDashboard = useDashboardStore((s) => s.refresh)

  const load = () => fetchAdminConversations().catch(() => null)

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

  // Keep a valid selection as conversations come and go.
  useEffect(() => {
    if (conversations.length === 0) {
      setSelected(null)
    } else if (!conversations.some((c) => c.interactionId === selected)) {
      setSelected(conversations[0].interactionId)
    }
  }, [conversations, selected])

  async function reset() {
    if (typeof window !== 'undefined' && !window.confirm(t('admin.resetConfirm'))) return
    setResetting(true)
    try {
      await resetBoard()
      setConversations([])
      setSelected(null)
      await refreshDashboard()
    } catch {
      /* leave the view as-is; the poll will reconcile */
    } finally {
      setResetting(false)
    }
  }

  const current = conversations.find((c) => c.interactionId === selected)

  return (
    <div className="rounded-2xl bg-slate-900/70 p-5 ring-1 ring-slate-800 backdrop-blur">
      <div className="flex items-start justify-between gap-3">
        <div>
          <h3 className="text-lg font-semibold">{t('admin.title')}</h3>
          <p className="mb-4 text-xs text-slate-400">{t('admin.subtitle')}</p>
        </div>
        <button
          onClick={reset}
          disabled={resetting}
          className="shrink-0 rounded-xl bg-rose-500/15 px-3 py-1.5 text-xs font-semibold text-rose-200 ring-1 ring-rose-500/30 transition hover:bg-rose-500/25 disabled:opacity-50"
        >
          {resetting ? t('admin.resetting') : t('admin.reset')}
        </button>
      </div>

      {conversations.length === 0 ? (
        <p className="text-xs text-slate-500">{t('admin.noConversations')}</p>
      ) : (
        <>
          <div className="mb-3 flex flex-wrap gap-2">
            {conversations.map((c) => (
              <button
                key={c.interactionId}
                onClick={() => setSelected(c.interactionId)}
                className={`rounded-full px-3 py-1 text-left text-xs ring-1 transition ${
                  selected === c.interactionId
                    ? 'bg-teal-500/20 text-teal-200 ring-teal-500/40'
                    : 'bg-slate-800/70 text-slate-300 ring-slate-700 hover:text-slate-100'
                }`}
              >
                {c.customerName}
                <span className="ml-1 text-slate-500">
                  · {c.agentName ?? '—'}
                  {c.teamName ? ` (${tv('team', c.teamName)})` : ''}
                </span>
              </button>
            ))}
          </div>
          {current && (
            <>
              <p className="mb-2 text-xs text-slate-400">
                {t('admin.watching', {
                  customer: current.customerName,
                  agent: current.agentName ?? '—',
                })}
              </p>
              <ChatThread key={selected} interactionId={selected} variant="admin" />
            </>
          )}
        </>
      )}
    </div>
  )
}
