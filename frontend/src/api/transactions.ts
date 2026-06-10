import { apiClient } from './index'
import type { Transaction, TransactionType } from './types'

export type TransactionPayload = {
  type: TransactionType
  accountId: string
  transferAccountId?: string | null
  categoryId?: string | null
  amount: number
  currency: string
  transactionTime: string
  note?: string
}

export type TransactionFilters = {
  type?: TransactionType
  accountId?: string
  categoryId?: string
}

export const transactionsApi = {
  list(filters: TransactionFilters = {}) {
    const params = new URLSearchParams()
    Object.entries(filters).forEach(([key, value]) => {
      if (value) {
        params.set(key, value)
      }
    })
    const query = params.toString()
    return apiClient.get<Transaction[]>(query ? `/transactions?${query}` : '/transactions')
  },

  create(payload: TransactionPayload) {
    return apiClient.post<Transaction>('/transactions', payload)
  },

  update(id: string, payload: Partial<TransactionPayload>) {
    return apiClient.patch<Transaction>(`/transactions/${id}`, payload)
  },

  remove(id: string) {
    return apiClient.delete<void>(`/transactions/${id}`)
  },
}

