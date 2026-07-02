import { describe, it, expect, afterEach, vi } from 'vitest'
import { render, screen } from '@testing-library/react'
import App from '@/App.jsx'
import { useAuthStore } from '@/features/auth/authStore.js'
import { useDashboardStore } from '@/features/dashboard/dashboardStore.js'

// The dashboard connects live on mount; stub those edges so App renders in isolation.
vi.mock('@/shared/api/realtime.js', () => ({ connectDashboard: vi.fn(() => vi.fn()) }))
vi.mock('@/features/dashboard/api.js', () => ({
  fetchSnapshot: vi.fn().mockResolvedValue({ teams: [] }),
  advanceQueue: vi.fn(),
}))

describe('App', () => {
  // Some tests navigate; always return to the root path afterwards.
  afterEach(() => window.history.pushState({}, '', '/'))

  it('shows the landing screen on the root path', () => {
    useAuthStore.setState({ token: null, username: null, roles: [] })
    render(<App />)
    expect(screen.getByText('How can we help you today?')).toBeInTheDocument()
  })

  it('shows the customer queue screen on the /atendimento path', () => {
    window.history.pushState({}, '', '/atendimento')
    useAuthStore.setState({ token: null, username: null, roles: [] })
    render(<App />)
    expect(screen.getByText('Talk to us')).toBeInTheDocument()
  })

  it('shows the login screen on /painel when unauthenticated', () => {
    window.history.pushState({}, '', '/painel')
    useAuthStore.setState({ token: null, username: null, roles: [] })
    render(<App />)
    expect(screen.getByText('Access the routing dashboard')).toBeInTheDocument()
  })

  it('shows the dashboard on /painel when authenticated', () => {
    window.history.pushState({}, '', '/painel')
    useAuthStore.setState({ token: 'x', username: 'admin', roles: ['ADMIN'] })
    useDashboardStore.setState({ teams: [], events: [], status: 'connected' })
    render(<App />)
    expect(screen.getByText('Teams')).toBeInTheDocument()
  })
})
