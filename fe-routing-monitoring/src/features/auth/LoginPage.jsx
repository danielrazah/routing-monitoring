import { useState } from 'react'
import { useAuthStore } from './authStore.js'
import { t } from '../../shared/i18n/i18n.js'

export default function LoginPage() {
  const login = useAuthStore((s) => s.login)
  const [username, setUsername] = useState('')
  const [password, setPassword] = useState('')
  const [busy, setBusy] = useState(false)
  const [error, setError] = useState(null)

  async function submit(event) {
    event.preventDefault()
    setBusy(true)
    setError(null)
    try {
      await login(username.trim(), password)
      // The App switches to the dashboard as soon as the token lands in the store.
    } catch {
      setError(t('login.error'))
    } finally {
      setBusy(false)
    }
  }

  return (
    <div className="grid min-h-screen place-items-center px-6">
      <form onSubmit={submit} className="w-full max-w-sm rounded-2xl bg-slate-900/70 p-7 ring-1 ring-slate-800 backdrop-blur">
        <div className="mb-6 flex items-center gap-3">
          <div className="grid h-11 w-11 place-items-center rounded-2xl bg-gradient-to-br from-indigo-500 to-teal-400 shadow-lg shadow-indigo-500/20">
            <span className="text-lg font-bold text-slate-950">F</span>
          </div>
          <div>
            <h1 className="text-lg font-semibold tracking-tight">{t('login.title')}</h1>
            <p className="text-xs text-slate-400">{t('login.subtitle')}</p>
          </div>
        </div>

        <label htmlFor="username" className="block text-xs font-medium text-slate-400">{t('login.username')}</label>
        <input
          id="username"
          value={username}
          onChange={(e) => setUsername(e.target.value)}
          autoComplete="username"
          className="mt-1 w-full rounded-xl bg-slate-950/60 px-3 py-2 text-sm ring-1 ring-slate-800 outline-none focus:ring-2 focus:ring-teal-400/60"
        />

        <label htmlFor="password" className="mt-4 block text-xs font-medium text-slate-400">{t('login.password')}</label>
        <input
          id="password"
          type="password"
          value={password}
          onChange={(e) => setPassword(e.target.value)}
          autoComplete="current-password"
          className="mt-1 w-full rounded-xl bg-slate-950/60 px-3 py-2 text-sm ring-1 ring-slate-800 outline-none focus:ring-2 focus:ring-teal-400/60"
        />

        {error && <p className="mt-3 text-xs text-rose-400">{error}</p>}

        <button
          type="submit"
          disabled={busy || !username || !password}
          className="mt-6 w-full rounded-xl bg-gradient-to-r from-indigo-500 to-teal-400 px-4 py-2.5 text-sm font-semibold text-slate-950 transition hover:opacity-90 disabled:opacity-50"
        >
          {busy ? t('login.submitting') : t('login.submit')}
        </button>

        <p className="mt-5 text-center text-[11px] text-slate-500">{t('login.demo')}</p>
      </form>
    </div>
  )
}
