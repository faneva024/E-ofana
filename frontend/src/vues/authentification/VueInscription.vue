<template>
  <div class="inscription-container">


    <main class="flex-grow-1 d-flex align-items-center justify-content-center p-3 p-md-5">
      <div class="registration-card">
        <div class="text-center mb-5">
          <div class="logo-badge d-inline-flex p-3 rounded-3 mb-3 shadow-sm">
            <svg style="color: var(--eofana-gold);" fill="currentColor" height="32" viewBox="0 0 16 16" width="32"
              xmlns="http://www.w3.org/2000/svg">
              <path
                d="M8 0 0 4l8 4 8-4L8 0zM1 5.438V8.5c0 1.857 3.134 3.5 7 3.5s7-1.643 7-3.5V5.438L8 8.938 1 5.438zM14 6v4c0 .5-.5 1-1 1s-1-.5-1-1V6l2-1z" />
            </svg>

          </div>
          <h1 class="h3 fw-bold text-dark mb-2">Créer un compte apprenant</h1>
          <p class="text-muted small mb-0">Rejoignez des milliers d'apprenants sur E-OFANA</p>
        </div>

        <div v-if="authStore.error" class="alert alert-danger mb-4" role="alert">
          <svg class="me-2" fill="none" height="18" stroke="currentColor" stroke-linecap="round" stroke-linejoin="round"
            stroke-width="2" width="18" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24">
            <path d="M10.29 3.86L1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z"></path>
            <line x1="12" y1="9" x2="12" y2="13"></line>
            <line x1="12" y1="17" x2="12.01" y2="17"></line>
          </svg>
          {{ authStore.error }}
        </div>

        <form @submit.prevent="handleSubmit" novalidate>
          <div class="row g-3 mb-4">
            <div class="col-12 col-md-6">
              <label class="form-label" for="nom">Nom *</label>
              <div class="input-icon-container">
                <span class="input-icon">
                  <svg fill="none" height="18" stroke="currentColor" stroke-linecap="round" stroke-linejoin="round"
                    stroke-width="2" width="18" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24">
                    <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"></path>
                    <circle cx="12" cy="7" r="4"></circle>
                  </svg>
                </span>
                <input class="form-control" :class="{ 'is-invalid': errors.nom }" id="nom" type="text"
                  v-model="form.nom" @blur="validateField('nom')" placeholder="Votre nom" />
                <div class="invalid-feedback" v-if="errors.nom">
                  {{ errors.nom }}
                </div>
              </div>
            </div>

            <div class="col-12 col-md-6">
              <label class="form-label" for="prenom">Prénom *</label>
              <div class="input-icon-container">
                <span class="input-icon">
                  <svg fill="none" height="18" stroke="currentColor" stroke-linecap="round" stroke-linejoin="round"
                    stroke-width="2" width="18" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24">
                    <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"></path>
                    <circle cx="12" cy="7" r="4"></circle>
                  </svg>
                </span>
                <input class="form-control" :class="{ 'is-invalid': errors.prenom }" id="prenom" type="text"
                  v-model="form.prenom" @blur="validateField('prenom')" placeholder="Votre prénom" />
                <div class="invalid-feedback" v-if="errors.prenom">
                  {{ errors.prenom }}
                </div>
              </div>
            </div>
          </div>

          <div class="mb-4">
            <label class="form-label" for="email">Adresse email *</label>
            <div class="input-icon-container">
              <span class="input-icon">
                <svg fill="none" height="18" stroke="currentColor" stroke-linecap="round" stroke-linejoin="round"
                  stroke-width="2" width="18" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24">
                  <path d="M4 4h16c1.1 0 2 .9 2 2v12c0 1.1-.9 2-2 2H4c-1.1 0-2-.9-2-2V6c0-1.1.9-2 2-2z"></path>
                  <polyline points="22,6 12,13 2,6"></polyline>
                </svg>
              </span>
              <input class="form-control" :class="{ 'is-invalid': errors.email }" id="email" type="email"
                v-model="form.email" @blur="validateField('email')" placeholder="votre@email.com" />
              <div class="invalid-feedback" v-if="errors.email">
                {{ errors.email }}
              </div>
            </div>
          </div>

          <div class="mb-4">
            <label class="form-label" for="telephone">Téléphone</label>
            <div class="input-icon-container">
              <span class="input-icon">
                <svg fill="none" height="18" stroke="currentColor" stroke-linecap="round" stroke-linejoin="round"
                  stroke-width="2" width="18" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24">
                  <path
                    d="M22 16.92v3a2 2 0 0 1-2.18 2 19.79 19.79 0 0 1-8.63-3.07 19.5 19.5 0 0 1-6-6 19.79 19.79 0 0 1-3.07-8.67A2 2 0 0 1 4.11 2h3a2 2 0 0 1 2 1.72 12.84 12.84 0 0 0 .7 2.81 2 2 0 0 1-.45 2.11L8.09 9.91a16 16 0 0 0 6 6l1.27-1.27a2 2 0 0 1 2.11-.45 12.84 12.84 0 0 0 2.81.7A2 2 0 0 1 22 16.92z">
                  </path>
                </svg>
              </span>
              <input class="form-control" :class="{ 'is-invalid': errors.telephone }" id="telephone" type="tel"
                v-model="form.telephone" @blur="validateField('telephone')" placeholder="+261 34 00 000 00" />
              <div class="invalid-feedback" v-if="errors.telephone">
                {{ errors.telephone }}
              </div>
            </div>
          </div>

          <div class="mb-4">
            <label class="form-label" for="password">Mot de passe *</label>
            <div class="input-icon-container">
              <span class="input-icon">
                <svg fill="none" height="18" stroke="currentColor" stroke-linecap="round" stroke-linejoin="round"
                  stroke-width="2" width="18" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24">
                  <rect height="11" rx="2" ry="2" width="18" x="3" y="11"></rect>
                  <path d="M7 11V7a5 5 0 0 1 10 0v4"></path>
                </svg>
              </span>
              <input class="form-control pe-5" :class="{ 'is-invalid': errors.password }" id="password"
                :type="showPassword ? 'text' : 'password'" v-model="form.password" @blur="validateField('password')"
                placeholder="Minimum 8 caractères" />
              <button class="password-toggle" type="button" @click="showPassword = !showPassword">
                <svg v-if="!showPassword" fill="none" height="18" stroke="currentColor" stroke-linecap="round"
                  stroke-linejoin="round" stroke-width="2" width="18" xmlns="http://www.w3.org/2000/svg"
                  viewBox="0 0 24 24">
                  <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"></path>
                  <circle cx="12" cy="12" r="3"></circle>
                </svg>
                <svg v-else fill="none" height="18" stroke="var(--eofana-gold)" stroke-linecap="round"
                  stroke-linejoin="round" stroke-width="2" width="18" xmlns="http://www.w3.org/2000/svg"
                  viewBox="0 0 24 24">
                  <path
                    d="M17.94 17.94A10.07 10.07 0 0 1 12 19c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19m-6.72-1.07a3 3 0 1 1-4.24-4.24">
                  </path>
                  <line x1="1" y1="1" x2="23" y2="23"></line>
                </svg>
              </button>
              <div class="invalid-feedback" v-if="errors.password">
                {{ errors.password }}
              </div>
            </div>

            <div class="password-strength mt-2">
              <div class="progress" style="height: 5px; background-color: #e9ecef;">
                <div class="progress-bar" :class="passwordStrengthClass" :style="{ width: passwordStrength + '%' }">
                </div>
              </div>
              <small class="text-muted text-xs shadow-none">{{ passwordStrengthText }}</small>
            </div>
          </div>

          <div class="mb-4">
            <label class="form-label" for="passwordConfirm">Confirmation mot de passe *</label>
            <div class="input-icon-container">
              <span class="input-icon">
                <svg fill="none" height="18" stroke="currentColor" stroke-linecap="round" stroke-linejoin="round"
                  stroke-width="2" width="18" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24">
                  <rect height="11" rx="2" ry="2" width="18" x="3" y="11"></rect>
                  <path d="M7 11V7a5 5 0 0 1 10 0v4"></path>
                </svg>
              </span>
              <input class="form-control pe-5" :class="{ 'is-invalid': errors.passwordConfirm }" id="passwordConfirm"
                :type="showPasswordConfirm ? 'text' : 'password'" v-model="form.passwordConfirm"
                @blur="validateField('passwordConfirm')" placeholder="Confirmez votre mot de passe" />
              <button class="password-toggle" type="button" @click="showPasswordConfirm = !showPasswordConfirm">
                <svg v-if="!showPasswordConfirm" fill="none" height="18" stroke="currentColor" stroke-linecap="round"
                  stroke-linejoin="round" stroke-width="2" width="18" xmlns="http://www.w3.org/2000/svg"
                  viewBox="0 0 24 24">
                  <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"></path>
                  <circle cx="12" cy="12" r="3"></circle>
                </svg>
                <svg v-else fill="none" height="18" stroke="var(--eofana-gold)" stroke-linecap="round"
                  stroke-linejoin="round" stroke-width="2" width="18" xmlns="http://www.w3.org/2000/svg"
                  viewBox="0 0 24 24">
                  <path
                    d="M17.94 17.94A10.07 10.07 0 0 1 12 19c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19m-6.72-1.07a3 3 0 1 1-4.24-4.24">
                  </path>
                  <line x1="1" y1="1" x2="23" y2="23"></line>
                </svg>
              </button>
              <div class="invalid-feedback" v-if="errors.passwordConfirm">
                {{ errors.passwordConfirm }}
              </div>
            </div>
          </div>

          <div class="mb-5">
            <div class="form-check">
              <input type="checkbox" class="form-check-input text-dark" :class="{ 'is-invalid': errors.conditions }"
                id="conditions" v-model="form.conditions" @change="validateField('conditions')" />
              <label class="form-check-label text-muted small ms-1" for="conditions">
                J'accepte les <a href="#" class="link-gold">conditions d'utilisation</a> et la
                <a href="#" class="link-gold">politique de confidentialité</a> *
              </label>
              <div class="invalid-feedback d-block mt-1" v-if="errors.conditions">
                {{ errors.conditions }}
              </div>
            </div>
          </div>

          <button type="submit" class="btn btn-eofana-dark w-100 d-flex align-items-center justify-content-center gap-2"
            :disabled="authStore.loading">
            <span v-if="authStore.loading" class="spinner-border spinner-border-sm"></span>
            <span>{{ authStore.loading ? 'Inscription en cours...' : 'Créer mon compte' }}</span>
            <svg v-if="!authStore.loading" fill="none" height="16" stroke="currentColor" stroke-linecap="round"
              stroke-linejoin="round" stroke-width="2" width="16" xmlns="http://www.w3.org/2000/svg"
              viewBox="0 0 24 24">
              <polyline points="9 18 15 12 9 6"></polyline>
            </svg>
          </button>
        </form>

        <div class="form-footer mt-4 text-center">
          <p class="text-muted small mb-0">
            Déjà inscrit ?
            <router-link to="/connexion" class="link-gold fw-bold ms-1">
              Connectez-vous
            </router-link>
          </p>
        </div>
      </div>
    </main>

    

    
  </div>
