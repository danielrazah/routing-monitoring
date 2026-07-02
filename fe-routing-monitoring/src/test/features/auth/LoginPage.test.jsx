import { describe, it, expect, beforeEach, afterEach, vi } from 'vitest'
import { render, screen, fireEvent, waitFor } from '@testing-library/react'
import LoginPage from '@/features/auth/LoginPage.jsx'
import { useAuthStore } from '@/features/auth/authStore.js'

describe('LoginPage', () => {
  beforeEach(() => {
    useAuthStore.setState({ token: null, username: null, roles: [] })
    localStorage.clear()
  })
  afterEach(() => vi.restoreAllMocks())

  function fillAndSubmit() {
    fireEvent.change(screen.getByLabelText('Username'), { target: { value: 'admin' } })
    fireEvent.change(screen.getByLabelText('Password'), { target: { value: 'admin123' } })
    fireEvent.click(screen.getByRole('button', { name: 'Sign in' }))
  }

  it('logs in on submit', async () => {
    vi.spyOn(globalThis, 'fetch').mockResolvedValue({
      ok: true,
      json: async () => ({ token: 'jwt', username: 'admin', roles: ['ADMIN'] }),
    })
    render(<LoginPage />)
    fillAndSubmit()
    await waitFor(() => expect(useAuthStore.getState().token).toBe('jwt'))
  })

  it('shows an error on invalid credentials', async () => {
    vi.spyOn(globalThis, 'fetch').mockResolvedValue({ ok: false })
    render(<LoginPage />)
    fillAndSubmit()
    await waitFor(() =>
      expect(screen.getByText('Invalid username or password')).toBeInTheDocument())
    expect(useAuthStore.getState().token).toBeNull()
  })
})
