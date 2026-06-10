import { apiClient } from './index'
import type { Account, AccountType } from './types'

export type AccountPayload = {
  name: string
  type: AccountType
  openingBalance: number
  currency: string
}

export type AccountUpdatePayload = Partial<Pick<AccountPayload, 'name' | 'type'>>

export const accountsApi = {
  list() {
    return apiClient.get<Account[]>('/accounts')
  },

  create(payload: AccountPayload) {
    return apiClient.post<Account>('/accounts', payload)
  },

  update(id: string, payload: AccountUpdatePayload) {
    return apiClient.patch<Account>(`/accounts/${id}`, payload)
  },

  archive(id: string) {
    return apiClient.delete<void>(`/accounts/${id}`)
  },
}

