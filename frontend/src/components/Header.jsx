const STATES = {
  connected: { dot: 'bg-emerald-400 animate-pulse', label: 'Live' },
  offline: { dot: 'bg-amber-400', label: 'Polling' },
  connecting: { dot: 'bg-slate-600', label: 'Connecting…' },
}

export default function Header({ status }) {
  const state = STATES[status] ?? STATES.connecting
  return (
    <header className="flex items-center justify-between">
      <div className="flex items-center gap-3">
        <div className="grid h-11 w-11 place-items-center rounded-2xl bg-gradient-to-br from-indigo-500 to-teal-400 shadow-lg shadow-indigo-500/20">
          <span className="text-lg font-bold text-slate-950">F</span>
        </div>
        <div>
          <h1 className="text-xl font-semibold tracking-tight">FlowPay · Routing Monitoring</h1>
          <p className="text-sm text-slate-400">Live view of how customer contacts are distributed</p>
        </div>
      </div>
      <div className="flex items-center gap-2 rounded-full bg-slate-900/70 px-3 py-1.5 text-sm ring-1 ring-slate-800">
        <span className={`h-2.5 w-2.5 rounded-full ${state.dot}`} />
        <span className="text-slate-300">{state.label}</span>
      </div>
    </header>
  )
}
