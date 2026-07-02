import { describe, it, expect, beforeEach, vi } from 'vitest'

const activate = vi.fn()
const deactivate = vi.fn()
const subscribe = vi.fn()
let capturedConfig

vi.mock('@stomp/stompjs', () => ({
  Client: vi.fn(function Client(config) {
    capturedConfig = config
    this.activate = activate
    this.deactivate = deactivate
    this.subscribe = subscribe
  }),
}))

import { connectDashboard } from '@/shared/api/realtime.js'

describe('connectDashboard', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    capturedConfig = undefined
  })

  it('activates, subscribes on connect and forwards parsed messages', () => {
    const onMessage = vi.fn()
    const onStatus = vi.fn()
    const disconnect = connectDashboard(onMessage, onStatus)

    expect(activate).toHaveBeenCalled()
    expect(capturedConfig.brokerURL).toMatch(/\/ws$/)

    subscribe.mockImplementation((topic, cb) => cb({ body: JSON.stringify({ type: 'CREATED' }) }))
    capturedConfig.onConnect()

    expect(onStatus).toHaveBeenCalledWith('connected')
    expect(subscribe).toHaveBeenCalledWith('/topic/dashboard', expect.any(Function))
    expect(onMessage).toHaveBeenCalledWith({ type: 'CREATED' })

    disconnect()
    expect(deactivate).toHaveBeenCalled()
  })

  it('goes offline and stops retrying if it never connected', () => {
    const onStatus = vi.fn()
    connectDashboard(vi.fn(), onStatus)

    capturedConfig.onWebSocketClose()

    expect(onStatus).toHaveBeenCalledWith('offline')
    expect(deactivate).toHaveBeenCalled()
  })

  it('keeps retrying after a close once it has connected', () => {
    const onStatus = vi.fn()
    connectDashboard(vi.fn(), onStatus)
    capturedConfig.onConnect()
    deactivate.mockClear()

    capturedConfig.onWebSocketClose()

    expect(onStatus).toHaveBeenLastCalledWith('offline')
    expect(deactivate).not.toHaveBeenCalled()
  })
})
