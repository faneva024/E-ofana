<template>
  <div class="layout-registration">
    <Header />
    <main class="registration-page">
      <div class="registration-container">
        <div class="registration-card">

          <div class="text-center mb-4">
            <div class="step-indicator">
              <div
                v-for="(s, idx) in steps"
                :key="idx"
                class="step-dot"
                :class="{ active: currentStep === idx, completed: currentStep > idx }"
              >
                <span v-if="currentStep > idx" class="step-check">&#10003;</span>
                <span v-else class="step-num">{{ idx + 1 }}</span>
              </div>
              <div class="step-bar">
                <div class="step-bar-fill" :style="{ width: stepBarWidth }"></div>
              </div>
            </div>
            <h1 class="h4 fw-bold text-dark mb-1">{{ steps[currentStep].title }}</h1>
            <p class="text-muted small mb-0">{{ steps[currentStep].subtitle }}</p>
          </div>

          <form @submit.prevent="handleSubmit" novalidate>

            <div v-show="currentStep === 0">
              <div class="row g-3 mb-4">
                <div class="col-12 col-md-6">
                  <label class="form-label" for="last-name">Nom</label>
                  <div class="input-icon-container">
                    <span class="input-icon">
                      <svg fill="none" height="18" stroke="currentColor" stroke-linecap="round" stroke-linejoin="round" stroke-width="2" width="18" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"></path><circle cx="12" cy="7" r="4"></circle></svg>
                    </span>
                    <input v-model="form.nom" class="form-control" id="last-name" placeholder="RAKOTO" type="text" required />
                  </div>
                </div>
                <div class="col-12 col-md-6">
                  <label class="form-label" for="first-name">Prénom</label>
                  <div class="input-icon-container">
                    <span class="input-icon">
                      <svg fill="none" height="18" stroke="currentColor" stroke-linecap="round" stroke-linejoin="round" stroke-width="2" width="18" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"></path><circle cx="12" cy="7" r="4"></circle></svg>
                    </span>
                    <input v-model="form.prenom" class="form-control" id="first-name" placeholder="Jean" type="text" required />
                  </div>
                </div>
              </div>
              <div class="mb-4">
                <label class="form-label" for="email">Adresse email</label>
                <div class="input-icon-container">
                  <span class="input-icon">
                    <svg fill="none" height="18" stroke="currentColor" stroke-linecap="round" stroke-linejoin="round" stroke-width="2" width="18" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24"><path d="M4 4h16c1.1 0 2 .9 2 2v12c0 1.1-.9 2-2 2H4c-1.1 0-2-.9-2-2V6c0-1.1.9-2 2-2z"></path><polyline points="22,6 12,13 2,6"></polyline></svg>
                  </span>
                  <input v-model="form.email" class="form-control" id="email" placeholder="exemple@email.com" type="email" required />
                </div>
              </div>
              <div class="mb-4">
                <label class="form-label" for="password">Mot de passe</label>
                <div class="input-icon-container">
                  <span class="input-icon">
                    <svg fill="none" height="18" stroke="currentColor" stroke-linecap="round" stroke-linejoin="round" stroke-width="2" width="18" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24"><rect height="11" rx="2" ry="2" width="18" x="3" y="11"></rect><path d="M7 11V7a5 5 0 0 1 10 0v4"></path></svg>
                  </span>
                  <input
                    v-model="form.password"
                    class="form-control pe-5"
                    id="password"
                    placeholder="••••••••"
                    :type="showPassword ? 'text' : 'password'"
                    required
                  />
                  <button class="password-toggle" type="button" @click="showPassword = !showPassword">
                    <svg v-if="!showPassword" fill="none" height="18" stroke="currentColor" stroke-linecap="round" stroke-linejoin="round" stroke-width="2" width="18" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24"><path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"></path><circle cx="12" cy="12" r="3"></circle></svg>
                    <svg v-else fill="none" height="18" stroke="var(--eofana-gold)" stroke-linecap="round" stroke-linejoin="round" stroke-width="2" width="18" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24"><path d="M17.94 17.94A10.07 10.07 0 0 1 12 19c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19m-6.72-1.07a3 3 0 1 1-4.24-4.24"></path><line x1="1" y1="1" x2="23" y2="23"></line></svg>
                  </button>
                </div>
              </div>
              <div class="mb-4">
                <label class="form-label" for="password-confirm">Confirmation mot de passe</label>
                <div class="input-icon-container">
                  <span class="input-icon">
                    <svg fill="none" height="18" stroke="currentColor" stroke-linecap="round" stroke-linejoin="round" stroke-width="2" width="18" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24"><rect height="11" rx="2" ry="2" width="18" x="3" y="11"></rect><path d="M7 11V7a5 5 0 0 1 10 0v4"></path></svg>
                  </span>
                  <input v-model="form.passwordConfirm" class="form-control" id="password-confirm" placeholder="••••••••" type="password" required />
                </div>
              </div>
            </div>

            <div v-show="currentStep === 1">
              <div class="mb-4">
                <label class="form-label">Sélectionnez une ou plusieurs formations</label>
                <div
                  v-for="f in formations"
                  :key="f.id"
                  class="formation-item"
                  :class="{ selected: selectedFormations.includes(f.id) }"
                  @click="toggleFormation(f.id)"
                >
                  <div class="formation-info">
                    <span class="formation-title">{{ f.titre }}</span>
                    <span class="formation-meta">{{ f.ecole }} &middot; {{ f.ville }}</span>
                  </div>
                  <div class="formation-price">
                    <span class="fw-bold">{{ formatPrice(f.prix) }} Ar</span>
                    <span v-if="selectedFormations.includes(f.id)" class="formation-check">&#10003;</span>
                  </div>
                </div>
              </div>
              <div class="price-summary">
                <div class="d-flex justify-content-between mb-1">
                  <span>Frais d'inscription</span>
                  <span>{{ formatPrice(fraisInscription) }} Ar</span>
                </div>
                <div class="d-flex justify-content-between mb-1">
                  <span>Formation(s) ({{ selectedFormations.length }})</span>
                  <span>{{ formatPrice(formationsTotal) }} Ar</span>
                </div>
                <hr class="my-2" />
                <div class="d-flex justify-content-between fw-bold fs-5">
                  <span>Total à payer</span>
                  <span class="text-gold">{{ formatPrice(totalAPayer) }} Ar</span>
                </div>
              </div>
            </div>

            <div v-show="currentStep === 2">
              <div class="recap-section mb-4">
                <h6 class="fw-bold mb-3">Informations personnelles</h6>
                <div class="recap-row"><span>Nom</span><span>{{ form.nom }}</span></div>
                <div class="recap-row"><span>Prénom</span><span>{{ form.prenom }}</span></div>
                <div class="recap-row"><span>Email</span><span>{{ form.email }}</span></div>
              </div>
              <div class="recap-section mb-4">
                <h6 class="fw-bold mb-3">Formations sélectionnées</h6>
                <div v-for="f in selectedFormationsData" :key="f.id" class="recap-row">
                  <span>{{ f.titre }}</span>
                  <span>{{ formatPrice(f.prix) }} Ar</span>
                </div>
              </div>
              <div class="price-summary">
                <div class="d-flex justify-content-between fw-bold fs-5">
                  <span>Total</span>
                  <span class="text-gold">{{ formatPrice(totalAPayer) }} Ar</span>
                </div>
              </div>
            </div>

            <div v-if="apiError" class="alert alert-danger d-flex align-items-center gap-2 mt-3 mb-0" role="alert">
              <svg fill="currentColor" height="18" width="18" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 16 16"><path d="M8.982 1.566a1.13 1.13 0 0 0-1.96 0L.165 13.233c-.457.778.091 1.767.98 1.767h13.713c.889 0 1.438-.99.98-1.767L8.982 1.566zM8 5c.535 0 .954.462.9.995l-.35 3.507a.552.552 0 0 1-1.1 0L7.1 5.995A.905.905 0 0 1 8 5zm.002 6a1 1 0 1 1 0 2 1 1 0 0 1 0-2z"/></svg>
              <span>{{ apiError }}</span>
            </div>

            <div class="d-flex gap-3 mt-4">
              <button
                v-if="currentStep > 0"
                type="button"
                class="btn btn-outline-dark flex-fill d-flex align-items-center justify-content-center gap-2"
                @click="prevStep"
              >
                <svg fill="none" height="16" stroke="currentColor" stroke-linecap="round" stroke-linejoin="round" stroke-width="2" width="16" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24"><polyline points="15 18 9 12 15 6"></polyline></svg>
                <span>Précédent</span>
              </button>
              <button
                v-if="currentStep < steps.length - 1"
                type="button"
                class="btn btn-eofana-dark flex-fill d-flex align-items-center justify-content-center gap-2"
                @click="nextStep"
              >
                <span>Suivant</span>
                <svg fill="none" height="16" stroke="currentColor" stroke-linecap="round" stroke-linejoin="round" stroke-width="2" width="16" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24"><polyline points="9 18 15 12 9 6"></polyline></svg>
              </button>
              <button
                v-if="currentStep === steps.length - 1"
                type="submit"
                class="btn btn-eofana-dark flex-fill d-flex align-items-center justify-content-center gap-2"
                :disabled="submitting"
              >
                <span v-if="submitting">Inscription en cours...</span>
                <span v-else>Créer mon compte</span>
                <svg v-if="!submitting" fill="none" height="16" stroke="currentColor" stroke-linecap="round" stroke-linejoin="round" stroke-width="2" width="16" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24"><polyline points="9 18 15 12 9 6"></polyline></svg>
              </button>
            </div>
          </form>
        </div>
      </div>
    </main>
    <Footer />
  </div>
