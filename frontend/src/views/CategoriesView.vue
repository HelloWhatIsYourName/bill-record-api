<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'

import AppIcon from '@/components/AppIcon.vue'
import EmptyState from '@/components/EmptyState.vue'
import ModalPanel from '@/components/ModalPanel.vue'
import type { Category, CategoryType } from '@/api/types'
import { useLedgerStore } from '@/stores/ledger'
import { categoryTypeLabels } from '@/utils/domain'
import { toErrorMessage } from '@/utils/errors'

type CategoryForm = {
  id: string | null
  name: string
  type: CategoryType
  color: string
  icon: string
}

const ledger = useLedgerStore()
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
  if (!window.confirm(`归档分类 ${category.name}？`)) {
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
        <h1>Categories</h1>
        <p>收入和支出分类</p>
      </div>
      <div class="toolbar">
        <button class="button" type="button" @click="openCreate('INCOME')">
          <AppIcon name="add" />
          收入分类
        </button>
        <button class="button button--primary" type="button" @click="openCreate('EXPENSE')">
          <AppIcon name="add" />
          支出分类
        </button>
      </div>
    </header>

    <p v-if="ledger.error" class="error-banner">{{ ledger.error }}</p>

    <section class="box">
      <header class="box__header">
        <h2>分类列表</h2>
      </header>
      <div class="table-wrap">
        <table v-if="ledger.categories.length" class="table">
          <thead>
            <tr>
              <th>名称</th>
              <th>类型</th>
              <th>颜色</th>
              <th>图标</th>
              <th>操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="category in ledger.categories" :key="category.id">
              <td>{{ category.name }}</td>
              <td>
                <span class="label" :class="category.type === 'INCOME' ? 'label--success' : 'label--danger'">
                  {{ categoryTypeLabels[category.type] }}
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
                <button class="icon-button" type="button" aria-label="编辑" @click="openEdit(category)">
                  <AppIcon name="edit" />
                </button>
                <button class="icon-button button--danger" type="button" aria-label="归档" @click="archive(category)">
                  <AppIcon name="archive" />
                </button>
              </td>
            </tr>
          </tbody>
        </table>
        <EmptyState v-else icon="category" title="暂无分类" text="分类列表为空" />
      </div>
    </section>

    <ModalPanel v-if="modalOpen" :title="form.id ? '编辑分类' : '新增分类'" @close="modalOpen = false">
      <form class="box__body form-grid" @submit.prevent="submit">
        <div class="form-row">
          <label for="category-name">名称</label>
          <input id="category-name" v-model.trim="form.name" class="input" maxlength="80" required />
        </div>
        <div class="form-row">
          <label for="category-type">类型</label>
          <select id="category-type" v-model="form.type" class="select" :disabled="Boolean(form.id)">
            <option value="INCOME">收入</option>
            <option value="EXPENSE">支出</option>
          </select>
        </div>
        <div class="form-row">
          <label for="category-color">颜色</label>
          <input id="category-color" v-model.trim="form.color" class="input" maxlength="20" />
        </div>
        <div class="form-row">
          <label for="category-icon">图标</label>
          <input id="category-icon" v-model.trim="form.icon" class="input" maxlength="40" />
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
