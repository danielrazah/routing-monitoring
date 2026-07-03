import { describe, it, expect, beforeEach, afterEach, vi } from 'vitest'
import { render, screen, fireEvent, waitFor, act } from '@testing-library/react'

vi.mock('@/shared/api/public.js', () => ({
  joinQueue: vi.fn(),
  fetchInteractionStatus: vi.fn(),
  endMyInteraction: vi.fn(),
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
import { joinQueue, fetchInteractionStatus, endMyInteraction } from '@/shared/api/public.js'
import { connectDashboard } from '@/shared/api/realtime.js'

function join(name = 'Ana') {
  fireEvent.change(screen.getByPlaceholderText('e.g. John Carter'), { target: { value: name } })
  fireEvent.click(screen.getByRole('button', { name: 'Join the queue' }))
}

describe('CustomerPage', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    // Harmless default so the served-phase status poll never throws.
    fetchInteractionStatus.mockResolvedValue({ state: 'IN_SERVICE', assignedAgentName: null })
  })
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

  it('shows who is serving the customer', async () => {
    joinQueue.mockResolvedValue({ id: 'i3', state: 'IN_SERVICE', assignedAgentName: 'Carla' })
    render(<CustomerPage />)
    join('Bruno')

    await waitFor(() => expect(screen.getByText('Carla is serving you.')).toBeInTheDocument())
  })

  it('lets the customer end the conversation', async () => {
    joinQueue.mockResolvedValue({ id: 'i4', state: 'IN_SERVICE', assignedAgentName: 'Carla' })
    endMyInteraction.mockResolvedValue()
    render(<CustomerPage />)
    join('Bruno')

    await waitFor(() => expect(screen.getByText('You will be served now')).toBeInTheDocument())
    fireEvent.click(screen.getByRole('button', { name: 'End conversation' }))

    await waitFor(() => expect(endMyInteraction).toHaveBeenCalledWith('i4'))
    await waitFor(() => expect(screen.getByText('Conversation ended')).toBeInTheDocument())
  })

  it('moves to the ended state when the agent closes the conversation', async () => {
    joinQueue.mockResolvedValue({ id: 'i5', state: 'IN_SERVICE', assignedAgentName: 'Carla' })
    fetchInteractionStatus.mockResolvedValue({ state: 'ENDED', assignedAgentName: 'Carla' })
    render(<CustomerPage />)
    join('Bruno')

    await waitFor(() => expect(screen.getByText('Conversation ended')).toBeInTheDocument())
  })

  it('keeps the served view when the status poll fails', async () => {
    joinQueue.mockResolvedValue({ id: 'i6', state: 'IN_SERVICE', assignedAgentName: 'Carla' })
    fetchInteractionStatus.mockRejectedValue(new Error('offline'))
    render(<CustomerPage />)
    join('Bruno')

    await waitFor(() => expect(screen.getByText('You will be served now')).toBeInTheDocument())
    // The transient error is swallowed; the customer stays in the served view.
    expect(screen.getByRole('button', { name: 'End conversation' })).toBeInTheDocument()
  })
})
