import { defineStore } from 'pinia'

import { type Language, type MessageKey, messages } from '@/i18n/messages'

const LANGUAGE_KEY = 'bill-record-language'

type PreferencesState = {
  language: Language
}

function readLanguage(): Language {
  const value = localStorage.getItem(LANGUAGE_KEY)
  return value === 'en' || value === 'zh' ? value : 'zh'
}

export const usePreferencesStore = defineStore('preferences', {
  state: (): PreferencesState => ({
    language: 'zh',
  }),

  getters: {
    languageLabel: (state) => (state.language === 'zh' ? 'EN' : '中文'),
  },

  actions: {
    bootstrapFromStorage() {
      this.language = readLanguage()
    },

    setLanguage(language: Language) {
      this.language = language
      localStorage.setItem(LANGUAGE_KEY, language)
    },

    toggleLanguage() {
      this.setLanguage(this.language === 'zh' ? 'en' : 'zh')
    },

    t(key: MessageKey | string) {
      const dictionary = messages[this.language] as Record<string, string>
      return dictionary[key] ?? key
    },
  },
})

