import { apiFetch } from './http.js'

// ADMIN-only cross-team operations.

// Every live customer↔agent conversation, so an admin can watch them all at once.
export async function fetchAdminConversations() {
  const res = await apiFetch('/api/admin/conversations')
  if (!res.ok) throw new Error('Failed to load conversations')
  return res.json()
}

// End every open interaction and empty every queue — a convenience for testing.
export async function resetBoard() {
  const res = await apiFetch('/api/admin/reset', { method: 'POST' })
  if (!res.ok) throw new Error('Failed to reset the board')
}
