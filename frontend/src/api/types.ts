export type User = {
  id: string
  email: string
  displayName: string
  defaultCurrency: string
  role: string
}

export type AuthResponse = {
  accessToken: string
  tokenType: string
  expiresAt: string
  user: User
}

export type AccountType = 'CASH' | 'BANK' | 'CREDIT_CARD' | 'INVESTMENT' | 'OTHER'

export type Account = {
  id: string
  name: string
  type: AccountType
  openingBalance: number | string
  currentBalance: number | string
  currency: string
  archived: boolean
}

export type CategoryType = 'INCOME' | 'EXPENSE'

export type Category = {
  id: string
  name: string
  type: CategoryType
  color?: string | null
  icon?: string | null
  archived: boolean
}

export type TransactionType = 'INCOME' | 'EXPENSE' | 'TRANSFER'

export type Transaction = {
  id: string
  type: TransactionType
  accountId: string
  transferAccountId?: string | null
  categoryId?: string | null
  amount: number | string
  currency: string
  transactionTime: string
  note?: string | null
}

export type Budget = {
  id: string
  categoryId: string
  month: string
  amount: number | string
  currency: string
  spent: number | string
  remaining: number | string
}

export type AccountBalance = {
  accountId: string
  name: string
  balance: number | string
  currency: string
}

export type MonthlySummary = {
  month: string
  income: number | string
  expense: number | string
  net: number | string
  accountBalances: AccountBalance[]
}

export type CategorySpending = {
  categoryId: string
  categoryName: string
  amount: number | string
  currency: string
}

export type CashFlowPoint = {
  date: string
  income: number | string
  expense: number | string
  net: number | string
}

export type FieldError = {
  field: string
  message: string
}

export type BackendErrorResponse = {
  timestamp?: string
  status: number
  code: string
  message: string
  path?: string
  fieldErrors?: FieldError[]
}
