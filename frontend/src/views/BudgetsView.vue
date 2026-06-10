<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'

import AppIcon from '@/components/AppIcon.vue'
import EmptyState from '@/components/EmptyState.vue'
import ModalPanel from '@/components/ModalPanel.vue'
import ProgressBar from '@/components/ProgressBar.vue'
import type { Budget } from '@/api/types'
import { useLedgerStore } from '@/stores/ledger'
import { usePreferencesStore } from '@/stores/preferences'
import { toErrorMessage } from '@/utils/errors'
import { formatMoney, toMonthInputValue } from '@/utils/format'

type BudgetForm = {
  id: string | null
  categoryId: string
  month: string
  amount: string
  currency: string
}

const ledger = useLedgerStore()
const preferences = usePreferencesStore()
const month = ref(ledger.month || toMonthInputValue())
const modalOpen = ref(false)
const saving = ref(false)
const error = ref<string | null>(null)
const form = reactive<BudgetForm>(freshForm())

function freshForm(): BudgetForm {
  return {
    id: null,
    categoryId: '',
    month: month.value,
    amount: '',
    currency: 'CNY',
  }
}

function categoryName(id: string) {
  return ledger.categoryById.get(id)?.name ?? preferences.t('categories.title')
}

function openCreate() {
  Object.assign(form, freshForm(), {
    categoryId: ledger.expenseCategories[0]?.id ?? '',
  })
  error.value = null
  modalOpen.value = true
}

function openEdit(budget: Budget) {
  Object.assign(form, {
    id: budget.id,
    categoryId: budget.categoryId,
    month: budget.month,
    amount: String(budget.amount),
    currency: budget.currency,
  })
  error.value = null
  modalOpen.value = true
}

async function reload() {
  await ledger.withLoading(async () => {
    await ledger.loadReference()
    await ledger.loadBudgets(month.value)
  })
}

async function submit() {
  saving.value = true
  error.value = null
  try {
    if (form.id) {
      await ledger.updateBudget(form.id, { amount: Number(form.amount), currency: form.currency })
    } else {
      await ledger.createBudget({
        categoryId: form.categoryId,
        month: form.month,
        amount: Number(form.amount),
        currency: form.currency,
      })
    }
    month.value = form.month
    modalOpen.value = false
  } catch (requestError) {
    error.value = toErrorMessage(requestError)
  } finally {
    saving.value = false
  }
}

async function remove(budget: Budget) {
  if (!window.confirm(`${preferences.t('common.confirmDelete')} ${categoryName(budget.categoryId)}`)) {
    return
  }
  await ledger.deleteBudget(budget.id)
}

onMounted(reload)
</script>

<template>
  <section class="page">
    <header class="page-header">
      <div>
        <h1>{{ preferences.t('budgets.title') }}</h1>
        <p>{{ month }} {{ preferences.t('budgets.description') }}</p>
      </div>
      <div class="toolbar">
        <input v-model="month" class="input" type="month" @change="reload" />
        <button class="button button--primary" type="button" @click="openCreate">
          <AppIcon name="add" />
          {{ preferences.t('budgets.new') }}
        </button>
      </div>
    </header>

    <p v-if="ledger.error" class="error-banner">{{ ledger.error }}</p>

    <section class="box">
      <header class="box__header">
        <h2>{{ preferences.t('budgets.list') }}</h2>
      </header>
      <div class="table-wrap">
        <table v-if="ledger.budgets.length" class="table">
          <thead>
            <tr>
              <th>{{ preferences.t('categories.title') }}</th>
              <th>{{ preferences.t('budgets.amount') }}</th>
              <th>{{ preferences.t('budgets.spent') }}</th>
              <th>{{ preferences.t('budgets.remaining') }}</th>
              <th>{{ preferences.t('budgets.progress') }}</th>
              <th>{{ preferences.t('common.actions') }}</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="budget in ledger.budgets" :key="budget.id">
              <td>{{ categoryName(budget.categoryId) }}</td>
              <td>{{ formatMoney(budget.amount, budget.currency) }}</td>
              <td class="amount--expense">{{ formatMoney(budget.spent, budget.currency) }}</td>
              <td>{{ formatMoney(budget.remaining, budget.currency) }}</td>
              <td style="min-width: 220px">
                <ProgressBar :label="categoryName(budget.categoryId)" :value="budget.spent" :max="budget.amount" />
              </td>
              <td>
                <button class="icon-button" type="button" :aria-label="preferences.t('common.edit')" @click="openEdit(budget)">
                  <AppIcon name="edit" />
                </button>
                <button class="icon-button button--danger" type="button" :aria-label="preferences.t('common.delete')" @click="remove(budget)">
                  <AppIcon name="delete" />
                </button>
              </td>
            </tr>
          </tbody>
        </table>
        <EmptyState v-else icon="savings" :title="preferences.t('budgets.empty')" :text="preferences.t('common.empty')" />
      </div>
    </section>

    <ModalPanel v-if="modalOpen" :title="form.id ? preferences.t('budgets.edit') : preferences.t('budgets.new')" @close="modalOpen = false">
      <form class="box__body form-grid" @submit.prevent="submit">
        <div class="form-row">
          <label for="budget-month">{{ preferences.t('budgets.month') }}</label>
          <input id="budget-month" v-model="form.month" class="input" type="month" :disabled="Boolean(form.id)" required />
        </div>
        <div class="form-row">
          <label for="budget-category">{{ preferences.t('budgets.category') }}</label>
          <select id="budget-category" v-model="form.categoryId" class="select" :disabled="Boolean(form.id)" required>
            <option v-for="category in ledger.expenseCategories" :key="category.id" :value="category.id">{{ category.name }}</option>
          </select>
        </div>
        <div class="form-row">
          <label for="budget-amount">{{ preferences.t('transactions.amount') }}</label>
          <input id="budget-amount" v-model="form.amount" class="input" type="number" min="0.01" step="0.01" required />
        </div>
        <div class="form-row">
          <label for="budget-currency">{{ preferences.t('common.currency') }}</label>
          <input id="budget-currency" v-model.trim="form.currency" class="input" maxlength="3" required />
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
