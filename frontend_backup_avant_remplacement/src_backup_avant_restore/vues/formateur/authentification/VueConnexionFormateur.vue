<template>
  <div class="eofana-app-wrapper">
    <main class="eo-main-content d-flex align-items-center justify-content-center p-3 p-md-5">
      <div class="login-card eo-form-card">
        <div class="text-center mb-4 eo-card-header">
          <div class="bg-eofana-dark d-inline-flex p-3 rounded-3 mb-3 shadow-sm eo-icon-bg">
            <svg style="color: var(--eofana-gold);" fill="currentColor" height="32" viewBox="0 0 16 16" width="32" xmlns="http://www.w3.org/2000/svg">
              <path d="M8 0 0 4l8 4 8-4L8 0zM1 5.438V8.5c0 1.857 3.134 3.5 7 3.5s7-1.643 7-3.5V5.438L8 8.938 1 5.438zM14 6v4c0 .5-.5 1-1 1s-1-.5-1-1V6l2-1z"/>
            </svg>
          </div>
          <h1 class="h3 fw-bold text-dark mb-2 eo-card-title">Espace Formateur</h1>
          <p class="text-muted small mb-0 eo-card-subtitle">Connectez-vous à votre espace formateur.</p>
        </div>

        <!-- Alerte erreur API -->
        <div v-if="authFormateurStore.error" class="alert alert-danger eo-alert" role="alert">
          <i class="bi bi-exclamation-triangle-fill me-2"></i>
          {{ authFormateurStore.error }}
        </div>

        <!-- Formulaire -->
        <form @submit.prevent="handleSubmit" novalidate>
          <!-- Email -->
          <div class="mb-4 eo-form-group">
            <label class="form-label eo-form-label" for="formateurEmail">Adresse email *</label>
            <div class="input-icon-container eo-input-container">
              <span class="input-icon eo-input-icon-left">
                <svg fill="none" height="18" stroke="currentColor" stroke-linecap="round" stroke-linejoin="round" stroke-width="2" width="18" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24">
                  <path d="M4 4h16c1.1 0 2 .9 2 2v12c0 1.1-.9 2-2 2H4c-1.1 0-2-.9-2-2V6c0-1.1.9-2 2-2z"></path>
                  <polyline points="22,6 12,13 2,6"></polyline>
                </svg>
              </span>
              <input
                class="form-control eo-form-control"
                :class="{ 'is-invalid': errors.email }"
                id="formateurEmail"
                name="email"
                v-model="form.email"
                @blur="validateField('email')"
                placeholder="exemple@email.com"
                type="email"
                required
              />
              <div class="invalid-feedback eo-invalid-feedback" v-if="errors.email">
                {{ errors.email }}
              </div>
            </div>
          </div>

          <!-- Mot de passe -->
          <div class="mb-4 eo-form-group">
            <label class="form-label eo-form-label" for="formateurPassword">Mot de passe *</label>
            <div class="input-icon-container eo-input-container">
              <span class="input-icon eo-input-icon-left">
                <svg fill="none" height="18" stroke="currentColor" stroke-linecap="round" stroke-linejoin="round" stroke-width="2" width="18" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24">
                  <rect height="11" rx="2" ry="2" width="18" x="3" y="11"></rect>
                  <path d="M7 11V7a5 5 0 0 1 10 0v4"></path>
                </svg>
              </span>
              <input
                class="form-control pe-5 eo-form-control"
                :class="{ 'is-invalid': errors.password }"
                id="formateurPassword"
                name="password"
                v-model="form.password"
                @blur="validateField('password')"
                placeholder="••••••••"
                :type="showPassword ? 'text' : 'password'"
                required
              />
              <button
                class="password-toggle eo-password-toggle"
                type="button"
                @click="showPassword = !showPassword"
                aria-label="Afficher/Masquer le mot de passe"
              >
                <svg v-if="!showPassword" fill="none" height="18" stroke="currentColor" stroke-linecap="round" stroke-linejoin="round" stroke-width="2" width="18" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24">
                  <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"></path>
                  <circle cx="12" cy="12" r="3"></circle>
                </svg>
                <svg v-else xmlns="http://www.w3.org/2000/svg" width="18" height="18" fill="none" stroke="var(--eofana-gold)" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                  <path d="M17.94 17.94A10.07 10.07 0 0 1 12 19c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19m-6.72-1.07a3 3 0 1 1-4.24-4.24"></path>
                  <line x1="1" y1="1" x2="23" y2="23"></line>
                </svg>
              </button>
              <div class="invalid-feedback eo-invalid-feedback" v-if="errors.password">
                {{ errors.password }}
              </div>
            </div>
          </div>

          <!-- Bouton Se connecter -->
          <button
            class="btn btn-eofana-dark w-100 d-flex align-items-center justify-content-center gap-2 eo-submit-btn"
            type="submit"
            :disabled="authFormateurStore.loading"
          >
            <span v-if="authFormateurStore.loading" class="spinner-border spinner-border-sm me-2" role="status" aria-hidden="true"></span>
            <span v-else>Se connecter</span>
            <svg fill="none" height="16" stroke="currentColor" stroke-linecap="round" stroke-linejoin="round" stroke-width="2" width="16" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24">
              <polyline points="9 18 15 12 9 6"></polyline>
            </svg>
          </button>
        </form>

        <!-- Message informatif (pas de lien d'inscription) -->
        <div class="mt-4 pt-2 text-center small eo-info-message">
          <div class="d-flex align-items-center justify-content-center gap-2 text-muted">
            <svg fill="none" height="16" stroke="currentColor" stroke-linecap="round" stroke-linejoin="round" stroke-width="2" width="16" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24">
              <circle cx="12" cy="12" r="10"></circle>
              <line x1="12" y1="16" x2="12" y2="12"></line>
              <line x1="12" y1="8" x2="12.01" y2="8"></line>
            </svg>
            <span>Vous n'avez pas de compte ? Contactez votre interlocuteur commercial E-ofana.</span>
          </div>
        </div>
      </div>
    </main>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthFormateurStore } from '../../../stores/authFormateurStore'

