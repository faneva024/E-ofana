<template>
  <div class="layout-inscription">
    <main class="inscription-page">
      <div class="inscription-container">
        <div class="inscription-card">

          <div class="text-center mb-4">
            <div v-if="!success" class="step-indicator">
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
            <h1 class="h4 fw-bold text-dark mb-1" v-if="!success">{{ steps[currentStep].title }}</h1>
            <p class="text-muted small mb-0" v-if="!success && currentStep < steps.length">{{ steps[currentStep].subtitle }}</p>
          </div>

          <form @submit.prevent="handleSubmit" novalidate>

            <!-- Step 0: Choix opérateur Mobile Money -->
            <div v-show="currentStep === 0">
              <label class="form-label text-center d-block mb-3">Choisissez votre opérateur Mobile Money</label>
              <div class="row g-3 mb-4">
                <div class="col-4">
                  <label
                    class="operator-option"
                    :class="{ selected: operateur === 'mvola' }"
                  >
                    <input v-model="operateur" type="radio" value="mvola" class="d-none" />
                    <span class="operator-icon">
                      <svg fill="none" height="32" stroke="#e60000" stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" width="32" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24"><path d="M12 2L2 7l10 5 10-5-10-5z"/><path d="M2 17l10 5 10-5"/><path d="M2 12l10 5 10-5"/></svg>
                    </span>
                    <span class="operator-name">Mvola</span>
                  </label>
                </div>
                <div class="col-4">
                  <label
                    class="operator-option"
                    :class="{ selected: operateur === 'orange-money' }"
                  >
                    <input v-model="operateur" type="radio" value="orange-money" class="d-none" />
                    <span class="operator-icon">
                      <svg fill="none" height="32" stroke="#ff7900" stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" width="32" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24"><circle cx="12" cy="12" r="10"/><path d="M8 12h8"/><path d="M12 8v8"/></svg>
                    </span>
                    <span class="operator-name">Orange Money</span>
                  </label>
                </div>
                <div class="col-4">
                  <label
                    class="operator-option"
                    :class="{ selected: operateur === 'airtel-money' }"
                  >
                    <input v-model="operateur" type="radio" value="airtel-money" class="d-none" />
                    <span class="operator-icon">
                      <svg fill="none" height="32" stroke="#e00000" stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" width="32" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24"><path d="M22 12h-4l-3 9L9 3l-3 9H2"/></svg>
                    </span>
                    <span class="operator-name">Airtel Money</span>
                  </label>
                </div>
              </div>
            </div>

            <!-- Step 1: Confirmation du montant -->
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

              <div v-if="formation.dateDebut || formation.dateFin" class="mb-4">
                <div class="d-flex align-items-center gap-2 text-muted small">
                  <svg fill="none" height="16" stroke="currentColor" stroke-linecap="round" stroke-linejoin="round" stroke-width="2" width="16" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24"><rect x="3" y="4" width="18" height="18" rx="2" ry="2"/><line x1="16" y1="2" x2="16" y2="6"/><line x1="8" y1="2" x2="8" y2="6"/><line x1="3" y1="10" x2="21" y2="10"/></svg>
                  <span v-if="formation.dateDebut">Du {{ formation.dateDebut }}</span>
                  <span v-if="formation.dateFin">au {{ formation.dateFin }}</span>
                </div>
              </div>

              <div class="price-summary">
                <div class="d-flex justify-content-between mb-1">
                  <span>Prix de la formation</span>
                  <span>{{ formatPrice(formation.prix) }} Ar</span>
                </div>
                <div class="d-flex justify-content-between mb-1">
                  <span>Réduction inscription en ligne (5 %)</span>
                  <span class="text-success">- {{ formatPrice(reduction) }} Ar</span>
                </div>
                <hr class="my-2" />
                <div class="d-flex justify-content-between fw-bold fs-5">
                  <span>Montant final à payer</span>
                  <span class="text-gold">{{ formatPrice(montantFinal) }} Ar</span>
                </div>
                <p class="text-muted small mt-2 mb-0">
                  <em>Paiement via {{ operateurLabel }}.</em>
                </p>
              </div>
            </div>

            <!-- Step 2: Attente du paiement -->
            <div v-show="currentStep === 2">
              <div class="text-center mb-4">
                <div class="payment-waiting-icon mb-3">
                  <svg fill="none" height="48" stroke="#c69c50" stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" width="48" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24"><path d="M22 12h-4l-3 9L9 3l-3 9H2"/></svg>
                </div>
                <p class="fw-bold mb-1">Demande de paiement envoyée à votre téléphone</p>
                <p class="text-muted small mb-0">Veuillez vérifier votre téléphone {{ operateurLabel }} et confirmer le paiement.</p>
              </div>

              <div class="countdown-container text-center mb-4">
                <div class="countdown-circle">
                  <svg width="120" height="120" viewBox="0 0 120 120">
                    <circle cx="60" cy="60" r="54" fill="none" stroke="#e0e0e0" stroke-width="6" />
                    <circle
                      cx="60" cy="60" r="54"
                      fill="none"
                      stroke="#c69c50"
                      stroke-width="6"
                      stroke-linecap="round"
                      :stroke-dasharray="339.292"
                      :stroke-dashoffset="countdownOffset"
                      transform="rotate(-90, 60, 60)"
                      style="transition: stroke-dashoffset 1s linear;"
                    />
                  </svg>
                  <div class="countdown-text">
                    <span class="countdown-time">{{ formattedTime }}</span>
                    <span class="countdown-label">minutes restantes</span>
                  </div>
                </div>
              </div>
            </div>

            <div v-if="apiError" class="alert alert-danger d-flex align-items-center gap-2 mt-3 mb-0" role="alert">
              <svg fill="currentColor" height="18" width="18" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 16 16"><path d="M8.982 1.566a1.13 1.13 0 0 0-1.96 0L.165 13.233c-.457.778.091 1.767.98 1.767h13.713c.889 0 1.438-.99.98-1.767L8.982 1.566zM8 5c.535 0 .954.462.9.995l-.35 3.507a.552.552 0 0 1-1.1 0L7.1 5.995A.905.905 0 0 1 8 5zm.002 6a1 1 0 1 1 0 2 1 1 0 0 1 0-2z"/></svg>
              <span>{{ apiError }}</span>
            </div>

          </form>

          <!-- Success state -->
          <div v-if="success" class="text-center py-5">
            <div class="success-icon mb-3">
              <svg fill="none" height="64" stroke="#28a745" stroke-linecap="round" stroke-linejoin="round" stroke-width="2" width="64" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24"><path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"/><polyline points="22 4 12 14.01 9 11.01"/></svg>
            </div>
            <h3 class="fw-bold text-dark mb-2">Inscription réussie !</h3>
            <p class="text-muted mb-4">Votre inscription à la formation a été confirmée.</p>
            <button type="button" class="btn btn-eofana-dark px-5" @click="downloadRecu">
              <svg fill="none" height="18" stroke="currentColor" stroke-linecap="round" stroke-linejoin="round" stroke-width="2" width="18" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" class="me-2"><path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"/><polyline points="7 10 12 15 17 10"/><line x1="12" y1="15" x2="12" y2="3"/></svg>
              Télécharger le reçu PDF
            </button>
            <p class="text-muted small mt-3">Redirection vers Mon espace dans {{ redirectCountdown }}s...</p>
          </div>

          <div v-if="!success" class="d-flex gap-3 mt-4">
            <button
              v-if="currentStep === 0"
              type="button"
              class="btn btn-outline-dark flex-fill d-flex align-items-center justify-content-center gap-2"
              @click="close"
            >
              <span>Annuler</span>
            </button>
            <button
              v-if="currentStep > 0 && currentStep < 2"
              type="button"
              class="btn btn-outline-dark flex-fill d-flex align-items-center justify-content-center gap-2"
              @click="prevStep"
            >
              <svg fill="none" height="16" stroke="currentColor" stroke-linecap="round" stroke-linejoin="round" stroke-width="2" width="16" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24"><polyline points="15 18 9 12 15 6"></polyline></svg>
              <span>Précédent</span>
            </button>
            <button
              v-if="currentStep === 0"
              type="button"
              class="btn btn-eofana-dark flex-fill d-flex align-items-center justify-content-center gap-2"
              @click="nextStep"
            >
              <span>Continuer</span>
              <svg fill="none" height="16" stroke="currentColor" stroke-linecap="round" stroke-linejoin="round" stroke-width="2" width="16" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24"><polyline points="9 18 15 12 9 6"></polyline></svg>
            </button>
            <button
              v-if="currentStep === 1"
              type="button"
              class="btn btn-eofana-dark flex-fill d-flex align-items-center justify-content-center gap-2"
              :disabled="submitting"
              @click="handleSubmit"
            >
              <span v-if="submitting">Traitement en cours...</span>
              <span v-else>Confirmer et payer</span>
              <svg v-if="!submitting" fill="none" height="16" stroke="currentColor" stroke-linecap="round" stroke-linejoin="round" stroke-width="2" width="16" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24"><polyline points="9 18 15 12 9 6"></polyline></svg>
            </button>
            <div v-if="currentStep === 2" class="d-flex gap-3 w-100">
              <button
                type="button"
                class="btn btn-outline-dark flex-fill d-flex align-items-center justify-content-center gap-2"
                :disabled="submitting"
                @click="cancelPayment"
              >
                <span>Annuler</span>
              </button>
              <button
                type="button"
                class="btn btn-eofana-dark flex-fill d-flex align-items-center justify-content-center gap-2"
                :disabled="submitting"
                @click="confirmPayment"
              >
                <svg fill="none" height="16" stroke="currentColor" stroke-linecap="round" stroke-linejoin="round" stroke-width="2" width="16" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24"><polyline points="9 18 15 12 9 6"/></svg>
                <span v-if="submitting">Vérification...</span>
                <span v-else>J'ai confirmé le paiement</span>
              </button>
            </div>
          </div>

        </div>
      </div>
    </main>
  </div>