</template>

<script setup>
import { ref, reactive, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../../stores/authStore'

const router = useRouter()
const authStore = useAuthStore()

const form = reactive({
  nom: '',
  prenom: '',
  email: '',
  telephone: '',
  password: '',
  passwordConfirm: '',
  conditions: false
})

const errors = reactive({})
const showPassword = ref(false)
const showPasswordConfirm = ref(false)

const passwordStrength = computed(() => {
  const password = form.password
  let strength = 0

  if (password.length >= 8) strength += 25
  if (password.match(/[a-z]/)) strength += 25
  if (password.match(/[A-Z]/)) strength += 25
  if (password.match(/[0-9]/) || password.match(/[^a-zA-Z0-9]/)) strength += 25

  return strength
})

const passwordStrengthClass = computed(() => {
  const strength = passwordStrength.value
  if (strength <= 25) return 'bg-danger'
  if (strength <= 50) return 'bg-warning'
  if (strength <= 75) return 'bg-info'
  return 'bg-success'
})

const passwordStrengthText = computed(() => {
  const strength = passwordStrength.value
  if (strength <= 25) return 'Très faible'
  if (strength <= 50) return 'Faible'
  if (strength <= 75) return 'Moyen'
  return 'Fort'
})

const validateField = (field) => {
  switch (field) {
    case 'nom':
      errors.nom = form.nom.trim() ? '' : 'Le nom est requis'
      break
    case 'prenom':
      errors.prenom = form.prenom.trim() ? '' : 'Le prénom est requis'
      break
    case 'email':
      const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
      errors.email = emailRegex.test(form.email) ? '' : 'Email invalide'
      break
    case 'telephone':
      if (form.telephone) {
        const phoneRegex = /^\+?[\d\s-]{10,}$/
        errors.telephone = phoneRegex.test(form.telephone) ? '' : 'Numéro de téléphone invalide'
      } else {
        errors.telephone = ''
      }
      break
    case 'password':
      if (form.password.length < 8) {
        errors.password = 'Le mot de passe doit contenir au moins 8 caractères'
      } else {
        errors.password = ''
      }
      if (form.passwordConfirm) {
        validateField('passwordConfirm')
      }
      break
    case 'passwordConfirm':
      errors.passwordConfirm = form.password === form.passwordConfirm ? '' : 'Les mots de passe ne correspondent pas'
      break
    case 'conditions':
      errors.conditions = form.conditions ? '' : 'Vous devez accepter les conditions'
      break
  }
}

const validateForm = () => {
  Object.keys(form).forEach(key => {
    if (key !== 'telephone') {
      validateField(key)
    }
  })

  return !Object.values(errors).some(error => error)
}

const handleSubmit = async () => {
  if (!validateForm()) return

  const { passwordConfirm, conditions, ...userData } = form

  const result = await authStore.register(userData)

  if (result.success) {
    router.push({ name: 'MonEspace' })
  }
}
</script>

<style scoped>
/* @import de la police si elle n'est pas chargée globalement */
@import url('https://fonts.googleapis.com/css2?family=Hanken+Grotesk:wght@400;500;600;700&display=swap');

.inscription-container {
  --eofana-gold: #c69c50;
  --eofana-gold-hover: #b58b45;
  --eofana-dark: #1a1a1a;
  --background: #f5f5f5;
  --surface: #ffffff;
  --radius-custom: 8px;
  --text-light: #777777;

  min-height: 100vh;
  background-color: var(--background);
  font-family: 'Hanken Grotesk', sans-serif;
  display: flex;
  flex-direction: column;
}

/* Header */
header {
  background-color: var(--eofana-dark);
  color: #ffffff;
  height: 56px;
  padding: 0 2rem;
  position: sticky;
  top: 0;
  z-index: 1050;
}

.logo-box {
  background-color: var(--eofana-gold);
  color: var(--eofana-dark);
  padding: 5px;
  border-radius: 4px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
}

.btn-header-link {
  color: #ffffff;
  text-decoration: none;
  font-size: 0.9rem;
  font-weight: 500;
  display: inline-flex;
  align-items: center;
  transition: color 0.2s;
  background: none;
  border: none;
}

.btn-header-link:hover {
  color: var(--eofana-gold);
}

.btn-eofana-gold {
  background-color: var(--eofana-gold);
  color: var(--eofana-dark);
  font-weight: 600;
  font-size: 0.9rem;
  border-radius: var(--radius-custom);
  border: none;
  padding: 6px 16px;
  display: inline-flex;
  align-items: center;
  transition: background-color 0.2s;
}

.btn-eofana-gold:hover {
  background-color: var(--eofana-gold-hover);
  color: var(--eofana-dark);
}

/* Card */
.registration-card {
  background: var(--surface);
  border-radius: 1rem;
  border: 1px solid #e0e0e0;
  padding: 3rem;
  max-width: 600px;
  width: 100%;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.05);
}

