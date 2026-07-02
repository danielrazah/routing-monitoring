import { create } from 'zustand'
import { persist } from 'zustand/middleware'

/**
 * Auth state: the JWT plus who we are and our roles. Persisted to localStorage so a
 * refresh keeps you logged in. Login uses a raw fetch (no token yet); every other call
 * goes through shared/api/http, which reads the token from here.
 */
export const useAuthStore = create(
  persist(
    (set) => ({
      token: null,
      username: null,
      roles: [],

      async login(username, password) {
        const res = await fetch('/api/auth/login', {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({ username, password }),
        })
        if (!res.ok) throw new Error('Invalid credentials')
        const data = await res.json() // { token, username, roles, expiresInSeconds }
        set({ token: data.token, username: data.username, roles: data.roles })
        return data
      },

      logout() {
        set({ token: null, username: null, roles: [] })
      },
    }),
    {
      name: 'routing-monitoring-auth',
      partialize: (state) => ({ token: state.token, username: state.username, roles: state.roles }),
    },
  ),
)
