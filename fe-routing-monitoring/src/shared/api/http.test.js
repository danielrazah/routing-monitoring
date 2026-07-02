import { describe, it, expect, beforeEach, afterEach, vi } from 'vitest'
import { apiFetch } from './http.js'
import { useAuthStore } from '../../features/auth/authStore.js'

describe('apiFetch', () => {
  beforeEach(() => {
    useAuthStore.setState({ token: null, username: null, roles: [] })
    localStorage.clear()
  })
  afterEach(() => vi.restoreAllMocks())

  it('attaches the Bearer token when present', async () => {
    useAuthStore.setState({ token: 'jwt-9', username: 'admin', roles: ['ADMIN'] })
    const fetchMock = vi.spyOn(globalThis, 'fetch').mockResolvedValue({ ok: true, status: 200 })

    await apiFetch('/api/dashboard')

    const [, options] = fetchMock.mock.calls[0]
    expect(options.headers.Authorization).toBe('Bearer jwt-9')
  })

  it('omits Authorization when there is no token', async () => {
    const fetchMock = vi.spyOn(globalThis, 'fetch').mockResolvedValue({ ok: true, status: 200 })

    await apiFetch('/api/dashboard')

    const [, options] = fetchMock.mock.calls[0]
    expect(options.headers.Authorization).toBeUndefined()
  })

  it('logs out and throws on 401/403', async () => {
    useAuthStore.setState({ token: 'jwt-9', username: 'admin', roles: ['ADMIN'] })
    vi.spyOn(globalThis, 'fetch').mockResolvedValue({ ok: false, status: 401 })

    await expect(apiFetch('/api/dashboard')).rejects.toThrow()
    expect(useAuthStore.getState().token).toBeNull()
  })
})
