<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'

import AppIcon from '@/components/AppIcon.vue'
import EmptyState from '@/components/EmptyState.vue'
import ModalPanel from '@/components/ModalPanel.vue'
import { useLedgerStore } from '@/stores/ledger'
import { categoryTypeLabels, transactionTone, transactionTypeLabels } from '@/utils/domain'
import { toErrorMessage } from '@/utils/errors'
import { formatMoney, fromDateTimeInputValue, toDateTimeInputValue } from '@/utils/format'
import type { Transaction, TransactionType } from '@/api/types'

type TransactionForm = {
  id: string | null
  type: TransactionType
  accountId: string
  transferAccountId: string
  categoryId: string
  amount: string
  currency: string
  transactionTime: string
  note: string
}

const ledger = useLedgerStore()
const modalOpen = ref(false)
const saving = ref(false)
const error = ref<string | null>(null)
const filters = reactive<{ type: '' | TransactionType; accountId: string; categoryId: string }>({
  type: '',
  accountId: '',
  categoryId: '',
})
const form = reactive<TransactionForm>(freshForm())

const availableCategories = computed(() => {
  if (form.type === 'INCOME') {
    return ledger.incomeCategories
  }
  if (form.type === 'EXPENSE') {
    return ledger.expenseCategories
  }
  return []
})

function freshForm(): TransactionForm {
  return {
    id: null,
    type: 'EXPENSE',
    accountId: '',
    transferAccountId: '',
    categoryId: '',
    amount: '',
    currency: 'CNY',
    transactionTime: toDateTimeInputValue(),
    note: '',
  }
}

function resetForm() {
  Object.assign(form, freshForm())
  form.accountId = ledger.accounts[0]?.id ?? ''
  form.categoryId = ledger.expenseCategories[0]?.id ?? ''
}

function accountName(id?: string | null) {
  if (!id) {
    return '账户'
  }
  return ledger.accountById.get(id)?.name ?? '账户'
}

function categoryName(id?: string | null) {
  if (!id) {
    return '无分类'
  }
  return ledger.categoryById.get(id)?.name ?? '分类'
}

function openCreate() {
  resetForm()
  error.value = null
  modalOpen.value = true
}

function openEdit(transaction: Transaction) {
  Object.assign(form, {
    id: transaction.id,
    type: transaction.type,
    accountId: transaction.accountId,
    transferAccountId: transaction.transferAccountId ?? '',
    categoryId: transaction.categoryId ?? '',
    amount: String(transaction.amount),
    currency: transaction.currency,
    transactionTime: toDateTimeInputValue(new Date(transaction.transactionTime)),
    note: transaction.note ?? '',
  })
  error.value = null
  modalOpen.value = true
}

async function submit() {
  saving.value = true
  error.value = null
  try {
    const payload = {
      type: form.type,
      accountId: form.accountId,
      transferAccountId: form.type === 'TRANSFER' ? form.transferAccountId : null,
      categoryId: form.type === 'TRANSFER' ? null : form.categoryId,
      amount: Number(form.amount),
      currency: form.currency,
      transactionTime: fromDateTimeInputValue(form.transactionTime),
      note: form.note,
    }

    if (form.id) {
      await ledger.updateTransaction(form.id, payload)
    } else {
      await ledger.createTransaction(payload)
    }
    modalOpen.value = false
  } catch (requestError) {
    error.value = toErrorMessage(requestError)
  } finally {
    saving.value = false
  }
}

async function remove(transaction: Transaction) {
  if (!window.confirm('删除这条交易？')) {
    return
  }
  await ledger.deleteTransaction(transaction.id)
}

async function applyFilters() {
  await ledger.withLoading(async () => {
    await ledger.loadTransactions({
      type: filters.type || undefined,
      accountId: filters.accountId || undefined,
      categoryId: filters.categoryId || undefined,
    })
  })
}

onMounted(async () => {
  await ledger.withLoading(async () => {
    await ledger.loadReference()
    await ledger.loadTransactions()
  })
})
</script>

