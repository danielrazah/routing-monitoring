import { describe, it, expect, afterEach, vi } from 'vitest'
import { joinQueue, fetchInteractionStatus, endMyInteraction } from '@/shared/api/public.js'

describe('public api', () => {
  afterEach(() => vi.restoreAllMocks())

  it('joinQueue POSTs name + subject and returns json', async () => {
    vi.spyOn(globalThis, 'fetch').mockResolvedValue({
      ok: true,
      json: async () => ({ id: 'x', state: 'WAITING' }),
    })
    const res = await joinQueue('Ana', 'OTHER')

    expect(res).toEqual({ id: 'x', state: 'WAITING' })
    const [path, options] = globalThis.fetch.mock.calls[0]
    expect(path).toBe('/api/public/interactions')
    expect(options.method).toBe('POST')
    expect(JSON.parse(options.body)).toEqual({ customerName: 'Ana', subject: 'OTHER' })
  })

  it('fetchInteractionStatus GETs by id', async () => {
    vi.spyOn(globalThis, 'fetch').mockResolvedValue({
      ok: true,
      json: async () => ({ id: 'x', state: 'IN_SERVICE' }),
    })
    const res = await fetchInteractionStatus('x')

    expect(res.state).toBe('IN_SERVICE')
    expect(globalThis.fetch).toHaveBeenCalledWith('/api/public/interactions/x')
  })

  it('endMyInteraction POSTs to the public end endpoint', async () => {
    vi.spyOn(globalThis, 'fetch').mockResolvedValue({ ok: true })
    await endMyInteraction('x')
    const [path, options] = globalThis.fetch.mock.calls[0]
    expect(path).toBe('/api/public/interactions/x/end')
    expect(options.method).toBe('POST')
  })

  it('throws when the response is not ok', async () => {
    vi.spyOn(globalThis, 'fetch').mockResolvedValue({ ok: false })
    await expect(joinQueue('A', 'OTHER')).rejects.toThrow()
    await expect(fetchInteractionStatus('x')).rejects.toThrow()
    await expect(endMyInteraction('x')).rejects.toThrow()
  })
})
