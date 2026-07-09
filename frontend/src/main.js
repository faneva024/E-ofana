import { createApp } from 'vue'
import { createPinia } from 'pinia'
import { useAuthStore } from './stores/authStore'
import { useAuthFormateurStore } from './stores/authFormateurStore'
import router from './router'
import App from './App.vue'
import OptimizedImage from './components/OptimizedImage.vue'
import './assets/scss/main.scss'
import './styles/variables.css'

const app = createApp(App)
const pinia = createPinia()
app.use(pinia)
app.use(router)
app.component('OptimizedImage', OptimizedImage)

const auth = useAuthStore()
auth.loadFromStorage()

const authFormateur = useAuthFormateurStore()
// 🔧 Auto-connexion en dev pour tester sans backend
if (import.meta.env.DEV && !localStorage.getItem('authFormateur')) {
  localStorage.setItem('authFormateur', JSON.stringify({
    formateur: { id: 1, nom: 'Formateur Test', email: 'test@test.com' },
    token: 'mock-token-formateur-123'
  }))
}
authFormateur.loadFromStorage()

app.mount('#app')
