import { useEffect, useState } from 'react'
import Header from './Header.jsx'
import TeamCard from './TeamCard.jsx'
import NewInteractionForm from './NewInteractionForm.jsx'
import EventFeed from './EventFeed.jsx'
import { advanceQueue, connectDashboard, fetchSnapshot } from '../lib/api.js'
import { t } from '../lib/i18n.js'

// How often we refresh counters when the live WebSocket isn't available.
const POLL_INTERVAL_MS = 2500

export default function Dashboard({ auth, onLogout }) {
  const [teams, setTeams] = useState([])
  const [events, setEvents] = useState([])
  const [status, setStatus] = useState('connecting')

  const isAdmin = (auth?.roles ?? []).includes('ADMIN')

  async function refresh() {
    try {
      const snapshot = await fetchSnapshot()
      setTeams(snapshot.teams)
    } catch {
      // ignore a transient failure; the next tick/event will refresh
    }
  }

  async function serveNext(teamId) {
    await advanceQueue(teamId)
    refresh()
  }

  useEffect(() => {
    refresh()

    // Live push where the browser allows it: each event feeds the timeline and refreshes counts.
    const disconnect = connectDashboard(
      (message) => {
        setEvents((prev) => [message, ...prev].slice(0, 30))
        refresh()
      },
      setStatus,
    )

    // Fallback that works everywhere (Safari included): poll the snapshot on an interval.
    const poll = setInterval(refresh, POLL_INTERVAL_MS)

    return () => {
      disconnect()
      clearInterval(poll)
    }
  }, [])

  return (
    <div className="mx-auto max-w-6xl px-6 py-8">
      <Header status={status} user={auth?.username} roles={auth?.roles} onLogout={onLogout} />

      <div className="mt-8 grid gap-6 lg:grid-cols-3">
        <section className="lg:col-span-2">
          <h2 className="mb-3 text-sm font-medium uppercase tracking-wider text-slate-400">{t('teams.title')}</h2>
          <div className="grid gap-4 sm:grid-cols-2">
            {teams.map((team) => (
              <TeamCard key={team.id} team={team} onServeNext={serveNext} canServe={isAdmin} />
            ))}
          </div>
        </section>

        <aside className="space-y-6">
          {isAdmin && <NewInteractionForm />}
          <EventFeed events={events} teams={teams} live={status === 'connected'} />
        </aside>
      </div>
    </div>
  )
}
