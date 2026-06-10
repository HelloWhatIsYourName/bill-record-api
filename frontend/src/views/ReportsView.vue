<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'

import EmptyState from '@/components/EmptyState.vue'
import { useLedgerStore } from '@/stores/ledger'
import { formatMoney, toMonthInputValue } from '@/utils/format'

const ledger = useLedgerStore()
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
        <h1>Reports</h1>
        <p>{{ month }} 报表</p>
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
        <span>账户数</span>
        <strong>{{ ledger.summary?.accountBalances.length ?? 0 }}</strong>
      </div>
    </div>

    <div class="grid grid--two">
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
          <EmptyState v-else icon="category" title="暂无分类支出" text="当前月份没有支出记录" />
        </div>
      </section>

      <section class="box">
        <header class="box__header">
          <h2>账户余额</h2>
        </header>
        <div class="table-wrap">
          <table v-if="ledger.summary?.accountBalances.length" class="table">
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
          <EmptyState v-else icon="account_balance_wallet" title="暂无账户余额" text="账户余额为空" />
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
        <EmptyState v-else icon="analytics" title="暂无现金流" text="当前月份没有现金流记录" />
      </div>
    </section>
  </section>
</template>
