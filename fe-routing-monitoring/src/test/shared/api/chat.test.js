import { describe, it, expect, beforeEach, afterEach, vi } from 'vitest'

vi.mock('@/shared/api/http.js', () => ({ apiFetch: vi.fn() }))

import {
  fetchPublicMessages,
  sendPublicMessage,
  fetchConversations,
  fetchMessages,
  sendMessage,
} from '@/shared/api/chat.js'
import { apiFetch } from '@/shared/api/http.js'

describe('chat api', () => {
  beforeEach(() => vi.clearAllMocks())
  afterEach(() => vi.restoreAllMocks())

  it('public send/fetch hit the public endpoints with no auth', async () => {
    const fetchSpy = vi.spyOn(globalThis, 'fetch').mockResolvedValue({
      ok: true,
      json: async () => ([{ id: 'm1', sender: 'CUSTOMER', body: 'oi' }]),
    })

    await fetchPublicMessages('i1')
    expect(fetchSpy).toHaveBeenCalledWith('/api/public/interactions/i1/messages')

    await sendPublicMessage('i1', 'olá')
    const [path, options] = fetchSpy.mock.calls[1]
    expect(path).toBe('/api/public/interactions/i1/messages')
    expect(options.method).toBe('POST')
    expect(JSON.parse(options.body)).toEqual({ body: 'olá' })
  })

  it('agent calls go through apiFetch (authenticated)', async () => {
    apiFetch.mockResolvedValue({ ok: true, json: async () => [] })

    await fetchConversations()
    expect(apiFetch).toHaveBeenCalledWith('/api/agent/conversations')

    await fetchMessages('i1')
    expect(apiFetch).toHaveBeenCalledWith('/api/interactions/i1/messages')

    await sendMessage('i1', 'resposta')
    const [path, options] = apiFetch.mock.calls[2]
    expect(path).toBe('/api/interactions/i1/messages')
    expect(options.method).toBe('POST')
    expect(JSON.parse(options.body)).toEqual({ body: 'resposta' })
  })

  it('throws when a response is not ok', async () => {
    vi.spyOn(globalThis, 'fetch').mockResolvedValue({ ok: false })
    apiFetch.mockResolvedValue({ ok: false })
    await expect(fetchPublicMessages('i1')).rejects.toThrow()
    await expect(sendPublicMessage('i1', 'x')).rejects.toThrow()
    await expect(fetchConversations()).rejects.toThrow()
  })
})
