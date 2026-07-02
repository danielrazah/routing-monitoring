import { describe, it, expect, beforeEach, afterEach, vi } from 'vitest'
import { useAuthStore } from './authStore.js'

describe('authStore', () => {
  beforeEach(() => {
    useAuthStore.getState().logout()
    localStorage.clear()
  })

  afterEach(() => {
    vi.restoreAllMocks()
  })

  it('stores token, username and roles on a successful login', async () => {
    vi.spyOn(globalThis, 'fetch').mockResolvedValue({
      ok: true,
      json: async () => ({ token: 'jwt-123', username: 'admin', roles: ['ADMIN'] }),
    })

    await useAuthStore.getState().login('admin', 'admin123')

    const state = useAuthStore.getState()
    expect(state.token).toBe('jwt-123')
    expect(state.username).toBe('admin')
    expect(state.roles).toEqual(['ADMIN'])
  })

  it('throws and keeps no token on invalid credentials', async () => {
    vi.spyOn(globalThis, 'fetch').mockResolvedValue({ ok: false })

    await expect(useAuthStore.getState().login('admin', 'wrong')).rejects.toThrow()
    expect(useAuthStore.getState().token).toBeNull()
  })

  it('clears everything on logout', async () => {
    vi.spyOn(globalThis, 'fetch').mockResolvedValue({
      ok: true,
      json: async () => ({ token: 'jwt-123', username: 'viewer', roles: ['VIEWER'] }),
    })
    await useAuthStore.getState().login('viewer', 'viewer123')

    useAuthStore.getState().logout()

    expect(useAuthStore.getState().token).toBeNull()
    expect(useAuthStore.getState().roles).toEqual([])
  })
})
