<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'

import EmptyState from '@/components/EmptyState.vue'
import { useLedgerStore } from '@/stores/ledger'
import { usePreferencesStore } from '@/stores/preferences'
import { formatMoney, toMonthInputValue } from '@/utils/format'

const ledger = useLedgerStore()
const preferences = usePreferencesStore()
const month = ref(ledger.month || toMonthInputValue())

const flowMax = computed(() => {
  const amounts = ledger.cashFlow.flatMap((point) => [Number(point.income), Number(point.expense), Math.abs(Number(point.net))])
  return Math.max(1, ...amounts)
})

function width(value: string | number) {
  return `${Math.min(100, (Math.abs(Number(value)) / flowMax.value) * 100)}%`
}

async function reload() {
  await ledger.withLoading(() => ledger.loadReports(month.value))
}

onMounted(reload)
</script>

<template>
  <section class="page">
    <header class="page-header">
      <div>
        <h1>{{ preferences.t('reports.title') }}</h1>
        <p>{{ month }} {{ preferences.t('reports.description') }}</p>
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
        <span>{{ preferences.t('reports.accountCount') }}</span>
        <strong>{{ ledger.summary?.accountBalances.length ?? 0 }}</strong>
      </div>
    </div>

    <div class="grid grid--two">
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
          <EmptyState v-else icon="category" :title="preferences.t('reports.noCategorySpending')" :text="preferences.t('empty.reportsCategorySpending')" />
        </div>
      </section>

      <section class="box">
        <header class="box__header">
          <h2>{{ preferences.t('dashboard.accountBalances') }}</h2>
        </header>
        <div class="table-wrap">
          <table v-if="ledger.summary?.accountBalances.length" class="table">
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
          <EmptyState v-else icon="account_balance_wallet" :title="preferences.t('reports.noAccountBalances')" :text="preferences.t('empty.accounts')" />
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
              <div class="flow-fill" :style="{ width: width(point.income) }" />
            </div>
            <strong class="tone--success">{{ formatMoney(point.income, 'CNY') }}</strong>
            <span></span>
            <div class="flow-track">
              <div class="flow-fill flow-fill--expense" :style="{ width: width(point.expense) }" />
            </div>
            <strong class="tone--danger">{{ formatMoney(point.expense, 'CNY') }}</strong>
          </div>
        </div>
        <EmptyState v-else icon="analytics" :title="preferences.t('dashboard.cashFlow')" :text="preferences.t('empty.cashFlow')" />
      </div>
    </section>
  </section>
</template>
