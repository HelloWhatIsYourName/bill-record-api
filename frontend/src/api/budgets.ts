import { apiClient } from './index'
import type { Budget } from './types'

export type BudgetPayload = {
  categoryId: string
  month: string
  amount: number
  currency: string
}

export const budgetsApi = {
  list(month: string) {
    return apiClient.get<Budget[]>(`/budgets?month=${encodeURIComponent(month)}`)
  },

  create(payload: BudgetPayload) {
    return apiClient.post<Budget>('/budgets', payload)
  },

  update(id: string, payload: Partial<Pick<BudgetPayload, 'amount' | 'currency'>>) {
    return apiClient.patch<Budget>(`/budgets/${id}`, payload)
  },

  remove(id: string) {
    return apiClient.delete<void>(`/budgets/${id}`)
  },
}

