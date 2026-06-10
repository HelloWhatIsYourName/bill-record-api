<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'

import EmptyState from '@/components/EmptyState.vue'
import ProgressBar from '@/components/ProgressBar.vue'
import { useLedgerStore } from '@/stores/ledger'
import { usePreferencesStore } from '@/stores/preferences'
import { transactionTone } from '@/utils/domain'
import { formatMoney, toMonthInputValue } from '@/utils/format'

const ledger = useLedgerStore()
const preferences = usePreferencesStore()
const month = ref(ledger.month || toMonthInputValue())

const totalBalance = computed(() =>
  ledger.accounts.reduce((total, account) => total + Number(account.currentBalance), 0),
)

const flowMax = computed(() => {
  const amounts = ledger.cashFlow.flatMap((point) => [Number(point.income), Number(point.expense)])
  return Math.max(1, ...amounts)
})

function accountName(id: string) {
  return ledger.accountById.get(id)?.name ?? preferences.t('transactions.account')
}

function categoryName(id?: string | null) {
  if (!id) {
    return preferences.t('common.none')
  }
  return ledger.categoryById.get(id)?.name ?? preferences.t('categories.title')
}

function flowWidth(value: string | number) {
  return `${Math.min(100, (Number(value) / flowMax.value) * 100)}%`
}

async function reload() {
  await ledger.loadDashboard(month.value)
}

onMounted(reload)
</script>

<template>
  <section class="page">
    <header class="page-header">
      <div>
        <h1>{{ preferences.t('dashboard.title') }}</h1>
        <p>{{ month }} {{ preferences.t('dashboard.description') }}</p>
      </div>
      <div class="toolbar">
        <input v-model="month" class="input" type="month" @change="reload" />
        <button class="button" type="button" @click="reload">{{ preferences.t('common.refresh') }}</button>
      </div>
    </header>

    <p v-if="ledger.error" class="error-banner">{{ ledger.error }}</p>

    <div class="grid grid--stats">
      <div class="stat">
        <span>{{ preferences.t('dashboard.income') }}</span>
        <strong class="tone--success">{{ formatMoney(ledger.summary?.income ?? 0, 'CNY') }}</strong>
      </div>
      <div class="stat">
        <span>{{ preferences.t('dashboard.expense') }}</span>
        <strong class="tone--danger">{{ formatMoney(ledger.summary?.expense ?? 0, 'CNY') }}</strong>
      </div>
      <div class="stat">
        <span>{{ preferences.t('dashboard.net') }}</span>
        <strong>{{ formatMoney(ledger.summary?.net ?? 0, 'CNY') }}</strong>
      </div>
      <div class="stat">
        <span>{{ preferences.t('dashboard.assets') }}</span>
        <strong>{{ formatMoney(totalBalance, 'CNY') }}</strong>
      </div>
    </div>

    <div class="grid grid--two">
      <section class="box">
        <header class="box__header">
          <h2>{{ preferences.t('dashboard.accountBalances') }}</h2>
        </header>
        <div class="box__body">
          <div v-if="ledger.summary?.accountBalances?.length" class="table-wrap">
            <table class="table">
              <thead>
                <tr>
                  <th>{{ preferences.t('transactions.account') }}</th>
                  <th>{{ preferences.t('dashboard.accountBalances') }}</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="account in ledger.summary.accountBalances" :key="account.accountId">
                  <td>{{ account.name }}</td>
                  <td>{{ formatMoney(account.balance, account.currency) }}</td>
                </tr>
              </tbody>
            </table>
          </div>
          <EmptyState v-else icon="account_balance_wallet" :title="preferences.t('reports.noAccountBalances')" :text="preferences.t('empty.accounts')" />
        </div>
      </section>

      <section class="box">
        <header class="box__header">
          <h2>{{ preferences.t('dashboard.budgetProgress') }}</h2>
        </header>
        <div class="box__body grid">
          <ProgressBar
            v-for="budget in ledger.budgets"
            :key="budget.id"
            :label="categoryName(budget.categoryId)"
            :value="budget.spent"
            :max="budget.amount"
          />
          <EmptyState v-if="!ledger.budgets.length" icon="savings" :title="preferences.t('budgets.empty')" :text="preferences.t('empty.budgets')" />
        </div>
      </section>
    </div>

    <div class="grid grid--two">
      <section class="box">
        <header class="box__header">
          <h2>{{ preferences.t('dashboard.recentTransactions') }}</h2>
        </header>
        <div class="table-wrap">
          <table v-if="ledger.recentTransactions.length" class="table">
            <thead>
              <tr>
                <th>{{ preferences.t('transactions.type') }}</th>
                <th>{{ preferences.t('transactions.account') }}</th>
                <th>{{ preferences.t('categories.title') }}</th>
                <th>{{ preferences.t('transactions.amount') }}</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="transaction in ledger.recentTransactions" :key="transaction.id">
                <td>
                  <span class="label" :class="`label--${transactionTone[transaction.type]}`">
                    {{ preferences.t(`domain.transaction.${transaction.type}`) }}
                  </span>
                </td>
                <td>{{ accountName(transaction.accountId) }}</td>
                <td>{{ transaction.type === 'TRANSFER' ? accountName(transaction.transferAccountId ?? '') : categoryName(transaction.categoryId) }}</td>
                <td :class="transaction.type === 'EXPENSE' ? 'amount--expense' : 'amount--income'">
                  {{ formatMoney(transaction.amount, transaction.currency) }}
                </td>
              </tr>
            </tbody>
          </table>
          <EmptyState v-else icon="receipt_long" :title="preferences.t('transactions.empty')" :text="preferences.t('empty.transactions')" />
        </div>
      </section>

      <section class="box">
        <header class="box__header">
          <h2>{{ preferences.t('dashboard.categorySpending') }}</h2>
        </header>
        <div class="table-wrap">
          <table v-if="ledger.categorySpending.length" class="table">
            <thead>
              <tr>
                <th>{{ preferences.t('categories.title') }}</th>
                <th>{{ preferences.t('transactions.amount') }}</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="item in ledger.categorySpending" :key="item.categoryId">
                <td>{{ item.categoryName }}</td>
                <td class="amount--expense">{{ formatMoney(item.amount, item.currency) }}</td>
              </tr>
            </tbody>
          </table>
          <EmptyState v-else icon="category" :title="preferences.t('reports.noCategorySpending')" :text="preferences.t('empty.categorySpending')" />
        </div>
      </section>
    </div>

    <section class="box">
      <header class="box__header">
        <h2>{{ preferences.t('dashboard.cashFlow') }}</h2>
      </header>
      <div class="box__body">
        <div v-if="ledger.cashFlow.length" class="flow-chart">
          <div v-for="point in ledger.cashFlow" :key="point.date" class="flow-row">
            <span>{{ point.date }}</span>
            <div class="flow-track">
              <div class="flow-fill" :style="{ width: flowWidth(point.income) }" />
            </div>
            <strong class="tone--success">{{ formatMoney(point.income, 'CNY') }}</strong>
            <span></span>
            <div class="flow-track">
              <div class="flow-fill flow-fill--expense" :style="{ width: flowWidth(point.expense) }" />
            </div>
            <strong class="tone--danger">{{ formatMoney(point.expense, 'CNY') }}</strong>
          </div>
        </div>
        <EmptyState v-else icon="analytics" :title="preferences.t('dashboard.cashFlow')" :text="preferences.t('empty.cashFlow')" />
      </div>
    </section>
  </section>
</template>
