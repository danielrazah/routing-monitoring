import { describe, it, expect } from 'vitest'
import { t, tv, formatTime } from './i18n.js'

// jsdom's navigator.language is English, so we assert the English strings.
describe('i18n', () => {
  it('translates a plain key', () => {
    expect(t('teams.title')).toBe('Teams')
  })

  it('interpolates parameters', () => {
    expect(t('teams.slots', { used: 1, total: 3 })).toBe('1/3 slots in use')
  })

  it('translates known dynamic values', () => {
    expect(tv('team', 'Cards')).toBe('Cards')
    expect(tv('subject', 'CARD_ISSUE')).toBe('Card issue')
  })

  it('falls back to the raw value for unknown dynamic values', () => {
    expect(tv('team', 'Marketing')).toBe('Marketing')
  })

  it('formats a time and returns empty for missing input', () => {
    expect(formatTime('')).toBe('')
    expect(formatTime('2026-01-01T10:15:30Z')).not.toBe('')
  })
})
