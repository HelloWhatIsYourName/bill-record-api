<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'

import EmptyState from '@/components/EmptyState.vue'
import ProgressBar from '@/components/ProgressBar.vue'
import { useLedgerStore } from '@/stores/ledger'
import { transactionTone, transactionTypeLabels } from '@/utils/domain'
import { formatMoney, toMonthInputValue } from '@/utils/format'

const ledger = useLedgerStore()
const month = ref(ledger.month || toMonthInputValue())

const totalBalance = computed(() =>
  ledger.accounts.reduce((total, account) => total + Number(account.currentBalance), 0),
)

const flowMax = computed(() => {
  const amounts = ledger.cashFlow.flatMap((point) => [Number(point.income), Number(point.expense)])
  return Math.max(1, ...amounts)
})

function accountName(id: string) {
  return ledger.accountById.get(id)?.name ?? '账户'
}

function categoryName(id?: string | null) {
  if (!id) {
    return '无分类'
  }
  return ledger.categoryById.get(id)?.name ?? '分类'
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
        <h1>Dashboard</h1>
        <p>{{ month }} 月度概览</p>
      </div>
      <div class="toolbar">
        <input v-model="month" class="input" type="month" @change="reload" />
        <button class="button" type="button" @click="reload">刷新</button>
      </div>
    </header>

    <p v-if="ledger.error" class="error-banner">{{ ledger.error }}</p>

    <div class="grid grid--stats">
      <div class="stat">
        <span>收入</span>
        <strong class="tone--success">{{ formatMoney(ledger.summary?.income ?? 0, 'CNY') }}</strong>
      </div>
      <div class="stat">
        <span>支出</span>
        <strong class="tone--danger">{{ formatMoney(ledger.summary?.expense ?? 0, 'CNY') }}</strong>
      </div>
      <div class="stat">
        <span>净额</span>
        <strong>{{ formatMoney(ledger.summary?.net ?? 0, 'CNY') }}</strong>
      </div>
      <div class="stat">
        <span>资产</span>
        <strong>{{ formatMoney(totalBalance, 'CNY') }}</strong>
      </div>
    </div>

    <div class="grid grid--two">
      <section class="box">
        <header class="box__header">
          <h2>账户余额</h2>
        </header>
        <div class="box__body">
          <div v-if="ledger.summary?.accountBalances?.length" class="table-wrap">
            <table class="table">
              <thead>
                <tr>
                  <th>账户</th>
                  <th>余额</th>
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
          <EmptyState v-else icon="account_balance_wallet" title="暂无账户" text="账户余额为空" />
        </div>
      </section>

      <section class="box">
        <header class="box__header">
          <h2>预算进度</h2>
        </header>
        <div class="box__body grid">
          <ProgressBar
            v-for="budget in ledger.budgets"
            :key="budget.id"
            :label="categoryName(budget.categoryId)"
            :value="budget.spent"
            :max="budget.amount"
          />
          <EmptyState v-if="!ledger.budgets.length" icon="savings" title="暂无预算" text="预算列表为空" />
        </div>
      </section>
    </div>

    <div class="grid grid--two">
      <section class="box">
        <header class="box__header">
          <h2>近期交易</h2>
        </header>
        <div class="table-wrap">
          <table v-if="ledger.recentTransactions.length" class="table">
            <thead>
              <tr>
                <th>类型</th>
                <th>账户</th>
                <th>分类</th>
                <th>金额</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="transaction in ledger.recentTransactions" :key="transaction.id">
                <td>
                  <span class="label" :class="`label--${transactionTone[transaction.type]}`">
                    {{ transactionTypeLabels[transaction.type] }}
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
          <EmptyState v-else icon="receipt_long" title="暂无交易" text="交易列表为空" />
        </div>
      </section>

      <section class="box">
        <header class="box__header">
          <h2>分类支出</h2>
        </header>
        <div class="table-wrap">
          <table v-if="ledger.categorySpending.length" class="table">
            <thead>
              <tr>
                <th>分类</th>
                <th>金额</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="item in ledger.categorySpending" :key="item.categoryId">
                <td>{{ item.categoryName }}</td>
                <td class="amount--expense">{{ formatMoney(item.amount, item.currency) }}</td>
              </tr>
            </tbody>
          </table>
          <EmptyState v-else icon="category" title="暂无支出" text="当前月份没有分类支出" />
        </div>
      </section>
    </div>

    <section class="box">
      <header class="box__header">
        <h2>现金流</h2>
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
        <EmptyState v-else icon="analytics" title="暂无现金流" text="当前月份没有现金流记录" />
      </div>
    </section>
  </section>
</template>
