import { describe, it, expect, beforeEach } from 'vitest'
import { render, screen } from '@testing-library/react'
import TeamCard from './TeamCard.jsx'
import { useAuthStore } from '../../auth/authStore.js'

const team = {
  id: 't1',
  name: 'Cards',
  waiting: 1,
  agents: [{ id: 'a1', name: 'Ana', currentLoad: 3, maxConcurrent: 3 }],
  serving: ['Maria'],
  queue: ['Joao'],
}

describe('TeamCard', () => {
  beforeEach(() => {
    useAuthStore.setState({ token: 'x', username: 'u', roles: [] })
  })

  it('shows serving and queued customer names', () => {
    render(<TeamCard team={team} />)
    expect(screen.getByText('Cards')).toBeInTheDocument()
    expect(screen.getByText('Maria')).toBeInTheDocument() // in service
    expect(screen.getByText('Joao')).toBeInTheDocument() // waiting
  })

  it('offers the Serve next button to an admin', () => {
    useAuthStore.setState({ token: 'x', username: 'admin', roles: ['ADMIN'] })
    render(<TeamCard team={team} />)
    expect(screen.getByText('Serve next')).toBeInTheDocument()
  })

  it('hides the Serve next button from a viewer', () => {
    useAuthStore.setState({ token: 'x', username: 'viewer', roles: ['VIEWER'] })
    render(<TeamCard team={team} />)
    expect(screen.queryByText('Serve next')).toBeNull()
  })
})