</template>

<script setup>
import { ref, reactive, computed } from 'vue'
import api from '../api/axios'
import Header from '../components/Header.vue'
import Footer from '../components/Footer.vue'

const currentStep = ref(0)
const showPassword = ref(false)
const submitting = ref(false)
const apiError = ref('')

const form = reactive({
  nom: '',
  prenom: '',
  email: '',
  password: '',
  passwordConfirm: ''
})

const selectedFormations = ref([])

const formations = ref([
  { id: 1, titre: 'Développement Web Full Stack', ecole: 'TechAcademy Antananarivo', ville: 'Antananarivo', prix: 150000 },
  { id: 2, titre: 'Marketing Digital & Réseaux Sociaux', ecole: 'Business School Tana', ville: 'Antananarivo', prix: 80000 },
  { id: 3, titre: 'Comptabilité & Gestion d\'Entreprise', ecole: 'ISCAM Formation', ville: 'Toamasina', prix: 120000 },
  { id: 4, titre: "Gestion d'Exploitation Agricole", ecole: 'Institut Rural Madagascar', ville: 'Antsirabe', prix: 120000 }
])

const steps = [
  { title: 'Créez votre compte apprenant', subtitle: 'Rejoignez des milliers d\'apprenants sur E-OFANA' },
  { title: 'Choisir vos formations', subtitle: 'Sélectionnez une ou plusieurs formations' },
  { title: 'Récapitulatif', subtitle: 'Vérifiez vos informations avant de valider' }
]

