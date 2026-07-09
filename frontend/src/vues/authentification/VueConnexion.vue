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
          <h1 class="h3 fw-bold text-dark mb-2 eo-card-title">Connexion à E-OFANA</h1>
          <p class="text-muted small mb-0 eo-card-subtitle">Bienvenue ! Connectez-vous à votre espace.</p>
        </div>

        <!-- appr ,forma,adm -->
        <div class="role-tabs-container mb-4">
          <button 
            class="role-tab" 
            :class="{ active: activeRole === 'apprenant' }"
            @click="switchRole('apprenant')"
            type="button"
          >
            Apprenant
          </button>
          <button 
            class="role-tab" 
            :class="{ active: activeRole === 'formateur' }"
            @click="switchRole('formateur')"
            type="button"
          >
            Formateur
          </button>
          <button 
            class="role-tab" 
            :class="{ active: activeRole === 'admin' }"
            @click="switchRole('admin')"
            type="button"
          >
            Admin
          </button>
        </div>

        <!-- API Error Alert -->
        <div v-if="currentError" class="alert alert-danger eo-alert" role="alert">
          <i class="bi bi-exclamation-triangle-fill me-2"></i>
          {{ currentError }}
        </div>

        <!-- Formulaire -->
        <form @submit.prevent="handleSubmit" novalidate>
          <!-- Email -->
          <div class="mb-4 eo-form-group">
            <label class="form-label eo-form-label" for="email">Adresse email</label>
            <div class="input-icon-container eo-input-container">
              <span class="input-icon eo-input-icon-left">
                <svg fill="none" height="18" stroke="currentColor" stroke-linecap="round" stroke-linejoin="round" stroke-width="2" width="18" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24"><path d="M4 4h16c1.1 0 2 .9 2 2v12c0 1.1-.9 2-2 2H4c-1.1 0-2-.9-2-2V6c0-1.1.9-2 2-2z"></path><polyline points="22,6 12,13 2,6"></polyline></svg>
              </span>
              <input 
                class="form-control eo-form-control" 
                :class="{ 'is-invalid': errors.email }"
                id="email" 
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
          <div class="mb-2 eo-form-group">
            <label class="form-label eo-form-label" for="password">Mot de passe</label>
            <div class="input-icon-container eo-input-container">
              <span class="input-icon eo-input-icon-left">
                <svg fill="none" height="18" stroke="currentColor" stroke-linecap="round" stroke-linejoin="round" stroke-width="2" width="18" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24"><rect height="11" rx="2" ry="2" width="18" x="3" y="11"></rect><path d="M7 11V7a5 5 0 0 1 10 0v4"></path></svg>
              </span>
              <input 
                class="form-control pe-5 eo-form-control" 
                :class="{ 'is-invalid': errors.password }"
                id="password" 
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
                <svg v-if="!showPassword" fill="none" height="18" stroke="currentColor" stroke-linecap="round" stroke-linejoin="round" stroke-width="2" width="18" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24"><path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"></path><circle cx="12" cy="12" r="3"></circle></svg>
                <svg v-else xmlns="http://www.w3.org/2000/svg" width="18" height="18" fill="none" stroke="var(--eofana-gold)" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M17.94 17.94A10.07 10.07 0 0 1 12 19c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19m-6.72-1.07a3 3 0 1 1-4.24-4.24"></path><line x1="1" y1="1" x2="23" y2="23"></line></svg>
              </button>
              <div class="invalid-feedback eo-invalid-feedback" v-if="errors.password">
                {{ errors.password }}
              </div>
            </div>
          </div>

          <!-- Lien mot de passe oublié -->
          <div class="d-flex justify-content-end mb-4 eo-helper-links">
            <router-link to="/mot-de-passe-oublie" class="forgot-link eo-forgot-link">
              Mot de passe oublié ?
            </router-link>
          </div>

          <!-- Bouton de soumission -->
          <button 
            class="btn btn-eofana-dark w-100 d-flex align-items-center justify-content-center gap-2 eo-submit-btn" 
            type="submit"
            :disabled="currentLoading"
          >
            <span v-if="currentLoading" class="spinner-border spinner-border-sm me-2" role="status" aria-hidden="true"></span>
            <span v-else>Se connecter</span>
            <svg fill="none" height="16" stroke="currentColor" stroke-linecap="round" stroke-linejoin="round" stroke-width="2" width="16" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24"><polyline points="9 18 15 12 9 6"></polyline></svg>
          </button>
        </form>

        <!-- Lien de création de compte (seulement pour apprenant) -->
        <div v-if="activeRole === 'apprenant'" class="mt-4 pt-2 text-center small text-muted eo-card-footer">
          Pas encore inscrit ? 
          <router-link to="/inscription" class="register-link ms-1 eo-register-link">
            Créer un compte
          </router-link>
        </div>

        <!-- Message informatif pour formateur/admin -->
        <div v-else class="mt-4 pt-2 text-center small eo-info-message">
          <div class="d-flex align-items-center justify-content-center gap-2 text-muted">
            <svg fill="none" height="16" stroke="currentColor" stroke-linecap="round" stroke-linejoin="round" stroke-width="2" width="16" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24">
              <circle cx="12" cy="12" r="10"></circle>
              <line x1="12" y1="16" x2="12" y2="12"></line>
              <line x1="12" y1="8" x2="12.01" y2="8"></line>
            </svg>
            <span v-if="activeRole === 'formateur'">Vous n'avez pas de compte ? Contactez votre interlocuteur commercial E-ofana.</span>
            <span v-else>Accès réservé aux administrateurs E-OFANA.</span>
          </div>
        </div>
      </div>
    </main>

  </div>
</template>

