<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'

import { useAuthStore } from '@/stores/auth'
import { toErrorMessage } from '@/utils/errors'

const auth = useAuthStore()
const router = useRouter()
const mode = ref<'login' | 'register'>('login')
const error = ref<string | null>(null)
const form = reactive({
  email: '',
  password: '',
  displayName: '',
})

async function submit() {
  error.value = null
  try {
    if (mode.value === 'login') {
      await auth.login({ email: form.email, password: form.password })
    } else {
      await auth.register({
        email: form.email,
        password: form.password,
        displayName: form.displayName || form.email.split('@')[0],
      })
    }
    router.push('/')
  } catch (requestError) {
    error.value = toErrorMessage(requestError)
  }
}
</script>

<template>
  <main class="login-page">
    <section class="login-panel">
      <header class="login-panel__header">
        <h1>Bill Record</h1>
        <p>个人记账工作台</p>
      </header>
      <div class="login-panel__body">
        <div class="segmented" role="group" aria-label="认证模式">
          <button type="button" :aria-pressed="mode === 'login'" @click="mode = 'login'">登录</button>
          <button type="button" :aria-pressed="mode === 'register'" @click="mode = 'register'">注册</button>
        </div>

        <form class="form-grid" @submit.prevent="submit">
          <div class="form-row form-row--full">
            <label for="email">邮箱</label>
            <input id="email" v-model.trim="form.email" class="input" type="email" autocomplete="email" required />
          </div>

          <div v-if="mode === 'register'" class="form-row form-row--full">
            <label for="display-name">名称</label>
            <input id="display-name" v-model.trim="form.displayName" class="input" type="text" maxlength="80" />
          </div>

          <div class="form-row form-row--full">
            <label for="password">密码</label>
            <input id="password" v-model="form.password" class="input" type="password" autocomplete="current-password" minlength="8" required />
          </div>

          <p v-if="error" class="error-banner form-row--full">{{ error }}</p>

          <div class="form-actions form-row--full">
            <button class="button button--primary" type="submit" :disabled="auth.loading">
              {{ mode === 'login' ? '登录' : '注册' }}
            </button>
          </div>
        </form>
      </div>
    </section>
  </main>
</template>

