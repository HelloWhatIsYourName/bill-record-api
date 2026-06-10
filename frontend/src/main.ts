import 'material-symbols/outlined.css'
import './styles/main.css'

import { createPinia } from 'pinia'
import { createApp } from 'vue'

import App from './App.vue'
import { router } from './router'
import { useAuthStore } from './stores/auth'

const app = createApp(App)
const pinia = createPinia()

app.use(pinia)
app.use(router)

const auth = useAuthStore(pinia)
auth.bootstrapFromStorage()

window.addEventListener('bill-record-unauthorized', () => {
  auth.logout()
  router.push('/login')
})

app.mount('#app')

