import { create } from 'zustand'
import { advanceQueue, fetchSnapshot } from './api.js'

/**
 * Dashboard state: the team snapshot, the live event feed and the connection status.
 * The WebSocket/polling lifecycle lives in useDashboardLive; this store just holds the
 * data and the actions that change it.
 */
export const useDashboardStore = create((set, get) => ({
  teams: [],
  events: [],
  status: 'connecting',

  setStatus: (status) => set({ status }),

  pushEvent: (message) => set((state) => ({ events: [message, ...state.events].slice(0, 30) })),

  async refresh() {
    try {
      const snapshot = await fetchSnapshot()
      set({ teams: snapshot.teams })
    } catch {
      // ignore a transient failure; the next tick/event will refresh
    }
  },

  async serveNext(teamId) {
    await advanceQueue(teamId)
    await get().refresh()
  },
}))
