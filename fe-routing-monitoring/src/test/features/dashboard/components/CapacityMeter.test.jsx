import { describe, it, expect } from 'vitest'
import { render } from '@testing-library/react'
import CapacityMeter from '@/features/dashboard/components/CapacityMeter.jsx'

describe('CapacityMeter', () => {
  it('renders one segment per slot, filled up to the current load', () => {
    const { container } = render(<CapacityMeter load={2} max={3} />)
    expect(container.querySelectorAll('span')).toHaveLength(3)
    expect(container.querySelectorAll('span.bg-teal-400')).toHaveLength(2)
  })

  it('shows no filled segments for an idle agent', () => {
    const { container } = render(<CapacityMeter load={0} max={3} />)
    expect(container.querySelectorAll('span.bg-teal-400')).toHaveLength(0)
  })
})