const router = useRouter()
const authFormateurStore = useAuthFormateurStore()

const form = reactive({
  email: '',
  password: ''
})

const errors = reactive({})
const showPassword = ref(false)

const validateField = (field) => {
  switch (field) {
    case 'email':
      const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
      errors.email = emailRegex.test(form.email) ? '' : 'Email invalide'
      break
    case 'password':
      errors.password = form.password ? '' : 'Le mot de passe est requis'
      break
  }
}

const validateForm = () => {
  validateField('email')
  validateField('password')

  return !Object.values(errors).some(error => error)
}

const handleSubmit = async () => {
  if (!validateForm()) return

  const result = await authFormateurStore.login(form.email, form.password)

  if (result.success) {
    router.push({ name: 'FormateurTableauDeBord' })
  }
}
</script>

<style scoped>
.eofana-app-wrapper {
  --eofana-gold: #c69c50;
  --eofana-gold-hover: #b58b45;
  --eofana-dark: #1a1a1a;
  --background: #f5f5f5;
  --surface: #ffffff;
  --radius-custom: 8px;
  --text-light: #777777;

  background-color: var(--background);
  font-family: 'Hanken Grotesk', sans-serif;
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  margin: 0;
}

.eo-icon-bg {
  background-color: var(--eofana-dark) !important;
}

.eo-form-card {
  background: var(--surface);
  border-radius: 1rem;
  border: 1px solid #e0e0e0;
  padding: 3rem;
  max-width: 512px;
  width: 100%;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.05);
}

.eo-card-title {
  color: var(--eofana-dark);
}

.eo-input-container {
  position: relative;
}

.eo-input-icon-left {
  position: absolute;
  left: 12px;
  top: 50%;
  transform: translateY(-50%);
  color: #9ca3af;
  display: flex;
  align-items: center;
  z-index: 4;
}

.eo-form-control {
  padding-left: 42px;
  height: 48px;
  border-radius: var(--radius-custom);
  border-color: #dee2e6;
}

.eo-form-control:focus {
  border-color: var(--eofana-gold);
  box-shadow: 0 0 0 0.2rem rgba(198, 156, 80, 0.25);
}

.eo-form-label {
  font-weight: 600;
  font-size: 0.875rem;
  color: #333333;
  margin-bottom: 0.5rem;
}

.eo-password-toggle {
  position: absolute;
  right: 12px;
  top: 50%;
  transform: translateY(-50%);
  background: none;
  border: none;
  color: #9ca3af;
  padding: 0;
  z-index: 4;
}

.eo-submit-btn {
  background-color: var(--eofana-dark);
  color: #ffffff;
  font-weight: 600;
  border-radius: var(--radius-custom);
  border: none;
  padding: 0.75rem 1.5rem;
  transition: background-color 0.2s;
}

.eo-submit-btn:hover:not(:disabled) {
  background-color: #333333;
  color: #ffffff;
}

.eo-submit-btn:disabled {
  opacity: 0.7;
}

.eo-alert {
  border-radius: var(--radius-custom);
  font-size: 0.9rem;
}

.eo-invalid-feedback {
  font-size: 0.8rem;
  margin-top: 0.25rem;
}

.eo-info-message {
  color: var(--text-light);
  line-height: 1.5;
}

@media (max-width: 768px) {
  .eo-form-card {
    padding: 1.5rem;
  }
}
</style>
