import type { TransactionType } from '@/api/types'


export const transactionTone: Record<TransactionType, 'success' | 'danger' | 'neutral'> = {
  INCOME: 'success',
  EXPENSE: 'danger',
  TRANSFER: 'neutral',
}

export function monthRange(month: string) {
  const [yearValue, monthValue] = month.split('-').map(Number)
  const from = new Date(Date.UTC(yearValue, monthValue - 1, 1))
  const to = new Date(Date.UTC(yearValue, monthValue, 1))
  return {
    from: from.toISOString(),
    to: to.toISOString(),
  }
}
