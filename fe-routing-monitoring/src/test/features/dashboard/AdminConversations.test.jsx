import { describe, it, expect, beforeEach, afterEach, vi } from 'vitest'
import { render, screen, waitFor, fireEvent } from '@testing-library/react'

vi.mock('@/shared/api/admin.js', () => ({
  fetchAdminConversations: vi.fn(),
  resetBoard: vi.fn(),
}))
// Stub the thread so this test focuses on the list, selection and reset.
vi.mock('@/features/chat/ChatThread.jsx', () => ({
  default: ({ interactionId, variant }) => <div>thread:{variant}:{interactionId}</div>,
}))

import AdminConversations from '@/features/dashboard/AdminConversations.jsx'
import { fetchAdminConversations, resetBoard } from '@/shared/api/admin.js'

const CONVERSATIONS = [
  { interactionId: 'i1', customerName: 'Maria', agentName: 'Carla', teamName: 'Loans' },
  { interactionId: 'i2', customerName: 'Joao', agentName: 'Ana', teamName: 'Cards' },
]

describe('AdminConversations', () => {
  beforeEach(() => vi.clearAllMocks())
  afterEach(() => vi.restoreAllMocks())

  it('lists every live conversation and opens the first as a read-only thread', async () => {
    fetchAdminConversations.mockResolvedValue(CONVERSATIONS)
    render(<AdminConversations />)

    await waitFor(() => expect(screen.getByText('Maria')).toBeInTheDocument())
    expect(screen.getByText('Joao')).toBeInTheDocument()
    await waitFor(() => expect(screen.getByText('thread:admin:i1')).toBeInTheDocument())
  })

  it('shows an empty state when nothing is in progress', async () => {
    fetchAdminConversations.mockResolvedValue([])
    render(<AdminConversations />)
    await waitFor(() =>
      expect(screen.getByText('No conversations in progress.')).toBeInTheDocument())
  })

  it('resets the board after confirmation', async () => {
    fetchAdminConversations.mockResolvedValue(CONVERSATIONS)
    resetBoard.mockResolvedValue()
    vi.spyOn(window, 'confirm').mockReturnValue(true)
    render(<AdminConversations />)

    await waitFor(() => expect(screen.getByText('Maria')).toBeInTheDocument())
    fireEvent.click(screen.getByRole('button', { name: 'End all' }))

    await waitFor(() => expect(resetBoard).toHaveBeenCalled())
    await waitFor(() =>
      expect(screen.getByText('No conversations in progress.')).toBeInTheDocument())
  })

  it('does not reset when the confirmation is dismissed', async () => {
    fetchAdminConversations.mockResolvedValue(CONVERSATIONS)
    vi.spyOn(window, 'confirm').mockReturnValue(false)
    render(<AdminConversations />)

    await waitFor(() => expect(screen.getByText('Maria')).toBeInTheDocument())
    fireEvent.click(screen.getByRole('button', { name: 'End all' }))
    expect(resetBoard).not.toHaveBeenCalled()
  })
})
