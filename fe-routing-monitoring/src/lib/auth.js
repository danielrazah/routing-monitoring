// Minimal auth state kept in localStorage: the JWT plus who we are and our roles.

const KEY = 'rm.auth'

export function getAuth() {
  try {
    return JSON.parse(localStorage.getItem(KEY))
  } catch {
    return null
  }
}

export function getToken() {
  return getAuth()?.token ?? null
}

export function isAdmin() {
  return (getAuth()?.roles ?? []).includes('ADMIN')
}

export async function login(username, password) {
  const res = await fetch('/api/auth/login', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ username, password }),
  })
  if (!res.ok) throw new Error('Invalid credentials')
  const data = await res.json() // { token, username, roles, expiresInSeconds }
  localStorage.setItem(KEY, JSON.stringify(data))
  return data
}

export function logout() {
  localStorage.removeItem(KEY)
}
