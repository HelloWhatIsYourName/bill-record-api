import type { AccountType, CategoryType, TransactionType } from '@/api/types'

export const accountTypeLabels: Record<AccountType, string> = {
  CASH: '现金',
  BANK: '银行',
  CREDIT_CARD: '信用卡',
  INVESTMENT: '投资',
  OTHER: '其他',
}

export const categoryTypeLabels: Record<CategoryType, string> = {
  INCOME: '收入',
  EXPENSE: '支出',
}

export const transactionTypeLabels: Record<TransactionType, string> = {
  INCOME: '收入',
  EXPENSE: '支出',
  TRANSFER: '转账',
}

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

