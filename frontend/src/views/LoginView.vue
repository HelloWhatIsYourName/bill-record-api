<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'

import { useAuthStore } from '@/stores/auth'
import { usePreferencesStore } from '@/stores/preferences'
import { toErrorMessage } from '@/utils/errors'

const auth = useAuthStore()
const preferences = usePreferencesStore()
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
        <p>{{ preferences.t('app.subtitle') }}</p>
      </header>
      <div class="login-panel__body">
        <div class="segmented" role="group" :aria-label="preferences.t('auth.mode')">
          <button type="button" :aria-pressed="mode === 'login'" @click="mode = 'login'">
            {{ preferences.t('auth.login') }}
          </button>
          <button type="button" :aria-pressed="mode === 'register'" @click="mode = 'register'">
            {{ preferences.t('auth.register') }}
          </button>
        </div>

        <form class="form-grid" @submit.prevent="submit">
          <div class="form-row form-row--full">
            <label for="email">{{ preferences.t('auth.email') }}</label>
            <input id="email" v-model.trim="form.email" class="input" type="email" autocomplete="email" required />
          </div>

          <div v-if="mode === 'register'" class="form-row form-row--full">
            <label for="display-name">{{ preferences.t('auth.displayName') }}</label>
            <input id="display-name" v-model.trim="form.displayName" class="input" type="text" maxlength="80" />
          </div>

          <div class="form-row form-row--full">
            <label for="password">{{ preferences.t('auth.password') }}</label>
            <input id="password" v-model="form.password" class="input" type="password" autocomplete="current-password" minlength="8" required />
          </div>

          <p v-if="error" class="error-banner form-row--full">{{ error }}</p>

          <div class="form-actions form-row--full">
            <button class="button button--primary" type="submit" :disabled="auth.loading">
              {{ mode === 'login' ? preferences.t('auth.login') : preferences.t('auth.register') }}
            </button>
          </div>
        </form>
      </div>
    </section>
  </main>
</template>
