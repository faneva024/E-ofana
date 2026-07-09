<template>
  <div class="layout-reservation">
    <main class="reservation-page">
      <div class="reservation-container">
        <div class="reservation-card">

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
            <p class="text-muted small mb-0">
              {{ currentStep === 0 ? 'Réservez votre place en payant uniquement la commission' : steps[currentStep].subtitle }}
            </p>
          </div>

          <form @submit.prevent="handleSubmit" novalidate>

            <div v-show="currentStep === 0">
              <div class="row g-3 mb-4">
                <div class="col-12 col-md-6">
                  <label class="form-label" for="res-nom">Nom</label>
                  <div class="input-icon-container">
                    <span class="input-icon">
                      <svg fill="none" height="18" stroke="currentColor" stroke-linecap="round" stroke-linejoin="round" stroke-width="2" width="18" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"></path><circle cx="12" cy="7" r="4"></circle></svg>
                    </span>
                    <input v-model="form.nom" class="form-control" id="res-nom" placeholder="RAKOTO" type="text" required />
                  </div>
                </div>
                <div class="col-12 col-md-6">
                  <label class="form-label" for="res-prenom">Prénom</label>
                  <div class="input-icon-container">
                    <span class="input-icon">
                      <svg fill="none" height="18" stroke="currentColor" stroke-linecap="round" stroke-linejoin="round" stroke-width="2" width="18" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"></path><circle cx="12" cy="7" r="4"></circle></svg>
                    </span>
                    <input v-model="form.prenom" class="form-control" id="res-prenom" placeholder="Jean" type="text" required />
                  </div>
                </div>
              </div>
              <div class="mb-4">
                <label class="form-label" for="res-email">Adresse email</label>
                <div class="input-icon-container">
                  <span class="input-icon">
                    <svg fill="none" height="18" stroke="currentColor" stroke-linecap="round" stroke-linejoin="round" stroke-width="2" width="18" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24"><path d="M4 4h16c1.1 0 2 .9 2 2v12c0 1.1-.9 2-2 2H4c-1.1 0-2-.9-2-2V6c0-1.1.9-2 2-2z"></path><polyline points="22,6 12,13 2,6"></polyline></svg>
                  </span>
                  <input v-model="form.email" class="form-control" id="res-email" placeholder="exemple@email.com" type="email" required />
                </div>
              </div>
            </div>

            <div v-show="currentStep === 1">
              <div class="formation-item selected mb-4" style="cursor: default;">
                <div class="formation-info">
                  <span class="formation-title">{{ formation.titre }}</span>
                  <span class="formation-meta">{{ formation.ecole }} &middot; {{ formation.ville }}</span>
                </div>
                <div class="formation-price">
                  <span class="fw-bold">{{ formatPrice(formation.prix) }} Ar</span>
                </div>
              </div>

              <div class="mb-4">
                <label class="form-label">Type de commission</label>
                <div class="d-flex gap-3">
                  <label
                    class="commission-option"
                    :class="{ selected: commissionRate === 0.07 }"
                  >
                    <input v-model="commissionRate" type="radio" :value="0.07" class="d-none" />
                    <span class="commission-label">Commission standard</span>
                    <span class="commission-value">7 %</span>
                  </label>
                  <label
                    class="commission-option"
                    :class="{ selected: commissionRate === 0.15 }"
                  >
                    <input v-model="commissionRate" type="radio" :value="0.15" class="d-none" />
                    <span class="commission-label">Commission premium</span>
                    <span class="commission-value">15 %</span>
                  </label>
                </div>
              </div>

              <div class="price-summary">
                <div class="d-flex justify-content-between mb-1">
                  <span>Prix de la formation</span>
                  <span>{{ formatPrice(formation.prix) }} Ar</span>
                </div>
                <div class="d-flex justify-content-between mb-1">
                  <span>Commission ({{ (commissionRate * 100).toFixed(0) }} %)</span>
                  <span>{{ formatPrice(commissionMontant) }} Ar</span>
                </div>
                <hr class="my-2" />
                <div class="d-flex justify-content-between fw-bold fs-5">
                  <span>Montant à payer</span>
                  <span class="text-gold">{{ formatPrice(commissionMontant) }} Ar</span>
                </div>
                <p class="text-muted small mt-2 mb-0">
                  <em>Réservez votre place en payant uniquement la commission.</em>
                </p>
              </div>
            </div>

            <div v-show="currentStep === 2">
              <div class="recap-section mb-4">
                <h6 class="fw-bold mb-3">Vos informations</h6>
                <div class="recap-row"><span>Nom</span><span>{{ form.nom }} {{ form.prenom }}</span></div>
                <div class="recap-row"><span>Email</span><span>{{ form.email }}</span></div>
              </div>
              <div class="recap-section mb-4">
                <h6 class="fw-bold mb-3">Formation réservée</h6>
                <div class="recap-row"><span>{{ formation.titre }}</span><span>{{ formatPrice(formation.prix) }} Ar</span></div>
                <div class="recap-row"><span>Commission ({{ (commissionRate * 100).toFixed(0) }} %)</span><span>{{ formatPrice(commissionMontant) }} Ar</span></div>
              </div>
              <div class="price-summary">
                <div class="d-flex justify-content-between fw-bold fs-5">
                  <span>Total à payer</span>
                  <span class="text-gold">{{ formatPrice(commissionMontant) }} Ar</span>
                </div>
              </div>
            </div>

            <div v-if="apiError" class="alert alert-danger d-flex align-items-center gap-2 mt-3 mb-0" role="alert">
              <svg fill="currentColor" height="18" width="18" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 16 16"><path d="M8.982 1.566a1.13 1.13 0 0 0-1.96 0L.165 13.233c-.457.778.091 1.767.98 1.767h13.713c.889 0 1.438-.99.98-1.767L8.982 1.566zM8 5c.535 0 .954.462.9.995l-.35 3.507a.552.552 0 0 1-1.1 0L7.1 5.995A.905.905 0 0 1 8 5zm.002 6a1 1 0 1 1 0 2 1 1 0 0 1 0-2z"/></svg>
              <span>{{ apiError }}</span>
            </div>

          </form>

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
              type="button"
              class="btn btn-eofana-dark flex-fill d-flex align-items-center justify-content-center gap-2"
              :disabled="submitting"
              @click="handleSubmit"
            >
              <span v-if="submitting">Réservation en cours...</span>
              <span v-else>Confirmer la réservation</span>
              <svg v-if="!submitting" fill="none" height="16" stroke="currentColor" stroke-linecap="round" stroke-linejoin="round" stroke-width="2" width="16" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24"><polyline points="9 18 15 12 9 6"></polyline></svg>
            </button>
          </div>

        </div>
      </div>
    </main>
  </div>
