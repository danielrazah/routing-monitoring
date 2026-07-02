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

  it('hides the Serve next button from a viewer', () => {
    useAuthStore.setState({ token: 'x', username: 'viewer', roles: ['VIEWER'] })
    render(<TeamCard team={team} />)
    expect(screen.queryByText('Serve next')).toBeNull()
  })
})
