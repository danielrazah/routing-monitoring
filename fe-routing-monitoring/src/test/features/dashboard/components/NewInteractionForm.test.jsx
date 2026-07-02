import { describe, it, expect, beforeEach, vi } from 'vitest'
import { render, screen, fireEvent, waitFor } from '@testing-library/react'

vi.mock('@/features/dashboard/api.js', () => ({
  createInteraction: vi.fn(),
  advanceQueue: vi.fn(),
  fetchSnapshot: vi.fn(),
}))

import NewInteractionForm from '@/features/dashboard/components/NewInteractionForm.jsx'
import { createInteraction } from '@/features/dashboard/api.js'

describe('NewInteractionForm', () => {
  beforeEach(() => vi.clearAllMocks())

  it('submits a new contact with name and subject', async () => {
    createInteraction.mockResolvedValue({})
    render(<NewInteractionForm />)

    fireEvent.change(screen.getByPlaceholderText('e.g. John Carter'), { target: { value: 'Maria' } })
    fireEvent.change(screen.getByRole('combobox'), { target: { value: 'LOAN_CONTRACTING' } })
    fireEvent.click(screen.getByRole('button'))

    await waitFor(() => expect(createInteraction).toHaveBeenCalledWith('Maria', 'LOAN_CONTRACTING'))
  })

  it('does nothing when the name is blank', () => {
    render(<NewInteractionForm />)
    fireEvent.click(screen.getByRole('button'))
    expect(createInteraction).not.toHaveBeenCalled()
  })

  it('shows an error message when the request fails', async () => {
    createInteraction.mockRejectedValue(new Error('boom'))
    render(<NewInteractionForm />)

    fireEvent.change(screen.getByPlaceholderText('e.g. John Carter'), { target: { value: 'Maria' } })
    fireEvent.click(screen.getByRole('button'))

    await waitFor(() => expect(screen.getByText(/Could not route the contact/i)).toBeInTheDocument())
  })
})
