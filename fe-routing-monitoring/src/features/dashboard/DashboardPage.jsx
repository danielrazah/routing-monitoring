import Header from './components/Header.jsx'
import TeamCard from './components/TeamCard.jsx'
import NewInteractionForm from './components/NewInteractionForm.jsx'
import EventFeed from './components/EventFeed.jsx'
import AgentConversations from './AgentConversations.jsx'
import AdminConversations from './AdminConversations.jsx'
import { useDashboardStore } from './dashboardStore.js'
import { useDashboardLive } from './useDashboardLive.js'
import { useAuthStore } from '../auth/authStore.js'
import { t } from '../../shared/i18n/i18n.js'

export default function DashboardPage() {
  useDashboardLive()

  const teams = useDashboardStore((s) => s.teams)
  const events = useDashboardStore((s) => s.events)
  const status = useDashboardStore((s) => s.status)
  const isAdmin = useAuthStore((s) => s.roles.includes('ADMIN'))
  const isAgent = useAuthStore((s) => s.roles.includes('AGENT'))

  return (
    <div className="mx-auto max-w-6xl px-6 py-8">
      <Header />

      <div className="mt-8 grid gap-6 lg:grid-cols-3">
        <section className="lg:col-span-2">
          <h2 className="mb-3 text-sm font-medium uppercase tracking-wider text-slate-400">{t('teams.title')}</h2>
          <div className="grid gap-4 sm:grid-cols-2">
            {teams.map((team) => (
              <TeamCard key={team.id} team={team} />
            ))}
          </div>
        </section>

        <aside className="space-y-6">
          {isAdmin && <NewInteractionForm />}
          {isAdmin && <AdminConversations />}
          {isAgent && <AgentConversations />}
          <EventFeed events={events} teams={teams} live={status === 'connected'} />
        </aside>
      </div>
    </div>
  )
}
