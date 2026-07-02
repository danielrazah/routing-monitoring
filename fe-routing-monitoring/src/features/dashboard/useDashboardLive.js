import { useEffect } from 'react'
import { useDashboardStore } from './dashboardStore.js'
import { connectDashboard } from '../../shared/api/realtime.js'

// How often we refresh counters when the live WebSocket isn't available.
const POLL_INTERVAL_MS = 2500

/**
 * Wires the dashboard to live data: an initial load, a WebSocket subscription for instant
 * updates, and a polling fallback that works in any browser. Cleans everything up on unmount.
 */
export function useDashboardLive() {
  useEffect(() => {
    const { refresh, pushEvent, setStatus } = useDashboardStore.getState()

    refresh()

    const disconnect = connectDashboard(
      (message) => {
        pushEvent(message)
        refresh()
      },
      setStatus,
    )

    const poll = setInterval(refresh, POLL_INTERVAL_MS)

    return () => {
      disconnect()
      clearInterval(poll)
    }
  }, [])
}
