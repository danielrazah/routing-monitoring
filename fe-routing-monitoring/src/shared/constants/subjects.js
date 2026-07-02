// The subject values the API accepts. Human labels come from i18n (tv('subject', value)).
export const SUBJECT_VALUES = ['CARD_ISSUE', 'LOAN_CONTRACTING', 'OTHER']

// Accent colors per event type, used in the live feed.
export const EVENT_STYLES = {
  CREATED: 'bg-indigo-500/15 text-indigo-300 ring-indigo-500/30',
  ASSIGNED: 'bg-emerald-500/15 text-emerald-300 ring-emerald-500/30',
  QUEUED: 'bg-amber-500/15 text-amber-300 ring-amber-500/30',
  ENDED: 'bg-slate-500/15 text-slate-300 ring-slate-500/30',
}
