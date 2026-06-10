import { apiClient } from './index'
import type { AuthResponse, User } from './types'

export type LoginPayload = {
  email: string
  password: string
}

export type RegisterPayload = LoginPayload & {
  displayName: string
}

export const authApi = {
  register(payload: RegisterPayload) {
    return apiClient.post<AuthResponse>('/auth/register', payload)
  },

  login(payload: LoginPayload) {
    return apiClient.post<AuthResponse>('/auth/login', payload)
  },

  me() {
    return apiClient.get<User>('/users/me')
  },
}

