// Public (no-login) API for the customer-facing queue screen. Unlike shared/api/http.js,
// these calls carry no token and never trigger a logout — the customer isn't authenticated.

export async function joinQueue(customerName, subject) {
  const res = await fetch('/api/public/interactions', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ customerName, subject }),
  })
  if (!res.ok) throw new Error('Failed to join the queue')
  return res.json()
}

export async function fetchInteractionStatus(id) {
  const res = await fetch(`/api/public/interactions/${id}`)
  if (!res.ok) throw new Error('Failed to fetch status')
  return res.json()
}