</template>

<script setup>
import { ref, computed, onBeforeUnmount, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import api from '../api/axios'

const router = useRouter()
const route = useRoute()

const props = defineProps({
  formation: {
    type: Object,
    default: () => ({
      id: 1,
      idFormation: 1,
      titre: 'Formation',
      ecole: 'École',
      centre: 'École',
      ville: 'Ville',
      prix: 0,
      prixRemise: 0,
      dateDebut: '',
      dateFin: ''
    })
  }
})

const currentStep = ref(0)
const submitting = ref(false)
const apiError = ref('')
const operateur = ref('')
const success = ref(false)
const redirectCountdown = ref(3)

const idInscription = ref(null)

const formation = ref({
  id: props.formation.id || props.formation.idFormation || 1,
  idFormation: props.formation.idFormation || props.formation.id || 1,
  titre: props.formation.titre || 'Formation',
  ecole: props.formation.ecole || props.formation.centre || 'École',
  centre: props.formation.centre || props.formation.ecole || 'École',
  ville: props.formation.ville || props.formation.lieu || 'Ville',
  prix: Number(props.formation.prixRemise || props.formation.prix || 0),
  dateDebut: props.formation.dateDebut || '',
  dateFin: props.formation.dateFin || ''
})

const steps = [
  {
    title: "Choix de l'opérateur Mobile Money",
    subtitle: "Sélectionnez votre opérateur pour effectuer le paiement"
  },
  {
    title: 'Confirmation du montant',
    subtitle: 'Vérifiez le montant final avant de payer'
  },
  {
    title: 'Attente du paiement',
    subtitle: 'Confirmez le paiement depuis votre téléphone'
  }
]

const operateurLabel = computed(() => {
  const labels = {
    mvola: 'Mvola',
    'orange-money': 'Orange Money',
    'airtel-money': 'Airtel Money'
  }

  return labels[operateur.value] || operateur.value
})

const operateurBackend = computed(() => {
  if (operateur.value === 'orange-money') return 'orange'
  if (operateur.value === 'airtel-money') return 'airtel'
  return 'mvola'
})

const reduction = computed(() => {
  return Math.round(Number(formation.value.prix || 0) * 0.05)
})

const montantFinal = computed(() => {
  const montant = Number(formation.value.prix || 0) - reduction.value
  return montant > 0 ? montant : Number(formation.value.prix || 0)
})

const stepBarWidth = computed(() => {
  return (currentStep.value / (steps.length - 1)) * 100 + '%'
})

const formatPrice = (value) => {
  return Number(value || 0).toLocaleString('fr-FR')
}

const getUtilisateurConnecte = () => {
  const auth = localStorage.getItem('auth')
  const utilisateur = localStorage.getItem('utilisateur')

  if (auth) {
    try {
      const parsed = JSON.parse(auth)
      if (parsed?.user) return parsed.user
    } catch (e) {
      console.error('Erreur lecture auth', e)
    }
  }

  if (utilisateur) {
    try {
      return JSON.parse(utilisateur)
    } catch (e) {
      console.error('Erreur lecture utilisateur', e)
    }
  }

  return null
}

const getIdUser = () => {
  const utilisateur = getUtilisateurConnecte()

  if (utilisateur?.idUser) return Number(utilisateur.idUser)
  if (utilisateur?.id) return Number(utilisateur.id)

  const token = localStorage.getItem('token') || localStorage.getItem('authToken')

  if (token && token.startsWith('TOKEN-DEMO-')) {
    const id = Number(token.replace('TOKEN-DEMO-', ''))
    if (!Number.isNaN(id)) return id
  }

  // Test local avec apprenant@test.com
  return 1
}

const getIdFormation = () => {
  if (route.query.formationId) return Number(route.query.formationId)
  if (route.query.idFormation) return Number(route.query.idFormation)

  const saved =
    localStorage.getItem('formationAInscrire') ||
    localStorage.getItem('selectedFormationId')

  if (saved) return Number(saved)

  return Number(formation.value.idFormation || formation.value.id || 1)
}

const chargerFormation = async () => {
  try {
    const idFormation = getIdFormation()

    const response = await api.get(`/formations/${idFormation}`)
    const data = response.data

    formation.value = {
      id: data.idFormation || data.id || idFormation,
      idFormation: data.idFormation || data.id || idFormation,
      titre: data.titre || 'Formation',
      ecole: data.centre || data.ecole || 'École',
      centre: data.centre || data.ecole || 'École',
      ville: data.ville || data.lieu || 'Ville',
      prix: Number(data.prixRemise || data.prix || 0),
      dateDebut: data.dateDebut || '',
      dateFin: data.dateFin || ''
    }
  } catch (err) {
    console.error('Erreur chargement formation', err)
  }
}

// Countdown 5 minutes
const COUNTDOWN_SECONDS = 300
const countdownRemaining = ref(COUNTDOWN_SECONDS)
let countdownInterval = null

const countdownOffset = computed(() => {
  const circumference = 339.292
  return circumference * (1 - countdownRemaining.value / COUNTDOWN_SECONDS)
})

const formattedTime = computed(() => {
  const min = Math.floor(countdownRemaining.value / 60)
  const sec = countdownRemaining.value % 60

  return `${min}:${sec.toString().padStart(2, '0')}`
})

function startCountdown() {
  stopCountdown()
  countdownRemaining.value = COUNTDOWN_SECONDS

  countdownInterval = setInterval(() => {
    countdownRemaining.value--

    if (countdownRemaining.value <= 0) {
      stopCountdown()
      apiError.value = 'Le délai de paiement a expiré. Veuillez réessayer.'
    }
  }, 1000)
}

function stopCountdown() {
  if (countdownInterval) {
    clearInterval(countdownInterval)
    countdownInterval = null
  }
}

let redirectInterval = null

function startRedirectCountdown() {
  redirectCountdown.value = 3

  redirectInterval = setInterval(() => {
    redirectCountdown.value--

    if (redirectCountdown.value <= 0) {
      clearInterval(redirectInterval)
      redirectInterval = null
      router.push({ name: 'MonEspace' })
    }
  }, 1000)
}

onMounted(() => {
  chargerFormation()
})

onBeforeUnmount(() => {
  stopCountdown()

  if (redirectInterval) {
    clearInterval(redirectInterval)
  }
})

const nextStep = () => {
  apiError.value = ''

  if (!operateur.value) {
    apiError.value = 'Veuillez sélectionner un opérateur Mobile Money.'
    return
  }

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
    const idUser = getIdUser()
    const idFormation = Number(formation.value.idFormation || formation.value.id)

    if (!idUser) {
      apiError.value = "L'utilisateur est obligatoire."
      return
    }

    if (!idFormation) {
      apiError.value = 'La formation est obligatoire.'
      return
    }

    const inscriptionResponse = await api.post('/inscriptions/inscrire', {
      idUser,
      idFormation
    })

    idInscription.value =
      inscriptionResponse.data?.idInscription ||
      inscriptionResponse.data?.inscription?.idInscription

    if (!idInscription.value) {
      throw new Error("L'inscription a été créée mais l'idInscription est introuvable.")
    }

    currentStep.value++
    startCountdown()
  } catch (err) {
    console.error('Erreur inscription', err)
    apiError.value =
      err.response?.data?.message ||
      err.message ||
      "Erreur lors de l'inscription."
  } finally {
    submitting.value = false
  }
}