<script setup>
import { ref, reactive, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../../stores/authStore'
import { useAuthFormateurStore } from '../../stores/authFormateurStore'
import { useAuthAdminStore } from '../../stores/authAdminStore'

const router = useRouter()
const authStore = useAuthStore()
const authFormateurStore = useAuthFormateurStore()
const authAdminStore = useAuthAdminStore()

const activeRole = ref('apprenant')

const form = reactive({
  email: '',
  password: ''
})

const errors = reactive({})
const showPassword = ref(false)

const currentLoading = computed(() => {
  if (activeRole.value === 'apprenant') return authStore.loading
  if (activeRole.value === 'formateur') return authFormateurStore.loading
  return authAdminStore.loading
})

const currentError = computed(() => {
  if (activeRole.value === 'apprenant') return authStore.error
  if (activeRole.value === 'formateur') return authFormateurStore.error
  return authAdminStore.error
})

const switchRole = (role) => {
  activeRole.value = role
  authStore.error = null
  authFormateurStore.error = null
  authAdminStore.error = null
  Object.keys(errors).forEach(key => errors[key] = '')
}

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

  let result

  if (activeRole.value === 'apprenant') {
    result = await authStore.login(form.email, form.password)
    if (result.success) {
      router.push({ name: 'Accueil' })
    }
  } else if (activeRole.value === 'formateur') {
    result = await authFormateurStore.login(form.email, form.password)
    if (result.success) {
      router.push({ name: 'FormateurTableauDeBord' })
    }
  } else {
    result = await authAdminStore.login(form.email, form.password)
    if (result.success) {
      router.push({ name: 'AdminTableauDeBord' })
    }
  }
}
</script>

<style scoped>
/* 
 * Variables CSS (Copied from target design)
 * Suffixes 'eo-' added to classes for scoped isolation and specificity.
 */
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

/* En-tête (Styles copied and scoped) */
.eo-header {
  background-color: var(--eofana-dark);
  color: #ffffff;
  height: 56px;
  padding: 0 2rem;
  position: sticky;
  top: 0;
  z-index: 1050;
}

.eo-header .logo-box {
  background-color: var(--eofana-gold);
  color: var(--eofana-dark);
  padding: 5px;
  border-radius: 4px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
}

.eo-header .btn-header-link {
  color: #ffffff;
  text-decoration: none;
  font-size: 0.9rem;
  font-weight: 500;
  display: inline-flex;
  align-items: center;
  gap: 6px;
  transition: color 0.2s;
  background: none;
  border: none;
  padding: 0;
}

.eo-header .btn-header-link:hover,
.eo-header .btn-header-link.active {
  color: var(--eofana-gold);
}

.eo-header .btn-eofana-gold {
  background-color: var(--eofana-gold);
  color: var(--eofana-dark);
  font-weight: 600;
  font-size: 0.9rem;
  border-radius: var(--radius-custom);
  border: none;
  padding: 6px 16px;
  display: inline-flex;
  align-items: center;
  gap: 6px;
  transition: background-color 0.2s;
}

.eo-header .btn-eofana-gold:hover {
  background-color: var(--eofana-gold-hover);
  color: var(--eofana-dark);
}

/* Carte de connexion (Styles copied and scoped) */
.eo-form-card {
  background: var(--surface);
  border-radius: 1rem;
  border: 1px solid #e0e0e0;
  padding: 3rem;
  max-width: 512px;
  width: 100%;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.05);
}

/* Onglets de Rôles (Static Design Parity) */
.role-tabs-container {
  background-color: #f3f4f6;
  padding: 0.375rem;
  border-radius: 0.75rem;
  display: flex;
}

.role-tab {
  flex: 1;
  padding: 0.625rem 0;
  font-size: 0.875rem;
  border-radius: 0.5rem;
  border: none;
  background: transparent;
  color: var(--text-light);
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s ease;
}

.role-tab.active {
  background-color: var(--eofana-dark);
  color: white;
  font-weight: 700;
}

/* Formulaire (Styles copied and scoped) */
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
  z-index: 4; /* Ensure it stays above input background */
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

.eo-forgot-link {
  color: var(--eofana-gold);
  font-size: 0.875rem;
  font-weight: 600;
  text-decoration: none;
  transition: color 0.2s;
}

.eo-forgot-link:hover {
  color: var(--eofana-dark);
}

.eo-register-link {
  font-weight: 700;
  color: var(--eofana-gold);
  text-decoration: none;
}

.eo-register-link:hover {
  color: var(--eofana-dark);
  text-decoration: underline;
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

/* Alert Integration Style */
.eo-alert {
  border-radius: var(--radius-custom);
  font-size: 0.9rem;
}

.eo-invalid-feedback {
  font-size: 0.8rem;
  margin-top: 0.25rem;
}

/* Pied de page (Styles copied and scoped) */
.eo-footer {
  background-color: var(--eofana-dark);
  color: #9ca3af;
  padding: 2.5rem 0 2rem 0;
  border-top: 1px solid #2d2d2d;
  font-size: 0.9rem;
}

.eo-footer-brand-name {
  color: var(--eofana-gold) !important;
  font-weight: 700;
}

.eo-footer-links a {
  color: #9ca3af;
  text-decoration: none;
  transition: color 0.2s;
}

.eo-footer-links a:hover {
  color: var(--eofana-gold);
}

.eo-footer-copyright {
  color: #555555;
  font-size: 0.85rem;
}

.eo-icon-bg {
  background-color: var(--eofana-dark) !important;
}

.eo-info-message {
  color: var(--text-light);
  line-height: 1.5;
}

/* Media Queries (Parity) */
@media (max-width: 768px) {
  .eo-form-card {
    padding: 1.5rem;
  }
  .eo-header {
    padding: 0 1rem;
  }
}
</style>