import { createPinia, setActivePinia } from 'pinia'
import { beforeEach, describe, expect, it } from 'vitest'

import { useAuthStore } from './auth'

describe('auth store', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
  })

  it('persists and restores the token and profile', () => {
    const auth = useAuthStore()

    auth.setSession({
      accessToken: 'token-123',
      tokenType: 'Bearer',
      expiresAt: '2026-06-10T02:00:00Z',
      user: {
        id: 'user-1',
        email: 'user@example.com',
        displayName: 'User',
        defaultCurrency: 'CNY',
        role: 'USER',
      },
    })

    const nextAuth = useAuthStore()
    nextAuth.$reset()
    nextAuth.bootstrapFromStorage()

    expect(nextAuth.token).toBe('token-123')
    expect(nextAuth.user?.email).toBe('user@example.com')
    expect(nextAuth.isAuthenticated).toBe(true)
  })

  it('clears persisted credentials on logout', () => {
    const auth = useAuthStore()
    auth.setSession({
      accessToken: 'token-123',
      tokenType: 'Bearer',
      expiresAt: '2026-06-10T02:00:00Z',
      user: {
        id: 'user-1',
        email: 'user@example.com',
        displayName: 'User',
        defaultCurrency: 'CNY',
        role: 'USER',
      },
    })

    auth.logout()

    expect(auth.token).toBeNull()
    expect(auth.user).toBeNull()
    expect(localStorage.getItem('bill-record-session')).toBeNull()
  })
})