<template>
  <section class="page">
    <header class="page-header">
      <div>
        <h1>Transactions</h1>
        <p>收入、支出和转账</p>
      </div>
      <button class="button button--primary" type="button" @click="openCreate">
        <AppIcon name="add" />
        新增交易
      </button>
    </header>

    <p v-if="ledger.error" class="error-banner">{{ ledger.error }}</p>

    <section class="box">
      <header class="box__header">
        <h2>筛选</h2>
        <button class="button" type="button" @click="applyFilters">应用</button>
      </header>
      <div class="box__body form-grid">
        <div class="form-row">
          <label for="filter-type">类型</label>
          <select id="filter-type" v-model="filters.type" class="select">
            <option value="">全部</option>
            <option value="INCOME">收入</option>
            <option value="EXPENSE">支出</option>
            <option value="TRANSFER">转账</option>
          </select>
        </div>
        <div class="form-row">
          <label for="filter-account">账户</label>
          <select id="filter-account" v-model="filters.accountId" class="select">
            <option value="">全部</option>
            <option v-for="account in ledger.accounts" :key="account.id" :value="account.id">{{ account.name }}</option>
          </select>
        </div>
        <div class="form-row">
          <label for="filter-category">分类</label>
          <select id="filter-category" v-model="filters.categoryId" class="select">
            <option value="">全部</option>
            <option v-for="category in ledger.categories" :key="category.id" :value="category.id">
              {{ category.name }} / {{ categoryTypeLabels[category.type] }}
            </option>
          </select>
        </div>
      </div>
    </section>

    <section class="box">
      <header class="box__header">
        <h2>交易列表</h2>
      </header>
      <div class="table-wrap">
        <table v-if="ledger.transactions.length" class="table">
          <thead>
            <tr>
              <th>类型</th>
              <th>时间</th>
              <th>账户</th>
              <th>分类/目标</th>
              <th>金额</th>
              <th>备注</th>
              <th>操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="transaction in ledger.transactions" :key="transaction.id">
              <td>
                <span class="label" :class="`label--${transactionTone[transaction.type]}`">
                  {{ transactionTypeLabels[transaction.type] }}
                </span>
              </td>
              <td>{{ transaction.transactionTime.slice(0, 10) }}</td>
              <td>{{ accountName(transaction.accountId) }}</td>
              <td>{{ transaction.type === 'TRANSFER' ? accountName(transaction.transferAccountId) : categoryName(transaction.categoryId) }}</td>
              <td :class="transaction.type === 'EXPENSE' ? 'amount--expense' : 'amount--income'">
                {{ formatMoney(transaction.amount, transaction.currency) }}
              </td>
              <td>{{ transaction.note || '-' }}</td>
              <td>
                <button class="icon-button" type="button" aria-label="编辑" @click="openEdit(transaction)">
                  <AppIcon name="edit" />
                </button>
                <button class="icon-button button--danger" type="button" aria-label="删除" @click="remove(transaction)">
                  <AppIcon name="delete" />
                </button>
              </td>
            </tr>
          </tbody>
        </table>
        <EmptyState v-else icon="receipt_long" title="暂无交易" text="交易列表为空" />
      </div>
    </section>

    <ModalPanel v-if="modalOpen" :title="form.id ? '编辑交易' : '新增交易'" @close="modalOpen = false">
      <form class="box__body form-grid" @submit.prevent="submit">
        <div class="form-row">
          <label for="tx-type">类型</label>
          <select id="tx-type" v-model="form.type" class="select">
            <option value="INCOME">收入</option>
            <option value="EXPENSE">支出</option>
            <option value="TRANSFER">转账</option>
          </select>
        </div>
        <div class="form-row">
          <label for="tx-account">账户</label>
          <select id="tx-account" v-model="form.accountId" class="select" required>
            <option v-for="account in ledger.accounts" :key="account.id" :value="account.id">{{ account.name }}</option>
          </select>
        </div>
        <div v-if="form.type === 'TRANSFER'" class="form-row">
          <label for="tx-transfer-account">目标账户</label>
          <select id="tx-transfer-account" v-model="form.transferAccountId" class="select" required>
            <option v-for="account in ledger.accounts" :key="account.id" :value="account.id">{{ account.name }}</option>
          </select>
        </div>
        <div v-else class="form-row">
          <label for="tx-category">分类</label>
          <select id="tx-category" v-model="form.categoryId" class="select" required>
            <option v-for="category in availableCategories" :key="category.id" :value="category.id">{{ category.name }}</option>
          </select>
        </div>
        <div class="form-row">
          <label for="tx-amount">金额</label>
          <input id="tx-amount" v-model="form.amount" class="input" type="number" min="0.01" step="0.01" required />
        </div>
        <div class="form-row">
          <label for="tx-currency">币种</label>
          <input id="tx-currency" v-model.trim="form.currency" class="input" maxlength="3" required />
        </div>
        <div class="form-row form-row--full">
          <label for="tx-time">时间</label>
          <input id="tx-time" v-model="form.transactionTime" class="input" type="datetime-local" required />
        </div>
        <div class="form-row form-row--full">
          <label for="tx-note">备注</label>
          <textarea id="tx-note" v-model.trim="form.note" class="textarea" maxlength="500" />
        </div>
        <p v-if="error" class="error-banner form-row--full">{{ error }}</p>
        <div class="form-actions form-row--full">
          <button class="button" type="button" @click="modalOpen = false">取消</button>
          <button class="button button--primary" type="submit" :disabled="saving">保存</button>
        </div>
      </form>
    </ModalPanel>
  </section>
</template>
