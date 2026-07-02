import { useState } from 'react'
import { SUBJECT_VALUES } from '../lib/subjects.js'
import { createInteraction } from '../lib/api.js'
import { t, tv } from '../lib/i18n.js'

export default function NewInteractionForm() {
  const [customerName, setCustomerName] = useState('')
  const [subject, setSubject] = useState(SUBJECT_VALUES[0])
  const [busy, setBusy] = useState(false)
  const [error, setError] = useState(null)

  async function submit(event) {
    event.preventDefault()
    if (!customerName.trim()) return
    setBusy(true)
    setError(null)
    try {
      await createInteraction(customerName.trim(), subject)
      setCustomerName('')
    } catch {
      setError(t('form.error'))
    } finally {
      setBusy(false)
    }
  }

  return (
    <form onSubmit={submit} className="rounded-2xl bg-slate-900/70 p-5 ring-1 ring-slate-800 backdrop-blur">
      <h3 className="text-lg font-semibold">{t('form.title')}</h3>
      <p className="mb-4 text-xs text-slate-400">{t('form.subtitle')}</p>

      <label className="block text-xs font-medium text-slate-400">{t('form.name')}</label>
      <input
        value={customerName}
        onChange={(e) => setCustomerName(e.target.value)}
        placeholder={t('form.namePlaceholder')}
        className="mt-1 w-full rounded-xl bg-slate-950/60 px-3 py-2 text-sm ring-1 ring-slate-800 outline-none focus:ring-2 focus:ring-teal-400/60"
      />

      <label className="mt-4 block text-xs font-medium text-slate-400">{t('form.subject')}</label>
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
        {busy ? t('form.submitting') : t('form.submit')}
      </button>
    </form>
  )
}
