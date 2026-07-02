import CapacityMeter from './CapacityMeter.jsx'

export default function TeamCard({ team }) {
  const totalLoad = team.agents.reduce((sum, a) => sum + a.currentLoad, 0)
  const totalCapacity = team.agents.reduce((sum, a) => sum + a.maxConcurrent, 0)

  return (
    <div className="rounded-2xl bg-slate-900/70 p-5 ring-1 ring-slate-800 backdrop-blur transition hover:ring-slate-700">
      <div className="flex items-center justify-between">
        <div>
          <h3 className="text-lg font-semibold">{team.name}</h3>
          <p className="text-xs text-slate-400">
            {totalLoad}/{totalCapacity} slots in use
          </p>
        </div>
        <WaitingBadge count={team.waiting} />
      </div>

      <ul className="mt-4 space-y-3">
        {team.agents.length === 0 && (
          <li className="text-sm text-slate-500">No agents on this team.</li>
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
      {waiting ? `${count} waiting` : 'No queue'}
    </span>
  )
}