.logo-badge {
  background-color: var(--eofana-dark);
}

/* Inputs */
.input-icon-container {
  position: relative;
}

.input-icon {
  position: absolute;
  left: 12px;
  top: 50%;
  transform: translateY(-50%);
  color: #9ca3af;
  display: flex;
  align-items: center;
  z-index: 4;
}

.form-control {
  padding-left: 42px;
  height: 48px;
  border-radius: var(--radius-custom);
  border-color: #dee2e6;
}

.form-control:focus {
  border-color: var(--eofana-gold);
  box-shadow: 0 0 0 0.2rem rgba(198, 156, 80, 0.25);
}

.form-label {
  font-weight: 600;
  font-size: 0.875rem;
  color: #333333;
  margin-bottom: 0.5rem;
}

/* Password & Form Check */
.password-toggle {
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

.form-check-input:checked {
  background-color: var(--eofana-dark);
  border-color: var(--eofana-dark);
}

.link-gold {
  color: var(--eofana-gold);
  text-decoration: none;
  font-weight: 600;
}

.link-gold:hover {
  color: var(--eofana-gold-hover);
  text-decoration: underline;
}

/* Buttons */
.btn-eofana-dark {
  background-color: var(--eofana-dark);
  color: #ffffff;
  font-weight: 600;
  border-radius: var(--radius-custom);
  border: none;
  padding: 0.75rem 1.5rem;
  transition: background-color 0.2s, transform 0.1s;
}

.btn-eofana-dark:hover:not(:disabled) {
  background-color: #333333;
  color: #ffffff;
}

.btn-eofana-dark:disabled {
  opacity: 0.7;
  cursor: not-allowed;
}

/* Floating Navigation */
.floating-nav {
  position: fixed;
  bottom: 20px;
  right: 20px;
  z-index: 1050;
}

.btn-nav {
  background-color: var(--eofana-gold);
  color: var(--eofana-dark);
  border: none;
  padding: 8px 16px;
  border-radius: 4px;
  font-weight: 600;
  font-size: 0.9rem;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
  display: flex;
  align-items: center;
  gap: 8px;
}

/* Footer */
footer {
  background-color: var(--eofana-dark);
  color: #9ca3af;
  padding: 2.5rem 0 2rem 0;
  border-top: 1px solid #2d2d2d;
  font-size: 0.9rem;
  width: 100%;
}

.footer-brand-text {
  color: var(--eofana-gold) !important;
  font-weight: 700;
}

.footer-links a {
  color: #9ca3af;
  text-decoration: none;
  transition: color 0.2s;
}

.footer-links a:hover {
  color: var(--eofana-gold);
}

.footer-copyright {
  color: #555555;
  font-size: 0.85rem;
}

.text-xs {
  font-size: 0.75rem;
}

@media (max-width: 768px) {
  .registration-card {
    padding: 1.5rem;
  }

  header {
    padding: 0 1rem;
  }
}
</style>