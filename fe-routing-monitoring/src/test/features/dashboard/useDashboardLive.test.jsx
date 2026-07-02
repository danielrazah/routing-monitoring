import { describe, it, expect, beforeEach, afterEach, vi } from 'vitest'
import { renderHook } from '@testing-library/react'

vi.mock('@/shared/api/realtime.js', () => ({ connectDashboard: vi.fn() }))

import { useDashboardLive } from '@/features/dashboard/useDashboardLive.js'
import { useDashboardStore } from '@/features/dashboard/dashboardStore.js'
import { connectDashboard } from '@/shared/api/realtime.js'

describe('useDashboardLive', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    vi.useFakeTimers()
  })
  afterEach(() => vi.useRealTimers())

  it('refreshes on mount, connects, polls, and cleans up on unmount', () => {
    const refresh = vi.fn().mockResolvedValue(undefined)
    useDashboardStore.setState({ refresh })
    const disconnect = vi.fn()
    connectDashboard.mockReturnValue(disconnect)

    const { unmount } = renderHook(() => useDashboardLive())

    expect(refresh).toHaveBeenCalledTimes(1) // initial load
    expect(connectDashboard).toHaveBeenCalledTimes(1)

    // A live event feeds the timeline and triggers a refresh.
    const onMessage = connectDashboard.mock.calls[0][0]
    onMessage({ type: 'CREATED' })
    expect(refresh).toHaveBeenCalledTimes(2)

    vi.advanceTimersByTime(2500) // one polling tick
    expect(refresh).toHaveBeenCalledTimes(3)

    unmount()
    expect(disconnect).toHaveBeenCalled()

    vi.advanceTimersByTime(5000) // no more polling after unmount
    expect(refresh).toHaveBeenCalledTimes(3)
  })
})
