import { describe, it, expect } from 'vitest'
import { SUBJECT_VALUES, EVENT_STYLES } from '@/shared/constants/subjects.js'

describe('subjects constants', () => {
  it('lists the API subject values', () => {
    expect(SUBJECT_VALUES).toEqual(['CARD_ISSUE', 'LOAN_CONTRACTING', 'OTHER'])
  })

  it('has a style for every event type', () => {
    expect(Object.keys(EVENT_STYLES)).toEqual(['CREATED', 'ASSIGNED', 'QUEUED', 'ENDED'])
  })
})
