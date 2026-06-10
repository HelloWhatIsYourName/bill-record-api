import { defineStore } from 'pinia'

import { authApi, type LoginPayload, type RegisterPayload } from '@/api/auth'
import { SESSION_KEY } from '@/api'
import type { AuthResponse, User } from '@/api/types'

type AuthState = {
  token: string | null
  user: User | null
  expiresAt: string | null
  loading: boolean
  error: string | null
}

type StoredSession = AuthResponse

function readStoredSession(): StoredSession | null {
  const raw = localStorage.getItem(SESSION_KEY)
  if (!raw) {
    return null
  }

  try {
    return JSON.parse(raw) as StoredSession
  } catch {
    localStorage.removeItem(SESSION_KEY)
    return null
  }
}

export const useAuthStore = defineStore('auth', {
  state: (): AuthState => ({
    token: null,
    user: null,
    expiresAt: null,
    loading: false,
    error: null,
  }),

  getters: {
    isAuthenticated: (state) => Boolean(state.token && state.user),
  },

  actions: {
    bootstrapFromStorage() {
      const session = readStoredSession()
      if (session) {
        this.token = session.accessToken
        this.expiresAt = session.expiresAt
        this.user = session.user
      }
    },

    setSession(session: AuthResponse) {
      this.token = session.accessToken
      this.expiresAt = session.expiresAt
      this.user = session.user
      this.error = null
      localStorage.setItem(SESSION_KEY, JSON.stringify(session))
    },

    async login(payload: LoginPayload) {
      await this.submit(() => authApi.login(payload))
    },

    async register(payload: RegisterPayload) {
      await this.submit(() => authApi.register(payload))
    },

    async refreshProfile() {
      if (!this.token) {
        return
      }
      this.user = await authApi.me()
    },

    logout() {
      this.token = null
      this.user = null
      this.expiresAt = null
      this.error = null
      localStorage.removeItem(SESSION_KEY)
    },

    async submit(request: () => Promise<AuthResponse>) {
      this.loading = true
      this.error = null
      try {
        this.setSession(await request())
      } catch (error) {
        this.error = error instanceof Error ? error.message : 'Request failed'
        throw error
      } finally {
        this.loading = false
      }
    },
  },
})
