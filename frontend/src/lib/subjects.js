// The subjects the API accepts, with human labels for the form.
export const SUBJECTS = [
  { value: 'CARD_ISSUE', label: 'Card issue' },
  { value: 'LOAN_CONTRACTING', label: 'Loan contracting' },
  { value: 'OTHER', label: 'Other' },
]

// Accent colors per event type, used in the live feed.
export const EVENT_STYLES = {
  CREATED: 'bg-indigo-500/15 text-indigo-300 ring-indigo-500/30',
  ASSIGNED: 'bg-emerald-500/15 text-emerald-300 ring-emerald-500/30',
  QUEUED: 'bg-amber-500/15 text-amber-300 ring-amber-500/30',
  ENDED: 'bg-slate-500/15 text-slate-300 ring-slate-500/30',
}
