import { apiFetch } from '../../shared/api/http.js'

export async function fetchSnapshot() {
  const res = await apiFetch('/api/dashboard')
  if (!res.ok) throw new Error('Failed to load dashboard')
  return res.json()
}

export async function createInteraction(customerName, subject) {
  const res = await apiFetch('/api/interactions', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ customerName, subject }),
  })
  if (!res.ok) throw new Error('Failed to create interaction')
  return res.json()
}

export async function endInteraction(interactionId) {
  const res = await apiFetch(`/api/interactions/${interactionId}/end`, { method: 'POST' })
  if (!res.ok) throw new Error('Failed to end interaction')
}

// Free one slot on a team so the next waiting customer is served.
export async function advanceQueue(teamId) {
  const res = await apiFetch(`/api/teams/${teamId}/advance-queue`, { method: 'POST' })
  if (!res.ok) throw new Error('Failed to advance queue')
}
