import { useState } from 'react'
import { SUBJECTS } from '../lib/subjects.js'
import { createInteraction } from '../lib/api.js'

export default function NewInteractionForm() {
  const [customerName, setCustomerName] = useState('')
  const [subject, setSubject] = useState(SUBJECTS[0].value)
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
    } catch (err) {
      setError(err.message)
    } finally {
      setBusy(false)
    }
  }

  return (
    <form onSubmit={submit} className="rounded-2xl bg-slate-900/70 p-5 ring-1 ring-slate-800 backdrop-blur">
      <h3 className="text-lg font-semibold">New contact</h3>
      <p className="mb-4 text-xs text-slate-400">Simulate a customer reaching out.</p>

      <label className="block text-xs font-medium text-slate-400">Customer name</label>
      <input
        value={customerName}
        onChange={(e) => setCustomerName(e.target.value)}
        placeholder="e.g. John Carter"
        className="mt-1 w-full rounded-xl bg-slate-950/60 px-3 py-2 text-sm ring-1 ring-slate-800 outline-none focus:ring-2 focus:ring-teal-400/60"
      />

      <label className="mt-4 block text-xs font-medium text-slate-400">Subject</label>
      <select
        value={subject}
        onChange={(e) => setSubject(e.target.value)}
        className="mt-1 w-full rounded-xl bg-slate-950/60 px-3 py-2 text-sm ring-1 ring-slate-800 outline-none focus:ring-2 focus:ring-teal-400/60"
      >
        {SUBJECTS.map((s) => (
          <option key={s.value} value={s.value}>{s.label}</option>
        ))}
      </select>

      {error && <p className="mt-3 text-xs text-rose-400">{error}</p>}

      <button
        type="submit"
        disabled={busy}
        className="mt-5 w-full rounded-xl bg-gradient-to-r from-indigo-500 to-teal-400 px-4 py-2.5 text-sm font-semibold text-slate-950 transition hover:opacity-90 disabled:opacity-50"
      >
        {busy ? 'Routing…' : 'Route contact'}
      </button>
    </form>
  )
}
