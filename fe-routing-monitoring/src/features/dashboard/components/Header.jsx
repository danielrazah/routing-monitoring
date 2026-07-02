import { t } from '../../../shared/i18n/i18n.js'
import { useAuthStore } from '../../auth/authStore.js'
import { useDashboardStore } from '../dashboardStore.js'
import ubotsLogo from '../../../assets/ubots-logo.png'

const DOTS = {
  connected: 'bg-emerald-400 animate-pulse',
  offline: 'bg-amber-400',
  connecting: 'bg-slate-600',
}

export default function Header() {
  const status = useDashboardStore((s) => s.status)
  const user = useAuthStore((s) => s.username)
  const roles = useAuthStore((s) => s.roles)
  const onLogout = useAuthStore((s) => s.logout)

  const dot = DOTS[status] ?? DOTS.connecting
  const label = t(`status.${status}`)
  return (
    <header className="flex flex-wrap items-center justify-between gap-3">
      <div className="flex items-center gap-3">
        <a href="/" aria-label={t('nav.home')} className="grid h-11 place-items-center rounded-2xl bg-white px-3 shadow-lg shadow-black/20 ring-1 ring-slate-200 transition hover:opacity-90">
          <img src={ubotsLogo} alt="ubots" className="h-6 w-auto" />
        </a>
        <div>
          <h1 className="text-xl font-semibold tracking-tight">FlowPay · Routing Monitoring</h1>
          <p className="text-sm text-slate-400">{t('header.subtitle')}</p>
        </div>
      </div>

      <div className="flex items-center gap-2">
        <div className="flex items-center gap-2 rounded-full bg-slate-900/70 px-3 py-1.5 text-sm ring-1 ring-slate-800">
          <span className={`h-2.5 w-2.5 rounded-full ${dot}`} />
          <span className="text-slate-300">{label}</span>
        </div>

        {user && (
          <div className="flex items-center gap-2 rounded-full bg-slate-900/70 px-3 py-1.5 text-sm ring-1 ring-slate-800">
            <span className="text-slate-200">{user}</span>
            {roles.map((r) => (
              <span key={r} className="rounded-full bg-indigo-500/15 px-2 py-0.5 text-[10px] font-semibold text-indigo-300 ring-1 ring-indigo-500/30">
                {r}
              </span>
            ))}
            <button
              onClick={onLogout}
              className="ml-1 text-xs font-medium text-slate-400 transition hover:text-rose-300"
            >
              {t('header.logout')}
            </button>
          </div>
        )}
      </div>
    </header>
  )
}