const stepBarWidth = computed(() => {
  return ((currentStep.value) / (steps.length - 1)) * 100 + '%'
})

const fraisInscription = 15000

const formationsTotal = computed(() => {
  return selectedFormations.value.reduce((sum, id) => {
    const f = formations.value.find(f => f.id === id)
    return sum + (f ? f.prix : 0)
  }, 0)
})

const totalAPayer = computed(() => {
  return fraisInscription + formationsTotal.value
})

const selectedFormationsData = computed(() => {
  return formations.value.filter(f => selectedFormations.value.includes(f.id))
})

const formatPrice = (value) => {
  return value.toString().replace(/\B(?=(\d{3})+(?!\d))/g, ' ')
}

const toggleFormation = (id) => {
  const idx = selectedFormations.value.indexOf(id)
  if (idx === -1) {
    selectedFormations.value.push(id)
  } else {
    selectedFormations.value.splice(idx, 1)
  }
}

const validateStep0 = () => {
  if (!form.nom || !form.prenom || !form.email || !form.password || !form.passwordConfirm) {
    apiError.value = 'Veuillez remplir tous les champs obligatoires.'
    return false
  }
  if (form.password.length < 6) {
    apiError.value = 'Le mot de passe doit contenir au moins 6 caractères.'
    return false
  }
  if (form.password !== form.passwordConfirm) {
    apiError.value = 'Les mots de passe ne correspondent pas.'
    return false
  }
  return true
}

const validateStep1 = () => {
  if (selectedFormations.value.length === 0) {
    apiError.value = 'Veuillez sélectionner au moins une formation.'
    return false
  }
  return true
}

const nextStep = () => {
  apiError.value = ''
  if (currentStep.value === 0 && !validateStep0()) return
  if (currentStep.value === 1 && !validateStep1()) return
  currentStep.value++
}

const prevStep = () => {
  apiError.value = ''
  currentStep.value--
}

