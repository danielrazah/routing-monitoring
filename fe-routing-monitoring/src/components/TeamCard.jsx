import { useState } from 'react'
import CapacityMeter from './CapacityMeter.jsx'
import { t, tv } from '../lib/i18n.js'

export default function TeamCard({ team, onServeNext, canServe = true }) {
  const totalLoad = team.agents.reduce((sum, a) => sum + a.currentLoad, 0)
  const totalCapacity = team.agents.reduce((sum, a) => sum + a.maxConcurrent, 0)
  const [busy, setBusy] = useState(false)
  const hasQueue = team.waiting > 0
  const serving = team.serving ?? []
  const queue = team.queue ?? []

  async function serveNext() {
    setBusy(true)
    try {
      await onServeNext(team.id)
    } finally {
      setBusy(false)
    }
  }

  return (
    <div className="rounded-2xl bg-slate-900/70 p-5 ring-1 ring-slate-800 backdrop-blur transition hover:ring-slate-700">
      <div className="flex items-center justify-between">
        <div>
          <h3 className="text-lg font-semibold">{tv('team', team.name)}</h3>
          <p className="text-xs text-slate-400">
            {t('teams.slots', { used: totalLoad, total: totalCapacity })}
          </p>
        </div>
        <WaitingBadge count={team.waiting} />
      </div>

      <ul className="mt-4 space-y-3">
        {team.agents.length === 0 && (
          <li className="text-sm text-slate-500">{t('teams.noAgents')}</li>
        )}
        {team.agents.map((agent) => (
          <li key={agent.id} className="flex items-center justify-between gap-3">
            <span className="truncate text-sm text-slate-200">{agent.name}</span>
            <div className="flex items-center gap-3">
              <CapacityMeter load={agent.currentLoad} max={agent.maxConcurrent} />
              <span className="w-8 text-right text-xs tabular-nums text-slate-400">
                {agent.currentLoad}/{agent.maxConcurrent}
              </span>
            </div>
          </li>
        ))}
      </ul>

      <div className="mt-4 space-y-3 border-t border-slate-800 pt-4">
        <NameRow
          label={t('teams.inService')}
          names={serving}
          tone="bg-teal-500/15 text-teal-200 ring-teal-500/30"
        />
        <NameRow
          label={t('teams.inQueue')}
          names={queue}
          tone="bg-amber-500/15 text-amber-200 ring-amber-500/30"
          numbered
        />
      </div>

      {canServe && (
        <button
          onClick={serveNext}
          disabled={!hasQueue || busy}
          title={t('teams.serveNextHint')}
          className="mt-4 w-full rounded-xl bg-slate-800/80 px-4 py-2 text-sm font-medium text-teal-300 ring-1 ring-slate-700 transition hover:bg-slate-800 hover:text-teal-200 disabled:cursor-not-allowed disabled:text-slate-500 disabled:ring-slate-800 disabled:hover:bg-slate-800/80"
        >
          {t('teams.serveNext')}
        </button>
      )}
    </div>
  )
}

function NameRow({ label, names, tone, numbered }) {
  return (
    <div>
      <p className="mb-1.5 text-[11px] font-medium uppercase tracking-wider text-slate-500">{label}</p>
      {names.length === 0 ? (
        <p className="text-xs text-slate-600">{t('teams.none')}</p>
      ) : (
        <div className="flex flex-wrap gap-1.5">
          {names.map((name, i) => (
            <span
              key={`${name}-${i}`}
              className={`inline-flex items-center gap-1 rounded-full px-2 py-0.5 text-xs ring-1 transition ${tone}`}
            >
              {numbered && <span className="tabular-nums opacity-60">{i + 1}.</span>}
              <span className="truncate max-w-[8rem]">{name}</span>
            </span>
          ))}
        </div>
      )}
    </div>
  )
}

function WaitingBadge({ count }) {
  const waiting = count > 0
  return (
    <span
      className={`rounded-full px-3 py-1 text-xs font-medium ring-1 ${
        waiting
          ? 'bg-amber-500/15 text-amber-300 ring-amber-500/30'
          : 'bg-emerald-500/15 text-emerald-300 ring-emerald-500/30'
      }`}
    >
      {waiting ? t('teams.waiting', { count }) : t('teams.noQueue')}
    </span>
  )
}
