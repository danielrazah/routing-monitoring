import { describe, it, expect, beforeEach, afterEach, vi } from 'vitest'
import { render, screen, waitFor, fireEvent, act } from '@testing-library/react'

vi.mock('@/shared/api/chat.js', () => ({
  fetchConversations: vi.fn(),
  endConversation: vi.fn(),
}))
vi.mock('@/shared/api/realtime.js', () => ({ connectChat: vi.fn(() => () => {}) }))
// Stub the thread so this test focuses on the conversation list/selection.
vi.mock('@/features/chat/ChatThread.jsx', () => ({
  default: ({ interactionId }) => <div>thread:{interactionId}</div>,
}))

import AgentConversations from '@/features/dashboard/AgentConversations.jsx'
import { fetchConversations, endConversation } from '@/shared/api/chat.js'
import { connectChat } from '@/shared/api/realtime.js'

const TWO = [
  { interactionId: 'i1', customerName: 'Maria' },
  { interactionId: 'i2', customerName: 'Joao' },
]

describe('AgentConversations', () => {
  beforeEach(() => vi.clearAllMocks())
  afterEach(() => vi.restoreAllMocks())

  it('lists the customers in service and opens the first dialog', async () => {
    fetchConversations.mockResolvedValue(TWO)
    render(<AgentConversations />)

    await waitFor(() => expect(screen.getByText('Maria')).toBeInTheDocument())
    expect(screen.getByText('Joao')).toBeInTheDocument()
    await waitFor(() => expect(screen.getByText('thread:i1')).toBeInTheDocument())
  })

  it('shows an empty state when nobody is in service', async () => {
    fetchConversations.mockResolvedValue([])
    render(<AgentConversations />)
    await waitFor(() =>
      expect(screen.getByText('No one in service right now.')).toBeInTheDocument())
  })

  it('ends the selected conversation and refreshes the list', async () => {
    fetchConversations.mockResolvedValueOnce(TWO).mockResolvedValue([])
    endConversation.mockResolvedValue()
    render(<AgentConversations />)

    await waitFor(() => expect(screen.getByText('thread:i1')).toBeInTheDocument())
    fireEvent.click(screen.getByRole('button', { name: 'End conversation' }))

    await waitFor(() => expect(endConversation).toHaveBeenCalledWith('i1'))
    await waitFor(() =>
      expect(screen.getByText('No one in service right now.')).toBeInTheDocument())
  })

  it('blinks a customer name when a new message arrives for a dialog not open', async () => {
    fetchConversations.mockResolvedValue(TWO)
    const handlers = {}
    connectChat.mockImplementation((id, onMessage) => {
      handlers[id] = onMessage
      return () => {}
    })
    render(<AgentConversations />)

    // i1 is auto-selected; a customer message on i2 (not open) should flag it.
    await waitFor(() => expect(handlers.i2).toBeTypeOf('function'))
    act(() => handlers.i2({ sender: 'CUSTOMER' }))

    await waitFor(() =>
      expect(screen.getByRole('button', { name: 'New message from Joao' })).toBeInTheDocument())
    // A message on the already-open dialog does not blink it.
    act(() => handlers.i1({ sender: 'CUSTOMER' }))
    expect(screen.queryByRole('button', { name: 'New message from Maria' })).toBeNull()
  })
})
