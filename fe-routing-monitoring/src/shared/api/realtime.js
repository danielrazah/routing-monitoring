import { Client } from '@stomp/stompjs'

/**
 * Subscribe to live distribution events over native WebSocket (STOMP).
 * Returns a disconnect function. onMessage receives each DashboardMessage;
 * onStatus reports 'connected' or 'offline'.
 *
 * If the browser refuses the native WebSocket (e.g. Safari on ws://localhost), we stop
 * retrying after the first failure and let the caller fall back to polling — no error loop.
 * The broadcast is open (no token needed); the dashboard reads carry the token themselves.
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

/**
 * Subscribe to one interaction's chat thread over the same native WebSocket.
 * onMessage receives each new MessageResponse. Returns a disconnect function.
 * Open like the dashboard broadcast, so the customer (no login) can use it too.
 */
export function connectChat(interactionId, onMessage) {
  const scheme = window.location.protocol === 'https:' ? 'wss' : 'ws'
  let everConnected = false

  const client = new Client({
    brokerURL: `${scheme}://${window.location.host}/ws`,
    reconnectDelay: 4000,
    onConnect: () => {
      everConnected = true
      // Dot, not slash: RabbitMQ's STOMP broker rejects extra path segments, so the id
      // rides in the routing key ("/topic/chat.<id>") to match the backend's destination.
      client.subscribe(`/topic/chat.${interactionId}`, (frame) => onMessage(JSON.parse(frame.body)))
    },
    onWebSocketClose: () => {
      if (!everConnected) client.deactivate()
    },
  })

  client.activate()
  return () => client.deactivate()
}
