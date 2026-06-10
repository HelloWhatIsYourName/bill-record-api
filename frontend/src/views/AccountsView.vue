<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'

import AppIcon from '@/components/AppIcon.vue'
import EmptyState from '@/components/EmptyState.vue'
import ModalPanel from '@/components/ModalPanel.vue'
import type { Account, AccountType } from '@/api/types'
import { useLedgerStore } from '@/stores/ledger'
import { accountTypeLabels } from '@/utils/domain'
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
  if (!window.confirm(`归档账户 ${account.name}？`)) {
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
        <h1>Accounts</h1>
        <p>现金、银行卡、信用卡和投资账户</p>
      </div>
      <button class="button button--primary" type="button" @click="openCreate">
        <AppIcon name="add" />
        新增账户
      </button>
    </header>

    <p v-if="ledger.error" class="error-banner">{{ ledger.error }}</p>

    <section class="box">
      <header class="box__header">
        <h2>账户列表</h2>
      </header>
      <div class="table-wrap">
        <table v-if="ledger.accounts.length" class="table">
          <thead>
            <tr>
              <th>名称</th>
              <th>类型</th>
              <th>初始余额</th>
              <th>当前余额</th>
              <th>币种</th>
              <th>操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="account in ledger.accounts" :key="account.id">
              <td>{{ account.name }}</td>
              <td>{{ accountTypeLabels[account.type] }}</td>
              <td>{{ formatMoney(account.openingBalance, account.currency) }}</td>
              <td>{{ formatMoney(account.currentBalance, account.currency) }}</td>
              <td>{{ account.currency }}</td>
              <td>
                <button class="icon-button" type="button" aria-label="编辑" @click="openEdit(account)">
                  <AppIcon name="edit" />
                </button>
                <button class="icon-button button--danger" type="button" aria-label="归档" @click="archive(account)">
                  <AppIcon name="archive" />
                </button>
              </td>
            </tr>
          </tbody>
        </table>
        <EmptyState v-else icon="account_balance_wallet" title="暂无账户" text="账户列表为空" />
      </div>
    </section>

    <ModalPanel v-if="modalOpen" :title="form.id ? '编辑账户' : '新增账户'" @close="modalOpen = false">
      <form class="box__body form-grid" @submit.prevent="submit">
        <div class="form-row">
          <label for="account-name">名称</label>
          <input id="account-name" v-model.trim="form.name" class="input" maxlength="80" required />
        </div>
        <div class="form-row">
          <label for="account-type">类型</label>
          <select id="account-type" v-model="form.type" class="select">
            <option v-for="type in accountTypes" :key="type" :value="type">{{ accountTypeLabels[type] }}</option>
          </select>
        </div>
        <div class="form-row">
          <label for="account-balance">初始余额</label>
          <input id="account-balance" v-model="form.openingBalance" class="input" type="number" min="0" step="0.01" :disabled="Boolean(form.id)" required />
        </div>
        <div class="form-row">
          <label for="account-currency">币种</label>
          <input id="account-currency" v-model.trim="form.currency" class="input" maxlength="3" :disabled="Boolean(form.id)" required />
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
