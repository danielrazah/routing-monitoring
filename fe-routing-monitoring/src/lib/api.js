import { Client } from '@stomp/stompjs'

// All calls are same-origin: Vite proxies to the backend in dev, nginx does in prod.

export async function fetchSnapshot() {
  const res = await fetch('/api/dashboard')
  if (!res.ok) throw new Error('Failed to load dashboard')
  return res.json()
}

export async function createInteraction(customerName, subject) {
  const res = await fetch('/api/interactions', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ customerName, subject }),
  })
  if (!res.ok) throw new Error('Failed to create interaction')
  return res.json()
}

export async function endInteraction(interactionId) {
  const res = await fetch(`/api/interactions/${interactionId}/end`, { method: 'POST' })
  if (!res.ok) throw new Error('Failed to end interaction')
}

// Free one slot on a team so the next waiting customer is served.
export async function advanceQueue(teamId) {
  const res = await fetch(`/api/teams/${teamId}/advance-queue`, { method: 'POST' })
  if (!res.ok) throw new Error('Failed to advance queue')
}

/**
 * Subscribe to live distribution events over native WebSocket (STOMP).
 * Returns a disconnect function. onMessage receives each DashboardMessage;
 * onStatus reports 'connected' or 'offline'.
 *
 * If the browser refuses the native WebSocket (e.g. Safari on ws://localhost), we stop
 * retrying after the first failure and let the caller fall back to polling — no error loop.
 */
export function connectDashboard(onMessage, onStatus) {
  const scheme = window.location.protocol === 'https:' ? 'wss' : 'ws'
  let everConnected = false

  const client = new Client({
    brokerURL: `${scheme}://${window.location.host}/ws`,
    reconnectDelay: 4000,
    onConnect: () => {
      everConnected = true
      onStatus?.('connected')
      client.subscribe('/topic/dashboard', (frame) => onMessage(JSON.parse(frame.body)))
    },
    onWebSocketClose: () => {
      onStatus?.('offline')
      if (!everConnected) client.deactivate()
    },
  })

  client.activate()
  return () => client.deactivate()
}
