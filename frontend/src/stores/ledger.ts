import { defineStore } from 'pinia'

import { accountsApi, type AccountPayload, type AccountUpdatePayload } from '@/api/accounts'
import { budgetsApi, type BudgetPayload } from '@/api/budgets'
import { categoriesApi, type CategoryPayload } from '@/api/categories'
import { reportsApi } from '@/api/reports'
import { transactionsApi, type TransactionFilters, type TransactionPayload } from '@/api/transactions'
import type { Account, Budget, CashFlowPoint, Category, CategorySpending, MonthlySummary, Transaction } from '@/api/types'
import { monthRange } from '@/utils/domain'
import { toErrorMessage } from '@/utils/errors'
import { toMonthInputValue } from '@/utils/format'

type LedgerState = {
  accounts: Account[]
  categories: Category[]
  transactions: Transaction[]
  budgets: Budget[]
  summary: MonthlySummary | null
  categorySpending: CategorySpending[]
  cashFlow: CashFlowPoint[]
  month: string
  loading: boolean
  error: string | null
}

export const useLedgerStore = defineStore('ledger', {
  state: (): LedgerState => ({
    accounts: [],
    categories: [],
    transactions: [],
    budgets: [],
    summary: null,
    categorySpending: [],
    cashFlow: [],
    month: toMonthInputValue(),
    loading: false,
    error: null,
  }),

  getters: {
    expenseCategories: (state) => state.categories.filter((category) => category.type === 'EXPENSE'),
    incomeCategories: (state) => state.categories.filter((category) => category.type === 'INCOME'),
    categoryById: (state) => new Map(state.categories.map((category) => [category.id, category])),
    accountById: (state) => new Map(state.accounts.map((account) => [account.id, account])),
    recentTransactions: (state) => state.transactions.slice(0, 8),
  },

  actions: {
    async withLoading(task: () => Promise<void>) {
      this.loading = true
      this.error = null
      try {
        await task()
      } catch (error) {
        this.error = toErrorMessage(error)
        throw error
      } finally {
        this.loading = false
      }
    },

    async loadReference() {
      const [accounts, categories] = await Promise.all([accountsApi.list(), categoriesApi.list()])
      this.accounts = accounts
      this.categories = categories
    },

    async loadTransactions(filters: TransactionFilters = {}) {
      this.transactions = await transactionsApi.list(filters)
    },

    async loadBudgets(month?: string) {
      const selectedMonth = month ?? this.month
      this.month = selectedMonth
      this.budgets = await budgetsApi.list(selectedMonth)
    },

    async loadReports(month?: string) {
      const selectedMonth = month ?? this.month
      this.month = selectedMonth
      const range = monthRange(selectedMonth)
      const [summary, categorySpending, cashFlow] = await Promise.all([
        reportsApi.monthlySummary(selectedMonth),
        reportsApi.categorySpending(range.from, range.to),
        reportsApi.cashFlow(range.from, range.to),
      ])
      this.summary = summary
      this.categorySpending = categorySpending
      this.cashFlow = cashFlow
    },

    async loadDashboard(month?: string) {
      const selectedMonth = month ?? this.month
      await this.withLoading(async () => {
        await this.loadReference()
        await Promise.all([this.loadTransactions(), this.loadBudgets(selectedMonth), this.loadReports(selectedMonth)])
      })
    },

    async createAccount(payload: AccountPayload) {
      await accountsApi.create(payload)
      await this.loadReference()
    },

    async updateAccount(id: string, payload: AccountUpdatePayload) {
      await accountsApi.update(id, payload)
      await this.loadReference()
    },

    async archiveAccount(id: string) {
      await accountsApi.archive(id)
      await this.loadReference()
    },

    async createCategory(payload: CategoryPayload) {
      await categoriesApi.create(payload)
      await this.loadReference()
    },

    async updateCategory(id: string, payload: Partial<CategoryPayload>) {
      await categoriesApi.update(id, payload)
      await this.loadReference()
    },

    async archiveCategory(id: string) {
      await categoriesApi.archive(id)
      await this.loadReference()
    },

    async createTransaction(payload: TransactionPayload) {
      await transactionsApi.create(payload)
      await this.loadDashboard(this.month)
    },

    async updateTransaction(id: string, payload: TransactionPayload) {
      await transactionsApi.update(id, payload)
      await this.loadDashboard(this.month)
    },

    async deleteTransaction(id: string) {
      await transactionsApi.remove(id)
      await this.loadDashboard(this.month)
    },

    async createBudget(payload: BudgetPayload) {
      await budgetsApi.create(payload)
      await this.loadDashboard(payload.month)
    },

    async updateBudget(id: string, payload: Partial<Pick<BudgetPayload, 'amount' | 'currency'>>) {
      await budgetsApi.update(id, payload)
      await this.loadDashboard(this.month)
    },

    async deleteBudget(id: string) {
      await budgetsApi.remove(id)
      await this.loadDashboard(this.month)
    },
  },
})
