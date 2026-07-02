import { describe, it, expect, beforeEach, vi } from 'vitest'

vi.mock('@/shared/api/http.js', () => ({ apiFetch: vi.fn() }))

import { fetchSnapshot, createInteraction, endInteraction, advanceQueue } from '@/features/dashboard/api.js'
import { apiFetch } from '@/shared/api/http.js'

describe('dashboard api', () => {
  beforeEach(() => vi.clearAllMocks())

  it('fetchSnapshot GETs the dashboard and returns json', async () => {
    apiFetch.mockResolvedValue({ ok: true, json: async () => ({ teams: [] }) })
    expect(await fetchSnapshot()).toEqual({ teams: [] })
    expect(apiFetch).toHaveBeenCalledWith('/api/dashboard')
  })

  it('createInteraction POSTs the payload', async () => {
    apiFetch.mockResolvedValue({ ok: true, json: async () => ({ id: '1' }) })
    const result = await createInteraction('Ana', 'CARD_ISSUE')

    expect(result).toEqual({ id: '1' })
    const [path, options] = apiFetch.mock.calls[0]
    expect(path).toBe('/api/interactions')
    expect(options.method).toBe('POST')
    expect(JSON.parse(options.body)).toEqual({ customerName: 'Ana', subject: 'CARD_ISSUE' })
  })

  it('endInteraction POSTs to the end path', async () => {
    apiFetch.mockResolvedValue({ ok: true })
    await endInteraction('42')
    expect(apiFetch).toHaveBeenCalledWith('/api/interactions/42/end', { method: 'POST' })
  })

  it('advanceQueue POSTs to the advance path', async () => {
    apiFetch.mockResolvedValue({ ok: true })
    await advanceQueue('t1')
    expect(apiFetch).toHaveBeenCalledWith('/api/teams/t1/advance-queue', { method: 'POST' })
  })

  it('throws when the response is not ok', async () => {
    apiFetch.mockResolvedValue({ ok: false })
    await expect(fetchSnapshot()).rejects.toThrow()
    await expect(createInteraction('A', 'OTHER')).rejects.toThrow()
    await expect(endInteraction('1')).rejects.toThrow()
    await expect(advanceQueue('t1')).rejects.toThrow()
  })
})
