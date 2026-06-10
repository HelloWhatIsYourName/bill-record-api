<script setup lang="ts">
import { computed } from 'vue'
import { RouterLink, RouterView, useRouter } from 'vue-router'

import { useAuthStore } from '@/stores/auth'
import { usePreferencesStore } from '@/stores/preferences'

import AppIcon from './AppIcon.vue'

const auth = useAuthStore()
const preferences = usePreferencesStore()
const router = useRouter()

const navItems = [
  { to: '/', icon: 'dashboard', labelKey: 'nav.dashboard' },
  { to: '/transactions', icon: 'receipt_long', labelKey: 'nav.transactions' },
  { to: '/accounts', icon: 'account_balance_wallet', labelKey: 'nav.accounts' },
  { to: '/categories', icon: 'category', labelKey: 'nav.categories' },
  { to: '/budgets', icon: 'savings', labelKey: 'nav.budgets' },
  { to: '/reports', icon: 'analytics', labelKey: 'nav.reports' },
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
        <button class="button button--subtle" type="button" @click="preferences.toggleLanguage">
          <AppIcon name="translate" />
          {{ preferences.languageLabel }}
        </button>
        <button class="button button--subtle" type="button" @click="logout">
          <AppIcon name="logout" />
          {{ preferences.t('common.logout') }}
        </button>
      </div>
    </header>

    <div class="app-shell__body">
      <aside class="sidebar" :aria-label="preferences.t('nav.dashboard')">
        <RouterLink v-for="item in navItems" :key="item.to" class="nav-item" :to="item.to">
          <AppIcon :name="item.icon" />
          <span>{{ preferences.t(item.labelKey) }}</span>
        </RouterLink>
      </aside>

      <main class="main-content">
        <RouterView />
      </main>
    </div>
  </div>
</template>
