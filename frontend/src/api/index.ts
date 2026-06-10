import { createApiClient } from './client'

const SESSION_KEY = 'bill-record-session'

function getStoredToken() {
  const raw = localStorage.getItem(SESSION_KEY)
  if (!raw) {
    return null
  }

  try {
    return (JSON.parse(raw) as { accessToken?: string }).accessToken ?? null
  } catch {
    return null
  }
}

export const apiBaseUrl = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api/v1'

export const apiClient = createApiClient({
  baseUrl: apiBaseUrl,
  getToken: getStoredToken,
  onUnauthorized: () => window.dispatchEvent(new CustomEvent('bill-record-unauthorized')),
})

export { SESSION_KEY }

