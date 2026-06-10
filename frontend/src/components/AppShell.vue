<script setup lang="ts">
import { computed } from 'vue'
import { RouterLink, RouterView, useRouter } from 'vue-router'

import { useAuthStore } from '@/stores/auth'

import AppIcon from './AppIcon.vue'

const auth = useAuthStore()
const router = useRouter()

const navItems = [
  { to: '/', icon: 'dashboard', label: 'Dashboard' },
  { to: '/transactions', icon: 'receipt_long', label: 'Transactions' },
  { to: '/accounts', icon: 'account_balance_wallet', label: 'Accounts' },
  { to: '/categories', icon: 'category', label: 'Categories' },
  { to: '/budgets', icon: 'savings', label: 'Budgets' },
  { to: '/reports', icon: 'analytics', label: 'Reports' },
]

const initials = computed(() => auth.user?.displayName?.slice(0, 1).toUpperCase() ?? 'U')

function logout() {
  auth.logout()
  router.push('/login')
}
</script>

<template>
  <div class="app-shell">
    <header class="topbar">
      <RouterLink class="brand" to="/">
        <span class="brand__mark">BR</span>
        <span>Bill Record</span>
      </RouterLink>
      <div v-if="auth.user" class="topbar__user">
        <span class="avatar">{{ initials }}</span>
        <span class="topbar__name">{{ auth.user.displayName }}</span>
        <button class="button button--subtle" type="button" @click="logout">
          <AppIcon name="logout" />
          退出
        </button>
      </div>
    </header>

    <div class="app-shell__body">
      <aside class="sidebar" aria-label="主导航">
        <RouterLink v-for="item in navItems" :key="item.to" class="nav-item" :to="item.to">
          <AppIcon :name="item.icon" />
          <span>{{ item.label }}</span>
        </RouterLink>
      </aside>

      <main class="main-content">
        <RouterView />
      </main>
    </div>
  </div>
</template>

