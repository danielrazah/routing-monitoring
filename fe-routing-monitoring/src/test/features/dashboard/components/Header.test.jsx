import { describe, it, expect, beforeEach } from 'vitest'
import { render, screen, fireEvent } from '@testing-library/react'
import Header from '@/features/dashboard/components/Header.jsx'
import { useAuthStore } from '@/features/auth/authStore.js'
import { useDashboardStore } from '@/features/dashboard/dashboardStore.js'

describe('Header', () => {
  beforeEach(() => {
    useDashboardStore.setState({ status: 'connected', teams: [], events: [] })
  })

  it('shows the user, role and connection status, and logs out', () => {
    useAuthStore.setState({ token: 'x', username: 'admin', roles: ['ADMIN'] })
    render(<Header />)

    expect(screen.getByText('admin')).toBeInTheDocument()
    expect(screen.getByText('ADMIN')).toBeInTheDocument()
    expect(screen.getByText('Live')).toBeInTheDocument()

    fireEvent.click(screen.getByText('Log out'))
    expect(useAuthStore.getState().token).toBeNull()
  })

  it('shows Polling when the connection is offline', () => {
    useAuthStore.setState({ token: 'x', username: 'u', roles: [] })
    useDashboardStore.setState({ status: 'offline' })
    render(<Header />)
    expect(screen.getByText('Polling')).toBeInTheDocument()
  })

  it('links the ubots logo to the main page', () => {
    useAuthStore.setState({ token: 'x', username: 'admin', roles: ['ADMIN'] })
    render(<Header />)
    expect(screen.getByAltText('ubots').closest('a')).toHaveAttribute('href', '/')
  })
})