const confirmPayment = async () => {
  apiError.value = ''
  submitting.value = true

  try {
    if (!idInscription.value) {
      apiError.value = 'Aucune inscription trouvée pour le paiement.'
      return
    }

    await api.post('/paiements/payer', {
      idInscription: idInscription.value,
      montant: montantFinal.value,
      operateur: operateurBackend.value,
      numeroTransaction: `TXN-${Date.now()}`
    })

    stopCountdown()
    success.value = true
    startRedirectCountdown()
  } catch (err) {
    console.error('Erreur paiement', err)
    apiError.value =
      err.response?.data?.message ||
      err.message ||
      'Erreur lors du paiement.'
  } finally {
    submitting.value = false
  }
}

const cancelPayment = async () => {
  stopCountdown()
  close()
}

const downloadRecu = async () => {
  try {
    if (!idInscription.value) {
      apiError.value = 'Aucune inscription trouvée pour le reçu.'
      return
    }

    const response = await api.get(`/inscriptions/${idInscription.value}/recu`, {
      responseType: 'blob'
    })

    const url = URL.createObjectURL(new Blob([response.data]))
    const link = document.createElement('a')

    link.href = url
    link.setAttribute('download', `recu-inscription-${idInscription.value}.pdf`)

    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)

    URL.revokeObjectURL(url)
  } catch (err) {
    console.error('Erreur reçu PDF', err)
    apiError.value = 'Erreur lors du téléchargement du reçu.'
  }
}

