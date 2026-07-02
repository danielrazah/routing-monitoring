import { describe, it, expect, beforeEach, vi } from 'vitest'
import { render, screen } from '@testing-library/react'

vi.mock('@/shared/api/realtime.js', () => ({ connectDashboard: vi.fn(() => vi.fn()) }))
vi.mock('@/features/dashboard/api.js', () => ({
  fetchSnapshot: vi.fn().mockResolvedValue({ teams: [] }),
  advanceQueue: vi.fn(),
}))

import DashboardPage from '@/features/dashboard/DashboardPage.jsx'
import { useAuthStore } from '@/features/auth/authStore.js'
import { useDashboardStore } from '@/features/dashboard/dashboardStore.js'

const team = {
  id: 't1',
  name: 'Cards',
  waiting: 0,
  agents: [{ id: 'a1', name: 'Ana', currentLoad: 0, maxConcurrent: 3 }],
  serving: [],
  queue: [],
}

describe('DashboardPage', () => {
  beforeEach(() => {
    useDashboardStore.setState({ teams: [team], events: [], status: 'connected' })
  })

  it('renders teams and the new-contact form for an admin', () => {
    useAuthStore.setState({ token: 'x', username: 'admin', roles: ['ADMIN'] })
    render(<DashboardPage />)
    expect(screen.getByText('Teams')).toBeInTheDocument()
    expect(screen.getByText('Cards')).toBeInTheDocument()
    expect(screen.getByText('New contact')).toBeInTheDocument()
  })

  it('hides the new-contact form for a viewer', () => {
    useAuthStore.setState({ token: 'x', username: 'viewer', roles: ['VIEWER'] })
    render(<DashboardPage />)
    expect(screen.queryByText('New contact')).toBeNull()
  })
})
