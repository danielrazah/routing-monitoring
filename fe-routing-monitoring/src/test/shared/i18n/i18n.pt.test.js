import { describe, it, expect, beforeEach, afterEach, vi } from 'vitest'

// Covers the Portuguese branch: stub the browser language before importing the module.
describe('i18n (pt)', () => {
  beforeEach(() => vi.resetModules())
  afterEach(() => vi.unstubAllGlobals())

  it('uses Portuguese when the browser language is pt', async () => {
    vi.stubGlobal('navigator', { language: 'pt-BR' })
    const { t, tv, locale } = await import('@/shared/i18n/i18n.js')

    expect(locale).toBe('pt')
    expect(t('teams.title')).toBe('Times')
    expect(tv('subject', 'CARD_ISSUE')).toBe('Problema com cartão')
    expect(tv('team', 'Cards')).toBe('Cartões')
  })
})
