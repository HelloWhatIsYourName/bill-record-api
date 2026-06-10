import { apiClient } from './index'
import type { CashFlowPoint, CategorySpending, MonthlySummary } from './types'

export const reportsApi = {
  monthlySummary(month: string) {
    return apiClient.get<MonthlySummary>(`/reports/monthly-summary?month=${encodeURIComponent(month)}`)
  },

  categorySpending(from: string, to: string) {
    const params = new URLSearchParams({ from, to })
    return apiClient.get<CategorySpending[]>(`/reports/category-spending?${params}`)
  },

  cashFlow(from: string, to: string) {
    const params = new URLSearchParams({ from, to })
    return apiClient.get<CashFlowPoint[]>(`/reports/cash-flow?${params}`)
  },
}

