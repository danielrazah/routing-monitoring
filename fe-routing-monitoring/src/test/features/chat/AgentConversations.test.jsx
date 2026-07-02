import { describe, it, expect, beforeEach, afterEach, vi } from 'vitest'
import { render, screen, waitFor } from '@testing-library/react'

vi.mock('@/shared/api/chat.js', () => ({ fetchConversations: vi.fn() }))
// Stub the thread so this test focuses on the conversation list/selection.
vi.mock('@/features/chat/ChatThread.jsx', () => ({
  default: ({ interactionId }) => <div>thread:{interactionId}</div>,
}))

import AgentConversations from '@/features/dashboard/AgentConversations.jsx'
import { fetchConversations } from '@/shared/api/chat.js'

describe('AgentConversations', () => {
  beforeEach(() => vi.clearAllMocks())
  afterEach(() => vi.restoreAllMocks())

  it('lists the customers in service and opens the first dialog', async () => {
    fetchConversations.mockResolvedValue([
      { interactionId: 'i1', customerName: 'Maria' },
      { interactionId: 'i2', customerName: 'Joao' },
    ])
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
})
