import { describe, it, expect, beforeEach } from 'vitest'
import { render, screen } from '@testing-library/react'
import LandingPage from '@/features/landing/LandingPage.jsx'
import { useAuthStore } from '@/features/auth/authStore.js'

describe('LandingPage', () => {
  beforeEach(() => useAuthStore.setState({ token: null, username: null, roles: [] }))

  it('links the customer entry to /atendimento and, when logged out, the team entry to sign in', () => {
    render(<LandingPage />)
    expect(screen.getByText('Get served').closest('a')).toHaveAttribute('href', '/atendimento')

    const team = screen.getByText('Team sign in').closest('a')
    expect(team).toHaveAttribute('href', '/painel')
  })

  it('invites a logged-in user straight to the dashboard', () => {
    useAuthStore.setState({ token: 'x', username: 'admin', roles: ['ADMIN'] })
    render(<LandingPage />)
    expect(screen.getByText('Go to dashboard').closest('a')).toHaveAttribute('href', '/painel')
  })
})
