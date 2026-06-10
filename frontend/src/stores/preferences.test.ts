import { createPinia, setActivePinia } from 'pinia'
import { beforeEach, describe, expect, it } from 'vitest'

import { usePreferencesStore } from './preferences'

describe('preferences store', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
  })

  it('defaults to Chinese and translates known keys', () => {
    const preferences = usePreferencesStore()

    expect(preferences.language).toBe('zh')
    expect(preferences.t('nav.dashboard')).toBe('仪表盘')
  })

  it('toggles language and persists the choice', () => {
    const preferences = usePreferencesStore()

    preferences.toggleLanguage()

    expect(preferences.language).toBe('en')
    expect(preferences.t('nav.dashboard')).toBe('Dashboard')
    expect(localStorage.getItem('bill-record-language')).toBe('en')

    const nextPreferences = usePreferencesStore()
    nextPreferences.$reset()
    nextPreferences.bootstrapFromStorage()

    expect(nextPreferences.language).toBe('en')
  })

  it('falls back to the translation key when a message is missing', () => {
    const preferences = usePreferencesStore()

    expect(preferences.t('missing.key')).toBe('missing.key')
  })
})
