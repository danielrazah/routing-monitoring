import { apiFetch } from './http.js'

// Customer side (public, no auth) — messages on the caller's own interaction.
export async function fetchPublicMessages(interactionId) {
  const res = await fetch(`/api/public/interactions/${interactionId}/messages`)
  if (!res.ok) throw new Error('Failed to load messages')
  return res.json()
}

export async function sendPublicMessage(interactionId, body) {
  const res = await fetch(`/api/public/interactions/${interactionId}/messages`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ body }),
  })
  if (!res.ok) throw new Error('Failed to send message')
  return res.json()
}

// Agent side (authenticated).
export async function fetchConversations() {
  const res = await apiFetch('/api/agent/conversations')
  if (!res.ok) throw new Error('Failed to load conversations')
  return res.json()
}

export async function fetchMessages(interactionId) {
  const res = await apiFetch(`/api/interactions/${interactionId}/messages`)
  if (!res.ok) throw new Error('Failed to load messages')
  return res.json()
}

export async function sendMessage(interactionId, body) {
  const res = await apiFetch(`/api/interactions/${interactionId}/messages`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ body }),
  })
  if (!res.ok) throw new Error('Failed to send message')
  return res.json()
}
