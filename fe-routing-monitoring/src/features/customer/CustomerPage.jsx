import { useEffect, useState } from 'react'
import { SUBJECT_VALUES } from '../../shared/constants/subjects.js'
import { joinQueue, fetchInteractionStatus, endMyInteraction } from '../../shared/api/public.js'
import { connectDashboard } from '../../shared/api/realtime.js'
import ChatThread from '../chat/ChatThread.jsx'
import { t, tv } from '../../shared/i18n/i18n.js'
import ubotsLogo from '../../assets/ubots-logo.png'

// How often we poll our own status as a fallback when the WebSocket isn't available.
const POLL_INTERVAL_MS = 3000

// An interaction that isn't WAITING has been picked up by an agent.
const isBeingServed = (state) => !!state && state !== 'WAITING'

export default function CustomerPage() {
  const [phase, setPhase] = useState('form') // 'form' | 'waiting' | 'served' | 'ended'
  const [customerName, setCustomerName] = useState('')
  const [subject, setSubject] = useState(SUBJECT_VALUES[0])
  const [interactionId, setInteractionId] = useState(null)
  const [agentName, setAgentName] = useState(null)
  const [busy, setBusy] = useState(false)
  const [ending, setEnding] = useState(false)
  const [error, setError] = useState(null)

  async function submit(event) {
    event.preventDefault()
    if (!customerName.trim()) return
    setBusy(true)
    setError(null)
    try {
      const created = await joinQueue(customerName.trim(), subject)
      setInteractionId(created.id)
      setAgentName(created.assignedAgentName ?? null)
      setPhase(isBeingServed(created.state) ? 'served' : 'waiting')
    } catch {
      setError(t('customer.error'))
    } finally {
      setBusy(false)
    }
  }

  // While waiting: listen for our ASSIGNED event over WebSocket, and poll as a fallback.
  useEffect(() => {
    if (phase !== 'waiting' || !interactionId) return

    const disconnect = connectDashboard((message) => {
      if (message.type === 'ASSIGNED' && message.interactionId === interactionId) {
        setPhase('served')
      }
    })

    const poll = setInterval(async () => {
      try {
        const status = await fetchInteractionStatus(interactionId)
        if (isBeingServed(status.state)) setPhase('served')
      } catch {
        /* transient: keep waiting */
      }
    }, POLL_INTERVAL_MS)

    return () => {
      disconnect()
      clearInterval(poll)
    }
  }, [phase, interactionId])

  // While served: learn who is serving us, and notice if the agent ends the conversation.
  useEffect(() => {
    if (phase !== 'served' || !interactionId) return
    let active = true

    const check = async () => {
      try {
        const status = await fetchInteractionStatus(interactionId)
        if (!active) return
        if (status.state === 'ENDED') setPhase('ended')
        else setAgentName(status.assignedAgentName ?? null)
      } catch {
        /* transient: keep the current view */
      }
    }

    check()
    const poll = setInterval(check, POLL_INTERVAL_MS)
    return () => {
      active = false
      clearInterval(poll)
    }
  }, [phase, interactionId])

  async function endChat() {
    if (!interactionId) return
    setEnding(true)
    try {
      await endMyInteraction(interactionId)
      setPhase('ended')
    } catch {
      /* the poll will reconcile */
    } finally {
      setEnding(false)
    }
  }

  function startOver() {
    setPhase('form')
    setInteractionId(null)
    setAgentName(null)
    setCustomerName('')
    setSubject(SUBJECT_VALUES[0])
  }

  return (
    <div className="grid min-h-screen place-items-center px-6">
      <div className="w-full max-w-sm">
        <div className="rounded-2xl bg-slate-900/70 p-7 ring-1 ring-slate-800 backdrop-blur">
        <div className="mb-6 flex items-center gap-3">
          <a href="/" aria-label={t('nav.home')} className="grid h-11 place-items-center rounded-2xl bg-white px-3 shadow-lg shadow-black/20 ring-1 ring-slate-200 transition hover:opacity-90">
            <img src={ubotsLogo} alt="ubots" className="h-6 w-auto" />
          </a>
          <div>
            <h1 className="text-lg font-semibold tracking-tight">{t('customer.title')}</h1>
            <p className="text-xs text-slate-400">{t('customer.subtitle')}</p>
          </div>
        </div>

        {phase === 'form' && (
          <form onSubmit={submit}>
            <label className="block text-xs font-medium text-slate-400">{t('customer.name')}</label>
            <input
              value={customerName}
              onChange={(e) => setCustomerName(e.target.value)}
              placeholder={t('customer.namePlaceholder')}
              className="mt-1 w-full rounded-xl bg-slate-950/60 px-3 py-2 text-sm ring-1 ring-slate-800 outline-none focus:ring-2 focus:ring-teal-400/60"
            />

            <label className="mt-4 block text-xs font-medium text-slate-400">{t('customer.subject')}</label>
            <select
              value={subject}
              onChange={(e) => setSubject(e.target.value)}
              className="mt-1 w-full rounded-xl bg-slate-950/60 px-3 py-2 text-sm ring-1 ring-slate-800 outline-none focus:ring-2 focus:ring-teal-400/60"
            >
              {SUBJECT_VALUES.map((value) => (
                <option key={value} value={value}>{tv('subject', value)}</option>
              ))}
            </select>

            {error && <p className="mt-3 text-xs text-rose-400">{error}</p>}

            <button
              type="submit"
              disabled={busy}
              className="mt-5 w-full rounded-xl bg-gradient-to-r from-indigo-500 to-teal-400 px-4 py-2.5 text-sm font-semibold text-slate-950 transition hover:opacity-90 disabled:opacity-50"
            >
              {busy ? t('customer.joining') : t('customer.join')}
            </button>
          </form>
        )}

        {phase === 'waiting' && (
          <div className="text-center">
            <div className="mx-auto grid h-14 w-14 place-items-center rounded-full bg-amber-500/15 ring-1 ring-amber-500/30">
              <span className="h-3 w-3 animate-pulse rounded-full bg-amber-400" />
            </div>
            <h2 className="mt-4 text-base font-semibold">{t('customer.waitingTitle')}</h2>
            <p className="mt-1 text-sm text-slate-400">{t('customer.waitingBody')}</p>
          </div>
        )}

        {phase === 'served' && (
          <div>
            <div className="mb-4 flex items-center gap-3">
              <div
                role="img"
                aria-label={t('customer.welcome')}
                className="grid h-10 w-10 place-items-center rounded-full bg-emerald-500/15 text-xl ring-1 ring-emerald-500/30 shadow-lg shadow-emerald-500/10"
              >
                👋
              </div>
              <div>
                <h2 className="text-sm font-semibold text-teal-300">{t('customer.servedTitle')}</h2>
                <p className="text-[11px] text-slate-400">
                  {agentName ? t('customer.servedBy', { name: agentName }) : t('customer.chatHint')}
                </p>
              </div>
            </div>

            <ChatThread interactionId={interactionId} variant="public" />

            <button
              onClick={endChat}
              disabled={ending}
              className="mt-3 w-full rounded-xl bg-rose-500/15 px-4 py-2 text-xs font-medium text-rose-200 ring-1 ring-rose-500/30 transition hover:bg-rose-500/25 disabled:opacity-50"
            >
              {ending ? t('customer.ending') : t('customer.endChat')}
            </button>
          </div>
        )}

        {phase === 'ended' && (
          <div className="text-center">
            <div className="mx-auto grid h-14 w-14 place-items-center rounded-full bg-slate-500/15 text-xl ring-1 ring-slate-500/30">
              👋
            </div>
            <h2 className="mt-4 text-base font-semibold">{t('customer.endedTitle')}</h2>
            <p className="mt-1 text-sm text-slate-400">{t('customer.endedBody')}</p>
            <button
              onClick={startOver}
              className="mt-4 w-full rounded-xl bg-slate-800 px-4 py-2 text-xs font-medium text-slate-300 transition hover:bg-slate-700"
            >
              {t('customer.newContact')}
            </button>
          </div>
        )}
        </div>
        <p className="mt-4 text-center text-[11px]">
          <a href="/" className="text-slate-500 hover:text-slate-300">← {t('nav.home')}</a>
        </p>
      </div>
    </div>
  )
}
