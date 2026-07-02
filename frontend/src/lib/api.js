import { Client } from '@stomp/stompjs'
import SockJS from 'sockjs-client'

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

/**
 * Subscribe to live distribution events. Returns a disconnect function.
 * onMessage receives each DashboardMessage; onStatus reports connection state.
 */
export function connectDashboard(onMessage, onStatus) {
  const client = new Client({
    webSocketFactory: () => new SockJS('/ws'),
    reconnectDelay: 3000,
    onConnect: () => {
      onStatus?.('connected')
      client.subscribe('/topic/dashboard', (frame) => onMessage(JSON.parse(frame.body)))
    },
    onWebSocketClose: () => onStatus?.('disconnected'),
  })
  client.activate()
  return () => client.deactivate()
}
