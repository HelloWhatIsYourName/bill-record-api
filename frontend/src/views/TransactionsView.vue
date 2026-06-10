<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'

import AppIcon from '@/components/AppIcon.vue'
import EmptyState from '@/components/EmptyState.vue'
import ModalPanel from '@/components/ModalPanel.vue'
import { useLedgerStore } from '@/stores/ledger'
import { usePreferencesStore } from '@/stores/preferences'
import { transactionTone } from '@/utils/domain'
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
const preferences = usePreferencesStore()
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
    return preferences.t('transactions.account')
  }
  return ledger.accountById.get(id)?.name ?? preferences.t('transactions.account')
}

function categoryName(id?: string | null) {
  if (!id) {
    return preferences.t('common.none')
  }
  return ledger.categoryById.get(id)?.name ?? preferences.t('categories.title')
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
  if (!window.confirm(preferences.t('common.confirmDelete'))) {
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
        <p>{{ preferences.t('transactions.description') }}</p>
      </div>
      <button class="button button--primary" type="button" @click="openCreate">
        <AppIcon name="add" />
        {{ preferences.t('transactions.new') }}
      </button>
    </header>

    <p v-if="ledger.error" class="error-banner">{{ ledger.error }}</p>

    <section class="box">
      <header class="box__header">
        <h2>{{ preferences.t('transactions.filters') }}</h2>
        <button class="button" type="button" @click="applyFilters">{{ preferences.t('common.apply') }}</button>
      </header>
      <div class="box__body form-grid">
        <div class="form-row">
          <label for="filter-type">{{ preferences.t('transactions.type') }}</label>
          <select id="filter-type" v-model="filters.type" class="select">
            <option value="">{{ preferences.t('domain.all') }}</option>
            <option value="INCOME">{{ preferences.t('domain.transaction.INCOME') }}</option>
            <option value="EXPENSE">{{ preferences.t('domain.transaction.EXPENSE') }}</option>
            <option value="TRANSFER">{{ preferences.t('domain.transaction.TRANSFER') }}</option>
          </select>
        </div>
        <div class="form-row">
          <label for="filter-account">{{ preferences.t('transactions.account') }}</label>
          <select id="filter-account" v-model="filters.accountId" class="select">
            <option value="">{{ preferences.t('domain.all') }}</option>
            <option v-for="account in ledger.accounts" :key="account.id" :value="account.id">{{ account.name }}</option>
          </select>
        </div>
        <div class="form-row">
          <label for="filter-category">{{ preferences.t('categories.title') }}</label>
          <select id="filter-category" v-model="filters.categoryId" class="select">
            <option value="">{{ preferences.t('domain.all') }}</option>
            <option v-for="category in ledger.categories" :key="category.id" :value="category.id">
              {{ category.name }} / {{ preferences.t(`domain.category.${category.type}`) }}
            </option>
          </select>
        </div>
      </div>
    </section>

    <section class="box">
      <header class="box__header">
        <h2>{{ preferences.t('transactions.list') }}</h2>
      </header>
      <div class="table-wrap">
        <table v-if="ledger.transactions.length" class="table">
          <thead>
            <tr>
              <th>{{ preferences.t('transactions.type') }}</th>
              <th>{{ preferences.t('transactions.time') }}</th>
              <th>{{ preferences.t('transactions.account') }}</th>
              <th>{{ preferences.t('transactions.categoryOrTarget') }}</th>
              <th>{{ preferences.t('transactions.amount') }}</th>
              <th>{{ preferences.t('transactions.note') }}</th>
              <th>{{ preferences.t('common.actions') }}</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="transaction in ledger.transactions" :key="transaction.id">
              <td>
                <span class="label" :class="`label--${transactionTone[transaction.type]}`">
                  {{ preferences.t(`domain.transaction.${transaction.type}`) }}
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
                <button class="icon-button" type="button" :aria-label="preferences.t('common.edit')" @click="openEdit(transaction)">
                  <AppIcon name="edit" />
                </button>
                <button class="icon-button button--danger" type="button" :aria-label="preferences.t('common.delete')" @click="remove(transaction)">
                  <AppIcon name="delete" />
                </button>
              </td>
            </tr>
          </tbody>
        </table>
        <EmptyState v-else icon="receipt_long" :title="preferences.t('transactions.empty')" :text="preferences.t('empty.transactions')" />
      </div>
    </section>

    <ModalPanel v-if="modalOpen" :title="form.id ? preferences.t('transactions.edit') : preferences.t('transactions.new')" @close="modalOpen = false">
      <form class="box__body form-grid" @submit.prevent="submit">
        <div class="form-row">
          <label for="tx-type">{{ preferences.t('transactions.type') }}</label>
          <select id="tx-type" v-model="form.type" class="select">
            <option value="INCOME">{{ preferences.t('domain.transaction.INCOME') }}</option>
            <option value="EXPENSE">{{ preferences.t('domain.transaction.EXPENSE') }}</option>
            <option value="TRANSFER">{{ preferences.t('domain.transaction.TRANSFER') }}</option>
          </select>
        </div>
        <div class="form-row">
          <label for="tx-account">{{ preferences.t('transactions.account') }}</label>
          <select id="tx-account" v-model="form.accountId" class="select" required>
            <option v-for="account in ledger.accounts" :key="account.id" :value="account.id">{{ account.name }}</option>
          </select>
        </div>
        <div v-if="form.type === 'TRANSFER'" class="form-row">
          <label for="tx-transfer-account">{{ preferences.t('transactions.targetAccount') }}</label>
          <select id="tx-transfer-account" v-model="form.transferAccountId" class="select" required>
            <option v-for="account in ledger.accounts" :key="account.id" :value="account.id">{{ account.name }}</option>
          </select>
        </div>
        <div v-else class="form-row">
          <label for="tx-category">{{ preferences.t('categories.title') }}</label>
          <select id="tx-category" v-model="form.categoryId" class="select" required>
            <option v-for="category in availableCategories" :key="category.id" :value="category.id">{{ category.name }}</option>
          </select>
        </div>
        <div class="form-row">
          <label for="tx-amount">{{ preferences.t('transactions.amount') }}</label>
          <input id="tx-amount" v-model="form.amount" class="input" type="number" min="0.01" step="0.01" required />
        </div>
        <div class="form-row">
          <label for="tx-currency">{{ preferences.t('common.currency') }}</label>
          <input id="tx-currency" v-model.trim="form.currency" class="input" maxlength="3" required />
        </div>
        <div class="form-row form-row--full">
          <label for="tx-time">{{ preferences.t('transactions.time') }}</label>
          <input id="tx-time" v-model="form.transactionTime" class="input" type="datetime-local" required />
        </div>
        <div class="form-row form-row--full">
          <label for="tx-note">{{ preferences.t('transactions.note') }}</label>
          <textarea id="tx-note" v-model.trim="form.note" class="textarea" maxlength="500" />
        </div>
        <p v-if="error" class="error-banner form-row--full">{{ error }}</p>
        <div class="form-actions form-row--full">
          <button class="button" type="button" @click="modalOpen = false">{{ preferences.t('common.cancel') }}</button>
          <button class="button button--primary" type="submit" :disabled="saving">{{ preferences.t('common.save') }}</button>
        </div>
      </form>
    </ModalPanel>
  </section>
</template>
