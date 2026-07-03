import { describe, it, expect, beforeEach, afterEach, vi } from 'vitest'
import { render, screen, fireEvent, waitFor, act } from '@testing-library/react'

vi.mock('@/shared/api/chat.js', () => ({
  fetchPublicMessages: vi.fn(),
  sendPublicMessage: vi.fn(),
  fetchMessages: vi.fn(),
  sendMessage: vi.fn(),
}))
vi.mock('@/shared/api/realtime.js', () => ({ connectChat: vi.fn(() => () => {}) }))

import ChatThread from '@/features/chat/ChatThread.jsx'
import { fetchPublicMessages, sendPublicMessage, fetchMessages } from '@/shared/api/chat.js'
import { connectChat } from '@/shared/api/realtime.js'

describe('ChatThread', () => {
  beforeEach(() => vi.clearAllMocks())
  afterEach(() => vi.restoreAllMocks())

  it('loads the thread and subscribes to the interaction', async () => {
    fetchPublicMessages.mockResolvedValue([
      { id: 'm1', sender: 'AGENT', body: 'Olá!', createdAt: '2026-01-01T00:00:00Z' },
    ])
    render(<ChatThread interactionId="i1" variant="public" />)

    await waitFor(() => expect(screen.getByText('Olá!')).toBeInTheDocument())
    expect(connectChat).toHaveBeenCalledWith('i1', expect.any(Function))
  })

  it('sends a message and shows it', async () => {
    fetchPublicMessages.mockResolvedValue([])
    sendPublicMessage.mockResolvedValue({
      id: 'm2', sender: 'CUSTOMER', body: 'preciso de ajuda', createdAt: '2026-01-01T00:01:00Z',
    })
    render(<ChatThread interactionId="i1" variant="public" />)

    fireEvent.change(screen.getByPlaceholderText('Type a message…'), {
      target: { value: 'preciso de ajuda' },
    })
    fireEvent.click(screen.getByRole('button', { name: 'Send' }))

    await waitFor(() => expect(sendPublicMessage).toHaveBeenCalledWith('i1', 'preciso de ajuda'))
    await waitFor(() => expect(screen.getByText('preciso de ajuda')).toBeInTheDocument())
  })

  it('appends a message pushed over WebSocket', async () => {
    fetchPublicMessages.mockResolvedValue([])
    let push
    connectChat.mockImplementation((id, onMessage) => {
      push = onMessage
      return () => {}
    })
    render(<ChatThread interactionId="i1" variant="public" />)
    await waitFor(() => expect(connectChat).toHaveBeenCalled())

    act(() => push({ id: 'm3', sender: 'AGENT', body: 'chegou pelo ws', createdAt: '2026-01-01T00:02:00Z' }))
    await waitFor(() => expect(screen.getByText('chegou pelo ws')).toBeInTheDocument())
  })

  it('renders the admin variant read-only, with sender labels and no composer', async () => {
    fetchMessages.mockResolvedValue([
      { id: 'm1', sender: 'CUSTOMER', body: 'oi', createdAt: '2026-01-01T00:00:00Z' },
      { id: 'm2', sender: 'AGENT', body: 'olá', createdAt: '2026-01-01T00:01:00Z' },
    ])
    render(<ChatThread interactionId="i9" variant="admin" />)

    await waitFor(() => expect(screen.getByText('oi')).toBeInTheDocument())
    expect(screen.getByText('Customer')).toBeInTheDocument()
    expect(screen.getByText('Agent')).toBeInTheDocument()
    // Read-only: no message box and no send button.
    expect(screen.queryByPlaceholderText('Type a message…')).toBeNull()
    expect(screen.queryByRole('button', { name: 'Send' })).toBeNull()
  })
})
