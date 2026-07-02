import { describe, it, expect } from 'vitest'
import { render, screen } from '@testing-library/react'
import EventFeed from './EventFeed.jsx'

const teams = [{ id: 't1', name: 'Cards', agents: [{ id: 'a1', name: 'Ana' }] }]

describe('EventFeed', () => {
  it('renders a readable line resolving team and agent names', () => {
    const events = [
      { type: 'ASSIGNED', interactionId: 'abcdef1234', teamId: 't1', agentId: 'a1', occurredAt: '2026-01-01T10:00:00Z' },
    ]
    render(<EventFeed events={events} teams={teams} live={true} />)

    expect(screen.getByText('Assigned')).toBeInTheDocument()
    expect(screen.getByText(/Ana/)).toBeInTheDocument()
  })

  it('shows the polling hint when not live and there are no events', () => {
    render(<EventFeed events={[]} teams={[]} live={false} />)
    expect(screen.getByText(/polling for counts/i)).toBeInTheDocument()
  })
})
