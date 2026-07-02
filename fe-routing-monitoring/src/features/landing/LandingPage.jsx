import { useAuthStore } from '../auth/authStore.js'
import { t } from '../../shared/i18n/i18n.js'
import ubotsLogo from '../../assets/ubots-logo.png'

// Two simple inline icons so the page stays self-contained (no icon dependency).
function ChatIcon() {
  return (
    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" className="h-6 w-6">
      <path strokeLinecap="round" strokeLinejoin="round"
        d="M12 20.25c4.97 0 9-3.694 9-8.25s-4.03-8.25-9-8.25S3 7.194 3 11.25c0 1.6.5 3.09 1.357 4.34.15.22.2.49.13.74l-.9 3.28a.75.75 0 0 0 .92.92l3.28-.9c.25-.07.52-.02.74.13A9.7 9.7 0 0 0 12 20.25Z" />
    </svg>
  )
}

function GridIcon() {
  return (
    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" className="h-6 w-6">
      <path strokeLinecap="round" strokeLinejoin="round"
        d="M3.75 6A2.25 2.25 0 0 1 6 3.75h2.25A2.25 2.25 0 0 1 10.5 6v2.25a2.25 2.25 0 0 1-2.25 2.25H6A2.25 2.25 0 0 1 3.75 8.25V6ZM3.75 15.75A2.25 2.25 0 0 1 6 13.5h2.25a2.25 2.25 0 0 1 2.25 2.25V18a2.25 2.25 0 0 1-2.25 2.25H6A2.25 2.25 0 0 1 3.75 18v-2.25ZM13.5 6a2.25 2.25 0 0 1 2.25-2.25H18A2.25 2.25 0 0 1 20.25 6v2.25A2.25 2.25 0 0 1 18 10.5h-2.25a2.25 2.25 0 0 1-2.25-2.25V6ZM13.5 15.75a2.25 2.25 0 0 1 2.25-2.25H18a2.25 2.25 0 0 1 2.25 2.25V18A2.25 2.25 0 0 1 18 20.25h-2.25a2.25 2.25 0 0 1-2.25-2.25v-2.25Z" />
    </svg>
  )
}

function Arrow() {
  return (
    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2"
      className="h-4 w-4 transition group-hover:translate-x-0.5">
      <path strokeLinecap="round" strokeLinejoin="round" d="M5 12h14M13 6l6 6-6 6" />
    </svg>
  )
}

export default function LandingPage() {
  const authenticated = useAuthStore((s) => !!s.token)

  return (
    <div className="grid min-h-screen place-items-center px-6 py-12">
      <div className="w-full max-w-3xl">
        <div className="mb-10 flex flex-col items-center text-center">
          <div className="mb-5 grid h-12 place-items-center rounded-2xl bg-white px-3 shadow-lg shadow-black/20 ring-1 ring-slate-200">
            <img src={ubotsLogo} alt="ubots" className="h-7 w-auto" />
          </div>
          <p className="text-xs font-medium uppercase tracking-[0.2em] text-teal-400/80">{t('landing.tagline')}</p>
          <h1 className="mt-3 bg-gradient-to-r from-indigo-300 via-slate-100 to-teal-300 bg-clip-text text-3xl font-semibold tracking-tight text-transparent sm:text-4xl">
            {t('landing.title')}
          </h1>
          <p className="mt-3 max-w-md text-sm text-slate-400">{t('landing.subtitle')}</p>
        </div>

        <div className="grid gap-5 sm:grid-cols-2">
          <a
            href="/atendimento"
            className="group rounded-2xl bg-slate-900/70 p-6 ring-1 ring-slate-800 backdrop-blur transition hover:-translate-y-0.5 hover:ring-teal-400/50"
          >
            <div className="mb-4 grid h-12 w-12 place-items-center rounded-xl bg-teal-500/15 text-teal-300 ring-1 ring-teal-500/30">
              <ChatIcon />
            </div>
            <h2 className="text-lg font-semibold">{t('landing.customer.title')}</h2>
            <p className="mt-1 text-sm text-slate-400">{t('landing.customer.desc')}</p>
            <span className="mt-4 inline-flex items-center gap-1.5 text-sm font-medium text-teal-300">
              {t('landing.customer.cta')}
              <Arrow />
            </span>
          </a>

          <a
            href="/painel"
            className="group rounded-2xl bg-slate-900/70 p-6 ring-1 ring-slate-800 backdrop-blur transition hover:-translate-y-0.5 hover:ring-indigo-400/50"
          >
            <div className="mb-4 grid h-12 w-12 place-items-center rounded-xl bg-indigo-500/15 text-indigo-300 ring-1 ring-indigo-500/30">
              <GridIcon />
            </div>
            <h2 className="text-lg font-semibold">{t('landing.team.title')}</h2>
            <p className="mt-1 text-sm text-slate-400">
              {authenticated ? t('landing.team.descIn') : t('landing.team.descOut')}
            </p>
            <span className="mt-4 inline-flex items-center gap-1.5 text-sm font-medium text-indigo-300">
              {authenticated ? t('landing.team.ctaIn') : t('landing.team.ctaOut')}
              <Arrow />
            </span>
          </a>
        </div>

        <p className="mt-10 text-center text-[11px] tracking-wide text-slate-500">FlowPay · Routing Monitoring</p>
      </div>
    </div>
  )
}
