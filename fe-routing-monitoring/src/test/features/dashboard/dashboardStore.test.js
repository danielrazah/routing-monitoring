import { describe, it, expect, beforeEach, vi } from 'vitest'

vi.mock('@/features/dashboard/api.js', () => ({
  fetchSnapshot: vi.fn(),
  advanceQueue: vi.fn(),
}))

import { useDashboardStore } from '@/features/dashboard/dashboardStore.js'
import { fetchSnapshot, advanceQueue } from '@/features/dashboard/api.js'

describe('dashboardStore', () => {
  beforeEach(() => {
    useDashboardStore.setState({ teams: [], events: [], status: 'connecting' })
    vi.clearAllMocks()
  })

  it('setStatus updates the status', () => {
    useDashboardStore.getState().setStatus('connected')
    expect(useDashboardStore.getState().status).toBe('connected')
  })

  it('pushEvent prepends newest first and caps at 30', () => {
    const { pushEvent } = useDashboardStore.getState()
    for (let i = 0; i < 35; i++) pushEvent({ type: 'CREATED', n: i })

    const events = useDashboardStore.getState().events
    expect(events).toHaveLength(30)
    expect(events[0].n).toBe(34)
  })

  it('refresh loads teams from the snapshot', async () => {
    fetchSnapshot.mockResolvedValue({ teams: [{ id: 't1', name: 'Cards' }] })
    await useDashboardStore.getState().refresh()
    expect(useDashboardStore.getState().teams).toEqual([{ id: 't1', name: 'Cards' }])
  })

  it('refresh swallows a transient failure', async () => {
    fetchSnapshot.mockRejectedValue(new Error('boom'))
    await useDashboardStore.getState().refresh()
    expect(useDashboardStore.getState().teams).toEqual([])
  })

  it('serveNext advances the queue then refreshes', async () => {
    advanceQueue.mockResolvedValue(undefined)
    fetchSnapshot.mockResolvedValue({ teams: [{ id: 't1', name: 'Cards' }] })

    await useDashboardStore.getState().serveNext('t1')

    expect(advanceQueue).toHaveBeenCalledWith('t1')
    expect(useDashboardStore.getState().teams).toHaveLength(1)
  })
})
