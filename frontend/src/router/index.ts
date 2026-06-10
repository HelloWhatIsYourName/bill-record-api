import { createRouter, createWebHistory } from 'vue-router'

import AccountsView from '@/views/AccountsView.vue'
import BudgetsView from '@/views/BudgetsView.vue'
import CategoriesView from '@/views/CategoriesView.vue'
import DashboardView from '@/views/DashboardView.vue'
import LoginView from '@/views/LoginView.vue'
import ReportsView from '@/views/ReportsView.vue'
import TransactionsView from '@/views/TransactionsView.vue'
import { useAuthStore } from '@/stores/auth'

export const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/login', name: 'login', component: LoginView, meta: { public: true } },
    { path: '/', name: 'dashboard', component: DashboardView },
    { path: '/transactions', name: 'transactions', component: TransactionsView },
    { path: '/accounts', name: 'accounts', component: AccountsView },
    { path: '/categories', name: 'categories', component: CategoriesView },
    { path: '/budgets', name: 'budgets', component: BudgetsView },
    { path: '/reports', name: 'reports', component: ReportsView },
  ],
})

router.beforeEach((to) => {
  const auth = useAuthStore()
  auth.bootstrapFromStorage()

  if (to.meta.public && auth.isAuthenticated) {
    return '/'
  }

  if (!to.meta.public && !auth.isAuthenticated) {
    return '/login'
  }

  return true
})