const handleSubmit = async () => {
  apiError.value = ''
  submitting.value = true
  try {
    const payload = {
      nom: form.nom,
      prenom: form.prenom,
      email: form.email,
      password: form.password,
      formationIds: selectedFormations.value
    }
    await api.post('/inscriptions/inscrire', payload)
    currentStep.value = steps.length
  } catch (err) {
    if (err.response && err.response.data && err.response.data.message) {
      apiError.value = err.response.data.message
    } else if (err.message) {
      apiError.value = err.message
    } else {
      apiError.value = 'Une erreur est survenue. Veuillez réessayer.'
    }
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped>
.layout-registration {
  display: flex;
  flex-direction: column;
  min-height: 100vh;
  background-color: #f5f5f5;
  font-family: 'Hanken Grotesk', sans-serif;
}

.registration-page {
  flex-grow: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 2rem 1rem;
}

.registration-container {
  width: 100%;
  max-width: 600px;
}

.registration-card {
  background: #ffffff;
  border-radius: 1rem;
  border: 1px solid #e0e0e0;
  padding: 3rem;
  width: 100%;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.05);
}

.step-indicator {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0.5rem;
  margin-bottom: 1.5rem;
  position: relative;
}

.step-bar {
  position: absolute;
  top: 50%;
  left: 0;
  right: 0;
  height: 3px;
  background: #e0e0e0;
  z-index: 0;
  transform: translateY(-50%);
  border-radius: 2px;
}

.step-bar-fill {
  height: 100%;
  background: #c69c50;
  transition: width 0.3s;
  border-radius: 2px;
}

.step-dot {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: #e0e0e0;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 0.8rem;
  font-weight: 700;
  color: #ffffff;
  z-index: 1;
  transition: background 0.3s;
}

.step-dot.active {
  background: #c69c50;
}

.step-dot.completed {
  background: #c69c50;
}

.step-check {
  font-size: 0.75rem;
}

.step-num {
  font-size: 0.8rem;
}

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
}

.form-control {
  padding-left: 42px;
  height: 48px;
  border-radius: 8px;
  border-color: #dee2e6;
}

.form-control:focus {
  border-color: #c69c50;
  box-shadow: 0 0 0 0.2rem rgba(198, 156, 80, 0.25);
}

.form-label {
  font-weight: 600;
  font-size: 0.875rem;
  color: #333333;
  margin-bottom: 0.5rem;
}

.password-toggle {
  position: absolute;
  right: 12px;
  top: 50%;
  transform: translateY(-50%);
  background: none;
  border: none;
  color: #9ca3af;
  padding: 0;
}

.btn-eofana-dark {
  background-color: #1a1a1a;
  color: #ffffff;
  font-weight: 600;
  border-radius: 8px;
  border: none;
  padding: 0.75rem 1.5rem;
  transition: background-color 0.2s;
}

.btn-eofana-dark:hover {
  background-color: #333333;
  color: #ffffff;
}

.btn-eofana-dark:disabled {
  opacity: 0.65;
  cursor: not-allowed;
}

.btn-outline-dark {
  border-radius: 8px;
  font-weight: 600;
  padding: 0.75rem 1.5rem;
  border-color: #dee2e6;
  color: #333333;
}

.btn-outline-dark:hover {
  background-color: #f5f5f5;
  border-color: #c69c50;
  color: #1a1a1a;
}

.formation-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 1rem 1.25rem;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  margin-bottom: 0.75rem;
  cursor: pointer;
  transition: border-color 0.2s, background 0.2s;
}

.formation-item:hover {
  border-color: #c69c50;
  background: rgba(198, 156, 80, 0.04);
}

.formation-item.selected {
  border-color: #c69c50;
  background: rgba(198, 156, 80, 0.08);
}

.formation-info {
  display: flex;
  flex-direction: column;
}

.formation-title {
  font-weight: 600;
  font-size: 0.95rem;
  color: #1a1a1a;
}

.formation-meta {
  font-size: 0.8rem;
  color: #777777;
  margin-top: 0.15rem;
}

.formation-price {
  display: flex;
  align-items: center;
  gap: 0.75rem;
}

.formation-check {
  color: #c69c50;
  font-weight: 700;
  font-size: 1.1rem;
}

.price-summary {
  background: #f9f9f9;
  border-radius: 8px;
  padding: 1.25rem;
  font-size: 0.9rem;
  color: #333333;
}

.text-gold {
  color: #c69c50;
}

.recap-section {
  background: #f9f9f9;
  border-radius: 8px;
  padding: 1.25rem;
}

.recap-row {
  display: flex;
  justify-content: space-between;
  padding: 0.35rem 0;
  font-size: 0.9rem;
  border-bottom: 1px solid #e0e0e0;
}

.recap-row:last-child {
  border-bottom: none;
}

.alert-danger {
  font-size: 0.85rem;
  border-radius: 8px;
}

@media (max-width: 768px) {
  .registration-card {
    padding: 1.5rem;
  }
}
</style>
