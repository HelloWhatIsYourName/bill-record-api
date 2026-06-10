<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'

import AppIcon from '@/components/AppIcon.vue'
import EmptyState from '@/components/EmptyState.vue'
import ModalPanel from '@/components/ModalPanel.vue'
import type { Account, AccountType } from '@/api/types'
import { useLedgerStore } from '@/stores/ledger'
import { usePreferencesStore } from '@/stores/preferences'
import { toErrorMessage } from '@/utils/errors'
import { formatMoney } from '@/utils/format'

type AccountForm = {
  id: string | null
  name: string
  type: AccountType
  openingBalance: string
  currency: string
}

const ledger = useLedgerStore()
const preferences = usePreferencesStore()
const modalOpen = ref(false)
const saving = ref(false)
const error = ref<string | null>(null)
const form = reactive<AccountForm>(freshForm())
const accountTypes: AccountType[] = ['CASH', 'BANK', 'CREDIT_CARD', 'INVESTMENT', 'OTHER']

function freshForm(): AccountForm {
  return {
    id: null,
    name: '',
    type: 'BANK',
    openingBalance: '0.00',
    currency: 'CNY',
  }
}

function openCreate() {
  Object.assign(form, freshForm())
  error.value = null
  modalOpen.value = true
}

function openEdit(account: Account) {
  Object.assign(form, {
    id: account.id,
    name: account.name,
    type: account.type,
    openingBalance: String(account.openingBalance),
    currency: account.currency,
  })
  error.value = null
  modalOpen.value = true
}

async function submit() {
  saving.value = true
  error.value = null
  try {
    if (form.id) {
      await ledger.updateAccount(form.id, { name: form.name, type: form.type })
    } else {
      await ledger.createAccount({
        name: form.name,
        type: form.type,
        openingBalance: Number(form.openingBalance),
        currency: form.currency,
      })
    }
    modalOpen.value = false
  } catch (requestError) {
    error.value = toErrorMessage(requestError)
  } finally {
    saving.value = false
  }
}

async function archive(account: Account) {
  if (!window.confirm(`${preferences.t('common.confirmArchive')} ${account.name}`)) {
    return
  }
  await ledger.archiveAccount(account.id)
}

onMounted(async () => {
  await ledger.withLoading(() => ledger.loadReference())
})
</script>

<template>
  <section class="page">
    <header class="page-header">
      <div>
        <h1>{{ preferences.t('accounts.title') }}</h1>
        <p>{{ preferences.t('accounts.description') }}</p>
      </div>
      <button class="button button--primary" type="button" @click="openCreate">
        <AppIcon name="add" />
        {{ preferences.t('accounts.new') }}
      </button>
    </header>

    <p v-if="ledger.error" class="error-banner">{{ ledger.error }}</p>

    <section class="box">
      <header class="box__header">
        <h2>{{ preferences.t('accounts.list') }}</h2>
      </header>
      <div class="table-wrap">
        <table v-if="ledger.accounts.length" class="table">
          <thead>
            <tr>
              <th>{{ preferences.t('accounts.name') }}</th>
              <th>{{ preferences.t('accounts.type') }}</th>
              <th>{{ preferences.t('accounts.openingBalance') }}</th>
              <th>{{ preferences.t('accounts.currentBalance') }}</th>
              <th>{{ preferences.t('common.currency') }}</th>
              <th>{{ preferences.t('common.actions') }}</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="account in ledger.accounts" :key="account.id">
              <td>{{ account.name }}</td>
              <td>{{ preferences.t(`domain.account.${account.type}`) }}</td>
              <td>{{ formatMoney(account.openingBalance, account.currency) }}</td>
              <td>{{ formatMoney(account.currentBalance, account.currency) }}</td>
              <td>{{ account.currency }}</td>
              <td>
                <button class="icon-button" type="button" :aria-label="preferences.t('common.edit')" @click="openEdit(account)">
                  <AppIcon name="edit" />
                </button>
                <button class="icon-button button--danger" type="button" :aria-label="preferences.t('common.archive')" @click="archive(account)">
                  <AppIcon name="archive" />
                </button>
              </td>
            </tr>
          </tbody>
        </table>
        <EmptyState v-else icon="account_balance_wallet" :title="preferences.t('accounts.empty')" :text="preferences.t('common.empty')" />
      </div>
    </section>

    <ModalPanel v-if="modalOpen" :title="form.id ? preferences.t('accounts.edit') : preferences.t('accounts.new')" @close="modalOpen = false">
      <form class="box__body form-grid" @submit.prevent="submit">
        <div class="form-row">
          <label for="account-name">{{ preferences.t('accounts.name') }}</label>
          <input id="account-name" v-model.trim="form.name" class="input" maxlength="80" required />
        </div>
        <div class="form-row">
          <label for="account-type">{{ preferences.t('accounts.type') }}</label>
          <select id="account-type" v-model="form.type" class="select">
            <option v-for="type in accountTypes" :key="type" :value="type">{{ preferences.t(`domain.account.${type}`) }}</option>
          </select>
        </div>
        <div class="form-row">
          <label for="account-balance">{{ preferences.t('accounts.openingBalance') }}</label>
          <input id="account-balance" v-model="form.openingBalance" class="input" type="number" min="0" step="0.01" :disabled="Boolean(form.id)" required />
        </div>
        <div class="form-row">
          <label for="account-currency">{{ preferences.t('common.currency') }}</label>
          <input id="account-currency" v-model.trim="form.currency" class="input" maxlength="3" :disabled="Boolean(form.id)" required />
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
