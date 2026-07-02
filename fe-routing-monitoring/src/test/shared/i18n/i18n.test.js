import { describe, it, expect } from 'vitest'
import { t, tv, formatTime, locale } from '@/shared/i18n/i18n.js'

// jsdom's navigator.language is English, so we assert the English strings here.
describe('i18n (default/en)', () => {
  it('resolves the English locale by default', () => {
    expect(locale).toBe('en')
  })

  it('translates a plain key', () => {
    expect(t('teams.title')).toBe('Teams')
  })

  it('interpolates parameters', () => {
    expect(t('teams.slots', { used: 1, total: 3 })).toBe('1/3 slots in use')
  })

  it('returns the key itself for an unknown key', () => {
    expect(t('does.not.exist')).toBe('does.not.exist')
  })

  it('translates known dynamic values and falls back for unknown ones', () => {
    expect(tv('team', 'Cards')).toBe('Cards')
    expect(tv('subject', 'CARD_ISSUE')).toBe('Card issue')
    expect(tv('team', 'Marketing')).toBe('Marketing')
  })

  it('formats a time and returns empty for missing input', () => {
    expect(formatTime('')).toBe('')
    expect(formatTime('2026-01-01T10:15:30Z')).not.toBe('')
  })
})