const close = () => {
  stopCountdown()
  router.push({ name: 'Home' })
}
</script>

<style scoped>
.layout-inscription {
  display: flex;
  flex-direction: column;
  min-height: 100vh;
  background-color: #f5f5f5;
  font-family: 'Hanken Grotesk', sans-serif;
}

.inscription-page {
  flex-grow: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 2rem 1rem;
}

.inscription-container {
  width: 100%;
  max-width: 600px;
}

.inscription-card {
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

.operator-option {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 0.75rem;
  padding: 1.5rem 1rem;
  border: 2px solid #e0e0e0;
  border-radius: 12px;
  cursor: pointer;
  transition: border-color 0.2s, background 0.2s, box-shadow 0.2s;
}

.operator-option:hover {
  border-color: #c69c50;
  background: rgba(198, 156, 80, 0.04);
}

.operator-option.selected {
  border-color: #c69c50;
  background: rgba(198, 156, 80, 0.08);
  box-shadow: 0 0 0 3px rgba(198, 156, 80, 0.15);
}

.operator-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 56px;
  height: 56px;
  border-radius: 50%;
  background: #f5f5f5;
}

.operator-name {
  font-size: 0.85rem;
  font-weight: 700;
  color: #1a1a1a;
  text-align: center;
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

.text-success {
  color: #28a745;
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

.alert-danger {
  font-size: 0.85rem;
  border-radius: 8px;
}

.payment-waiting-icon {
  display: flex;
  align-items: center;
  justify-content: center;
}

.countdown-container {
  display: flex;
  justify-content: center;
}

.countdown-circle {
  position: relative;
  width: 120px;
  height: 120px;
}

.countdown-text {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  display: flex;
  flex-direction: column;
  align-items: center;
}

.countdown-time {
  font-size: 1.5rem;
  font-weight: 800;
  color: #1a1a1a;
}

.countdown-label {
  font-size: 0.65rem;
  color: #999999;
  text-transform: uppercase;
  letter-spacing: 0.05em;
}

.success-icon {
  display: flex;
  align-items: center;
  justify-content: center;
}

@media (max-width: 576px) {
  .modal-header {
    padding: 1.5rem 1.25rem 0 1.25rem;
  }
  .modal-body {
    padding: 1rem 1.25rem;
  }
  .modal-footer {
    padding: 0 1.25rem 1.5rem 1.25rem;
  }
  .operator-option {
    padding: 1rem 0.5rem;
  }
  .operator-icon {
    width: 44px;
    height: 44px;
  }
}
</style>
