import { describe, it, expect } from 'vitest'
import { render, screen } from '@testing-library/react'
import EventFeed from '@/features/dashboard/components/EventFeed.jsx'

const teams = [{ id: 't1', name: 'Cards', agents: [{ id: 'a1', name: 'Ana' }] }]

function event(type, extra = {}) {
  return { type, interactionId: 'abcdef1234', teamId: 't1', agentId: 'a1', occurredAt: '2026-01-01T10:00:00Z', ...extra }
}

describe('EventFeed', () => {
  it('renders a readable line for each event type, resolving names', () => {
    const events = [
      event('CREATED', { subject: 'CARD_ISSUE' }),
      event('ASSIGNED'),
      event('QUEUED'),
      event('ENDED'),
    ]
    render(<EventFeed events={events} teams={teams} live={true} />)

    expect(screen.getByText('New contact')).toBeInTheDocument()
    expect(screen.getByText('Assigned')).toBeInTheDocument()
    expect(screen.getByText('Queued')).toBeInTheDocument()
    expect(screen.getByText('Ended')).toBeInTheDocument()
    expect(screen.getAllByText(/Ana/).length).toBeGreaterThan(0)
  })

  it('falls back to generic names for unknown ids', () => {
    render(<EventFeed events={[event('ASSIGNED', { teamId: 'zzz', agentId: 'zzz' })]} teams={teams} live={true} />)
    expect(screen.getByText(/an agent/)).toBeInTheDocument()
  })

  it('shows the waiting message when live with no events', () => {
    render(<EventFeed events={[]} teams={teams} live={true} />)
    expect(screen.getByText('Waiting for activity…')).toBeInTheDocument()
  })

  it('shows the polling hint when not live with no events', () => {
    render(<EventFeed events={[]} teams={[]} live={false} />)
    expect(screen.getByText(/polling for counts/i)).toBeInTheDocument()
  })
})
