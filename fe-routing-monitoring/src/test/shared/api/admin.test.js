import { describe, it, expect, beforeEach, afterEach, vi } from 'vitest'

vi.mock('@/shared/api/http.js', () => ({ apiFetch: vi.fn() }))

import { fetchAdminConversations, resetBoard } from '@/shared/api/admin.js'
import { apiFetch } from '@/shared/api/http.js'

describe('admin api', () => {
  beforeEach(() => vi.clearAllMocks())
  afterEach(() => vi.restoreAllMocks())

  it('fetchAdminConversations GETs the admin endpoint (authenticated)', async () => {
    apiFetch.mockResolvedValue({ ok: true, json: async () => [{ interactionId: 'i1' }] })
    const res = await fetchAdminConversations()
    expect(res).toEqual([{ interactionId: 'i1' }])
    expect(apiFetch).toHaveBeenCalledWith('/api/admin/conversations')
  })

  it('resetBoard POSTs to the reset endpoint', async () => {
    apiFetch.mockResolvedValue({ ok: true })
    await resetBoard()
    const [path, options] = apiFetch.mock.calls[0]
    expect(path).toBe('/api/admin/reset')
    expect(options.method).toBe('POST')
  })

  it('throws when a response is not ok', async () => {
    apiFetch.mockResolvedValue({ ok: false })
    await expect(fetchAdminConversations()).rejects.toThrow()
    await expect(resetBoard()).rejects.toThrow()
  })
})
