import { describe, it, expect, beforeEach, vi } from 'vitest'
import { render, screen, fireEvent, waitFor } from '@testing-library/react'
import TeamCard from '@/features/dashboard/components/TeamCard.jsx'
import { useAuthStore } from '@/features/auth/authStore.js'
import { useDashboardStore } from '@/features/dashboard/dashboardStore.js'

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
    useDashboardStore.setState({ serveNext: vi.fn().mockResolvedValue(undefined) })
  })

  it('shows serving and queued customer names', () => {
    render(<TeamCard team={team} />)
    expect(screen.getByText('Cards')).toBeInTheDocument()
    expect(screen.getByText('Maria')).toBeInTheDocument()
    expect(screen.getByText('Joao')).toBeInTheDocument()
  })

  it('renders "nobody" when there is no one serving or waiting', () => {
    const idle = { ...team, waiting: 0, serving: [], queue: [] }
    render(<TeamCard team={idle} />)
    expect(screen.getAllByText('nobody')).toHaveLength(2) // in service + in queue
  })

  it('lets an admin serve the next customer', async () => {
    const serveNext = vi.fn().mockResolvedValue(undefined)
    useAuthStore.setState({ token: 'x', username: 'admin', roles: ['ADMIN'] })
    useDashboardStore.setState({ serveNext })

    render(<TeamCard team={team} />)
    fireEvent.click(screen.getByText('Serve next'))

    await waitFor(() => expect(serveNext).toHaveBeenCalledWith('t1'))
  })

  it('lets an agent serve the next customer too', () => {
    useAuthStore.setState({ token: 'x', username: 'carla', roles: ['AGENT'] })
    render(<TeamCard team={team} />)
    expect(screen.getByText('Serve next')).toBeInTheDocument()
  })

  it('reveals per-agent serving names when a team has more than two agents', () => {
    const bigTeam = {
      ...team,
      serving: [], // keep team-level empty so the names below only come from the popovers
      agents: [
        { id: 'a1', name: 'Ana', currentLoad: 1, maxConcurrent: 3, serving: ['Cliente A'] },
        { id: 'a2', name: 'Bruno', currentLoad: 0, maxConcurrent: 3, serving: [] },
        { id: 'a3', name: 'Cesar', currentLoad: 2, maxConcurrent: 3, serving: ['Cliente C1', 'Cliente C2'] },
      ],
    }
    render(<TeamCard team={bigTeam} />)
    expect(screen.getByText('Ana is serving')).toBeInTheDocument()
    expect(screen.getByText('Cliente A')).toBeInTheDocument()
    expect(screen.getByText('Cliente C2')).toBeInTheDocument()
  })

  it('does not render the per-agent popover for two or fewer agents', () => {
    const twoTeam = {
      ...team,
      serving: [],
      agents: [
        { id: 'a1', name: 'Ana', currentLoad: 1, maxConcurrent: 3, serving: ['Hidden A'] },
        { id: 'a2', name: 'Bruno', currentLoad: 0, maxConcurrent: 3, serving: ['Hidden B'] },
      ],
    }
    render(<TeamCard team={twoTeam} />)
    expect(screen.queryByText('Hidden A')).toBeNull()
    expect(screen.queryByText('Ana is serving')).toBeNull()
  })
})
