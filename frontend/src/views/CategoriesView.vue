<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'

import AppIcon from '@/components/AppIcon.vue'
import EmptyState from '@/components/EmptyState.vue'
import ModalPanel from '@/components/ModalPanel.vue'
import type { Category, CategoryType } from '@/api/types'
import { useLedgerStore } from '@/stores/ledger'
import { usePreferencesStore } from '@/stores/preferences'
import { toErrorMessage } from '@/utils/errors'

type CategoryForm = {
  id: string | null
  name: string
  type: CategoryType
  color: string
  icon: string
}

const ledger = useLedgerStore()
const preferences = usePreferencesStore()
const modalOpen = ref(false)
const saving = ref(false)
const error = ref<string | null>(null)
const form = reactive<CategoryForm>(freshForm())

function freshForm(): CategoryForm {
  return {
    id: null,
    name: '',
    type: 'EXPENSE',
    color: '#0969da',
    icon: 'label',
  }
}

function openCreate(type: CategoryType = 'EXPENSE') {
  Object.assign(form, freshForm(), { type })
  error.value = null
  modalOpen.value = true
}

function openEdit(category: Category) {
  Object.assign(form, {
    id: category.id,
    name: category.name,
    type: category.type,
    color: category.color ?? '#0969da',
    icon: category.icon ?? 'label',
  })
  error.value = null
  modalOpen.value = true
}

async function submit() {
  saving.value = true
  error.value = null
  try {
    if (form.id) {
      await ledger.updateCategory(form.id, { name: form.name, color: form.color, icon: form.icon })
    } else {
      await ledger.createCategory({
        name: form.name,
        type: form.type,
        color: form.color,
        icon: form.icon,
      })
    }
    modalOpen.value = false
  } catch (requestError) {
    error.value = toErrorMessage(requestError)
  } finally {
    saving.value = false
  }
}

async function archive(category: Category) {
  if (!window.confirm(`${preferences.t('common.confirmArchive')} ${category.name}`)) {
    return
  }
  await ledger.archiveCategory(category.id)
}

onMounted(async () => {
  await ledger.withLoading(() => ledger.loadReference())
})
</script>

<template>
  <section class="page">
    <header class="page-header">
      <div>
        <h1>{{ preferences.t('categories.title') }}</h1>
        <p>{{ preferences.t('categories.description') }}</p>
      </div>
      <div class="toolbar">
        <button class="button" type="button" @click="openCreate('INCOME')">
          <AppIcon name="add" />
          {{ preferences.t('categories.newIncome') }}
        </button>
        <button class="button button--primary" type="button" @click="openCreate('EXPENSE')">
          <AppIcon name="add" />
          {{ preferences.t('categories.newExpense') }}
        </button>
      </div>
    </header>

    <p v-if="ledger.error" class="error-banner">{{ ledger.error }}</p>

    <section class="box">
      <header class="box__header">
        <h2>{{ preferences.t('categories.list') }}</h2>
      </header>
      <div class="table-wrap">
        <table v-if="ledger.categories.length" class="table">
          <thead>
            <tr>
              <th>{{ preferences.t('categories.name') }}</th>
              <th>{{ preferences.t('categories.type') }}</th>
              <th>{{ preferences.t('categories.color') }}</th>
              <th>{{ preferences.t('categories.icon') }}</th>
              <th>{{ preferences.t('common.actions') }}</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="category in ledger.categories" :key="category.id">
              <td>{{ category.name }}</td>
              <td>
                <span class="label" :class="category.type === 'INCOME' ? 'label--success' : 'label--danger'">
                  {{ preferences.t(`domain.category.${category.type}`) }}
                </span>
              </td>
              <td>
                <span class="label">
                  <span class="swatch" :style="{ backgroundColor: category.color || '#d0d7de' }" />
                  {{ category.color || '-' }}
                </span>
              </td>
              <td>
                <AppIcon :name="category.icon || 'label'" />
                {{ category.icon || 'label' }}
              </td>
              <td>
                <button class="icon-button" type="button" :aria-label="preferences.t('common.edit')" @click="openEdit(category)">
                  <AppIcon name="edit" />
                </button>
                <button class="icon-button button--danger" type="button" :aria-label="preferences.t('common.archive')" @click="archive(category)">
                  <AppIcon name="archive" />
                </button>
              </td>
            </tr>
          </tbody>
        </table>
        <EmptyState v-else icon="category" :title="preferences.t('categories.empty')" :text="preferences.t('common.empty')" />
      </div>
    </section>

    <ModalPanel v-if="modalOpen" :title="form.id ? preferences.t('categories.edit') : preferences.t('categories.new')" @close="modalOpen = false">
      <form class="box__body form-grid" @submit.prevent="submit">
        <div class="form-row">
          <label for="category-name">{{ preferences.t('categories.name') }}</label>
          <input id="category-name" v-model.trim="form.name" class="input" maxlength="80" required />
        </div>
        <div class="form-row">
          <label for="category-type">{{ preferences.t('categories.type') }}</label>
          <select id="category-type" v-model="form.type" class="select" :disabled="Boolean(form.id)">
            <option value="INCOME">{{ preferences.t('domain.category.INCOME') }}</option>
            <option value="EXPENSE">{{ preferences.t('domain.category.EXPENSE') }}</option>
          </select>
        </div>
        <div class="form-row">
          <label for="category-color">{{ preferences.t('categories.color') }}</label>
          <input id="category-color" v-model.trim="form.color" class="input" maxlength="20" />
        </div>
        <div class="form-row">
          <label for="category-icon">{{ preferences.t('categories.icon') }}</label>
          <input id="category-icon" v-model.trim="form.icon" class="input" maxlength="40" />
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
