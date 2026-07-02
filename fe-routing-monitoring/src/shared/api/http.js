import { useAuthStore } from '../../features/auth/authStore.js'

/**
 * fetch wrapper for the backend API: attaches the Bearer token and, on 401/403,
 * logs out (which sends the app back to the login screen). Same-origin, so the
 * Vite/nginx proxy forwards to the backend.
 */
export async function apiFetch(path, options = {}) {
  const token = useAuthStore.getState().token
  const headers = { ...(options.headers ?? {}) }
  if (token) headers.Authorization = `Bearer ${token}`

  const res = await fetch(path, { ...options, headers })
  if (res.status === 401 || res.status === 403) {
    useAuthStore.getState().logout()
    throw new Error('Not authorized')
  }
  return res
}