</template>

<script setup>
import { ref, reactive, computed } from 'vue'
import { useRouter } from 'vue-router'
import api from '../api/axios'

const router = useRouter()

const props = defineProps({
  formation: {
    type: Object,
    default: () => ({ id: 1, titre: 'Formation', ecole: 'École', ville: 'Ville', prix: 0 })
  }
})

const currentStep = ref(0)
const submitting = ref(false)
const apiError = ref('')

const form = reactive({
  nom: '',
  prenom: '',
  email: ''
})

const commissionRate = ref(0.07)

const steps = [
  { title: 'Vos informations', subtitle: 'Pour finaliser votre réservation' },
  { title: 'Commission & paiement', subtitle: 'Choisissez votre type de commission' },
  { title: 'Récapitulatif', subtitle: 'Vérifiez avant de confirmer' }
]

const stepBarWidth = computed(() => {
  return ((currentStep.value) / (steps.length - 1)) * 100 + '%'
})

const commissionMontant = computed(() => {
  return Math.round(props.formation.prix * commissionRate.value)
})

const formatPrice = (value) => {
  return value.toString().replace(/\B(?=(\d{3})+(?!\d))/g, ' ')
}

const validateStep0 = () => {
  if (!form.nom || !form.prenom || !form.email) {
    apiError.value = 'Veuillez remplir tous les champs obligatoires.'
    return false
  }
  return true
}

const nextStep = () => {
  apiError.value = ''
  if (currentStep.value === 0 && !validateStep0()) return
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
      formationId: props.formation.id,
      commissionRate: commissionRate.value
    }
    await api.post('/inscriptions/reserver', payload)
    router.push({ name: 'Home' })
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
.layout-reservation {
  display: flex;
  flex-direction: column;
  min-height: 100vh;
  background-color: #f5f5f5;
  font-family: 'Hanken Grotesk', sans-serif;
}

.reservation-page {
  flex-grow: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 2rem 1rem;
}

.reservation-container {
  width: 100%;
  max-width: 600px;
}

.reservation-card {
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

.commission-option {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 0.25rem;
  padding: 1.25rem 1rem;
  border: 2px solid #e0e0e0;
  border-radius: 8px;
  cursor: pointer;
  transition: border-color 0.2s, background 0.2s;
}

.commission-option:hover {
  border-color: #c69c50;
  background: rgba(198, 156, 80, 0.04);
}

.commission-option.selected {
  border-color: #c69c50;
  background: rgba(198, 156, 80, 0.08);
}

.commission-label {
  font-size: 0.85rem;
  color: #777777;
  font-weight: 500;
}

.commission-value {
  font-size: 1.5rem;
  font-weight: 800;
  color: #1a1a1a;
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

@media (max-width: 576px) {
  .reservation-card {
    padding: 1.5rem;
  }
  .commission-option {
    padding: 1rem 0.75rem;
  }
  .commission-value {
    font-size: 1.25rem;
  }
}
</style>
