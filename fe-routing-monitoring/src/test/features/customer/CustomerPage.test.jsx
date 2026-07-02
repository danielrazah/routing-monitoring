import { describe, it, expect, beforeEach, afterEach, vi } from 'vitest'
import { render, screen, fireEvent, waitFor, act } from '@testing-library/react'

vi.mock('@/shared/api/public.js', () => ({
  joinQueue: vi.fn(),
  fetchInteractionStatus: vi.fn(),
}))
vi.mock('@/shared/api/realtime.js', () => ({
  connectDashboard: vi.fn(() => () => {}),
  connectChat: vi.fn(() => () => {}),
}))
vi.mock('@/shared/api/chat.js', () => ({
  fetchPublicMessages: vi.fn().mockResolvedValue([]),
  sendPublicMessage: vi.fn(),
  fetchMessages: vi.fn().mockResolvedValue([]),
  sendMessage: vi.fn(),
}))

import CustomerPage from '@/features/customer/CustomerPage.jsx'
import { joinQueue } from '@/shared/api/public.js'
import { connectDashboard } from '@/shared/api/realtime.js'

function join(name = 'Ana') {
  fireEvent.change(screen.getByPlaceholderText('e.g. John Carter'), { target: { value: name } })
  fireEvent.click(screen.getByRole('button', { name: 'Join the queue' }))
}

describe('CustomerPage', () => {
  beforeEach(() => vi.clearAllMocks())
  afterEach(() => vi.restoreAllMocks())

  it('joins the queue and shows the waiting state', async () => {
    joinQueue.mockResolvedValue({ id: 'i1', state: 'WAITING' })
    render(<CustomerPage />)
    join()

    await waitFor(() => expect(screen.getByText("You're in line")).toBeInTheDocument())
    expect(joinQueue).toHaveBeenCalledWith('Ana', 'CARD_ISSUE')
    expect(connectDashboard).toHaveBeenCalled()
  })

  it('welcomes the customer when its own interaction is assigned over WebSocket', async () => {
    joinQueue.mockResolvedValue({ id: 'i1', state: 'WAITING' })
    let onMessage
    connectDashboard.mockImplementation((handler) => {
      onMessage = handler
      return () => {}
    })
    render(<CustomerPage />)
    join()
    await waitFor(() => expect(screen.getByText("You're in line")).toBeInTheDocument())

    // An event for a different interaction must not trigger the welcome.
    act(() => onMessage({ type: 'ASSIGNED', interactionId: 'someone-else' }))
    expect(screen.queryByText('You will be served now')).toBeNull()

    // Our interaction being assigned does.
    act(() => onMessage({ type: 'ASSIGNED', interactionId: 'i1' }))
    await waitFor(() => expect(screen.getByText('You will be served now')).toBeInTheDocument())
  })

  it('goes straight to the welcome when created already in service', async () => {
    joinQueue.mockResolvedValue({ id: 'i2', state: 'IN_SERVICE' })
    render(<CustomerPage />)
    join('Bruno')

    await waitFor(() => expect(screen.getByText('You will be served now')).toBeInTheDocument())
  })

  it('shows an error when joining fails', async () => {
    joinQueue.mockRejectedValue(new Error('nope'))
    render(<CustomerPage />)
    join()

    await waitFor(() =>
      expect(screen.getByText('Could not join the queue. Try again.')).toBeInTheDocument())
  })
})
