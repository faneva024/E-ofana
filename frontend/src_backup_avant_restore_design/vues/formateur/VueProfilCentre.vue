<template>
  <div class="profil-centre-page">
        <h1 class="page-title">Profil du centre de formation</h1>

        <!-- Bandeau en attente de validation -->
        <div v-if="pendingValidation" class="validation-banner">
          <i class="bi bi-clock-history me-2"></i>
          <div>
            <strong>Modifications en attente de validation</strong>
            <p class="mb-0">Vos modifications ont été soumises et sont en cours de révision par un administrateur.</p>
          </div>
        </div>

        <div v-if="loading" class="text-center py-5">
          <div class="spinner-border" role="status" style="color: var(--eo-secondary);">
            <span class="visually-hidden">Chargement...</span>
          </div>
        </div>

        <div v-else class="profil-content">
          <div class="row g-4">
            <!-- Colonne gauche : Avatar + Abonnement -->
            <div class="col-lg-4">
              <!-- Carte Avatar -->
              <div class="card-section avatar-card">
                <div class="avatar-circle">
                  <img v-if="profil.logo" :src="profil.logo" alt="Logo centre" class="avatar-img" />
                  <span v-else class="avatar-initial">{{ centreInitial }}</span>
                </div>
                <h3 class="centre-name">{{ profil.nom }}</h3>
                <span class="plan-badge" :class="profil.abonnement?.plan === 'Premium' ? 'premium' : 'basic'">
                  {{ profil.abonnement?.plan || 'Basic' }}
                </span>
                <button v-if="editing" class="btn btn-outline-change-logo mt-3" @click="triggerLogoUpload">
                  <i class="bi bi-upload me-2"></i>
                  Changer le logo
                </button>
                <input ref="logoInput" type="file" accept="image/*" class="d-none" @change="handleLogoChange" />
              </div>

              <!-- Carte Abonnement -->
              <div class="card-section abonnement-card">
                <h4 class="card-section-title">Abonnement</h4>
                <div class="abonnement-row">
                  <span class="abonnement-label">Plan</span>
                  <span class="abonnement-value fw-bold">{{ profil.abonnement?.plan || 'Basic' }}</span>
                </div>
                <div class="abonnement-row">
                  <span class="abonnement-label">Renouvellement</span>
                  <span class="abonnement-value">{{ formatDate(profil.abonnement?.renouvellement) }}</span>
                </div>
                <div class="abonnement-row">
                  <span class="abonnement-label">Formations max</span>
                  <span class="abonnement-value">{{ profil.abonnement?.formationsMax || '—' }}</span>
                </div>
                <div class="abonnement-row">
                  <span class="abonnement-label">Commission</span>
                  <span class="abonnement-value">{{ profil.abonnement?.commission || '—' }}</span>
                </div>
                <hr />
                <h4 class="card-section-title">Mobile Money</h4>
                <div class="abonnement-row">
                  <span class="abonnement-label">Compte</span>
                  <span class="abonnement-value">{{ profil.mobileMoney || 'Non renseigné' }}</span>
                </div>
                <small class="text-muted d-block mt-2">
                  <i class="bi bi-lock me-1"></i>
                  Géré par le commercial
                </small>
              </div>
            </div>

            <!-- Colonne droite : Informations du centre -->
            <div class="col-lg-8">
              <div class="card-section info-card">
                <div class="d-flex justify-content-between align-items-center mb-4">
                  <h4 class="card-section-title mb-0">Informations du centre</h4>
                  <button v-if="!editing" class="btn btn-edit" @click="startEditing">
                    <i class="bi bi-pencil me-2"></i>
                    Modifier mon profil
                  </button>
                </div>

                <form @submit.prevent="submitModifications">
                  <!-- Nom du centre -->
                  <div class="mb-3">
                    <label class="form-label">Nom du centre</label>
                    <input
                      type="text"
                      class="form-control"
                      v-model="editForm.nom"
                      :disabled="!editing"
                      :class="{ 'is-invalid': errors.nom }"
                    />
                    <div class="invalid-feedback" v-if="errors.nom">{{ errors.nom }}</div>
                  </div>

                  <!-- Email de contact -->
                  <div class="mb-3">
                    <label class="form-label">Email de contact</label>
                    <input
                      type="email"
                      class="form-control"
                      v-model="editForm.email"
                      :disabled="!editing"
                      :class="{ 'is-invalid': errors.email }"
                    />
                    <div class="invalid-feedback" v-if="errors.email">{{ errors.email }}</div>
                  </div>

                  <!-- Numéro de téléphone -->
                  <div class="mb-3">
                    <label class="form-label">Numéro de téléphone</label>
                    <input
                      type="tel"
                      class="form-control"
                      v-model="editForm.telephone"
                      :disabled="!editing"
                      placeholder="+261 34 00 000 00"
                    />
                  </div>

                  <!-- Adresse -->
                  <div class="mb-3">
                    <label class="form-label">Adresse</label>
                    <input
                      type="text"
                      class="form-control"
                      v-model="editForm.adresse"
                      :disabled="!editing"
                    />
                  </div>

                  <!-- Description -->
                  <div class="mb-3">
                    <label class="form-label">Description du centre</label>
                    <textarea
                      class="form-control"
                      v-model="editForm.description"
                      :disabled="!editing"
                      rows="3"
                    ></textarea>
                  </div>

                  <!-- Services proposés -->
                  <div class="mb-3">
                    <label class="form-label">Services proposés</label>
                    <textarea
                      class="form-control"
                      v-model="editForm.services"
                      :disabled="!editing"
                      rows="2"
                      placeholder="Ex: Formations en présentiel, à distance, coaching..."
                    ></textarea>
                  </div>

                  <!-- Fourchette de prix -->
                  <div class="row mb-3">
                    <div class="col-md-6">
                      <label class="form-label">Prix minimum (Ar)</label>
                      <input
                        type="number"
                        class="form-control"
                        v-model.number="editForm.prixMin"
                        :disabled="!editing"
                      />
                    </div>
                    <div class="col-md-6">
                      <label class="form-label">Prix maximum (Ar)</label>
                      <input
                        type="number"
                        class="form-control"
                        v-model.number="editForm.prixMax"
                        :disabled="!editing"
                      />
                    </div>
                  </div>

                  <!-- Site web -->
                  <div class="mb-3">
                    <label class="form-label">Site web</label>
                    <input
                      type="url"
                      class="form-control"
                      v-model="editForm.siteWeb"
                      :disabled="!editing"
                      placeholder="https://..."
                    />
                  </div>

                  <!-- Réseaux sociaux -->
                  <div class="row mb-3">
                    <div class="col-md-6 mb-3 mb-md-0">
                      <label class="form-label">
                        <i class="bi bi-facebook me-1"></i> Facebook
                      </label>
                      <input
                        type="url"
                        class="form-control"
                        v-model="editForm.facebook"
                        :disabled="!editing"
                        placeholder="https://facebook.com/..."
                      />
                    </div>
                    <div class="col-md-6">
                      <label class="form-label">
                        <i class="bi bi-linkedin me-1"></i> LinkedIn
                      </label>
                      <input
                        type="url"
                        class="form-control"
                        v-model="editForm.linkedin"
                        :disabled="!editing"
                        placeholder="https://linkedin.com/..."
                      />
                    </div>
                  </div>

                  <!-- Avertissement + Actions -->
                  <div v-if="editing">
                    <div class="alert alert-warning d-flex align-items-start">
                      <i class="bi bi-exclamation-triangle-fill me-2 mt-1"></i>
                      <span>Toute modification doit être validée par un administrateur avant publication.</span>
                    </div>

                    <div v-if="apiError" class="alert alert-danger">{{ apiError }}</div>
                    <div v-if="apiSuccess" class="alert alert-success">{{ apiSuccess }}</div>

                    <div class="d-flex gap-3">
                      <button type="submit" class="btn btn-submit" :disabled="submitting">
                        <span v-if="submitting" class="spinner-border spinner-border-sm me-2"></span>
                        <i v-else class="bi bi-send me-2"></i>
                        Soumettre les modifications
                      </button>
                      <button type="button" class="btn btn-outline-cancel" @click="cancelEditing">
                        Annuler
                      </button>
                    </div>
                  </div>
                </form>
              </div>
            </div>
          </div>
        </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthFormateurStore } from '../../stores/authFormateurStore'
import axios from 'axios'

const router = useRouter()
const authFormateurStore = useAuthFormateurStore()

const loading = ref(false)
const editing = ref(false)
const submitting = ref(false)
const pendingValidation = ref(false)
const apiError = ref('')
const apiSuccess = ref('')
const errors = ref({})
const logoInput = ref(null)

const profil = ref({
  nom: 'TechAcademy Antananarivo',
  logo: null,
  email: 'contact@techacademy.mg',
  telephone: '+261 34 00 000 00',
  adresse: 'Lot II A 47 Bis Antananarivo 101',
  description: 'Centre de formation spécialisé en informatique et développement numérique.',
  services: 'Formations en présentiel, formations à distance, coaching personnalisé',
  prixMin: 50000,
  prixMax: 500000,
  siteWeb: 'https://techacademy.mg',
  facebook: 'https://facebook.com/techacademy.mg',
  linkedin: '',
  mobileMoney: '+261 34 00 000 00',
  abonnement: {
    plan: 'Premium',
    renouvellement: '2026-12-15',
    formationsMax: 'Illimité',
    commission: '10%'
  }
})

const editForm = ref({ ...profil.value })

const centreInitial = computed(() => {
  const nom = profil.value.nom || ''
  return nom.charAt(0).toUpperCase() || 'C'
})

function formatDate(dateStr) {
  if (!dateStr) return '—'
  return new Date(dateStr + 'T00:00:00').toLocaleDateString('fr-FR', {
    day: 'numeric',
    month: 'short',
    year: 'numeric'
  })
}

function startEditing() {
  editForm.value = {
    nom: profil.value.nom,
    logo: profil.value.logo,
    email: profil.value.email,
    telephone: profil.value.telephone,
    adresse: profil.value.adresse,
    description: profil.value.description,
    services: profil.value.services,
    prixMin: profil.value.prixMin,
    prixMax: profil.value.prixMax,
    siteWeb: profil.value.siteWeb,
    facebook: profil.value.facebook,
    linkedin: profil.value.linkedin
  }
  errors.value = {}
  apiError.value = ''
  apiSuccess.value = ''
  editing.value = true
}

function cancelEditing() {
  editForm.value = { ...profil.value }
  errors.value = {}
  apiError.value = ''
  apiSuccess.value = ''
  editing.value = false
}

function triggerLogoUpload() {
  logoInput.value?.click()
}

function handleLogoChange(event) {
  const file = event.target.files[0]
  if (file) {
    const reader = new FileReader()
    reader.onload = (e) => {
      editForm.value.logo = e.target.result
    }
    reader.readAsDataURL(file)
  }
}

function validate() {
  errors.value = {}
  if (!editForm.value.nom?.trim()) {
    errors.value.nom = 'Le nom du centre est requis'
  }
  if (!editForm.value.email?.trim()) {
    errors.value.email = "L'email est requis"
  } else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(editForm.value.email)) {
    errors.value.email = "L'email n'est pas valide"
  }
  return Object.keys(errors.value).length === 0
}

async function submitModifications() {
  if (!validate()) return

  submitting.value = true
  apiError.value = ''
  apiSuccess.value = ''

  try {
    // TODO: API call
    // await axios.put('/api/v1/formateurs/profil', editForm.value)
    await new Promise(resolve => setTimeout(resolve, 800))

    profil.value = {
      ...profil.value,
      nom: editForm.value.nom,
      logo: editForm.value.logo,
      email: editForm.value.email,
      telephone: editForm.value.telephone,
      adresse: editForm.value.adresse,
      description: editForm.value.description,
      services: editForm.value.services,
      prixMin: editForm.value.prixMin,
      prixMax: editForm.value.prixMax,
      siteWeb: editForm.value.siteWeb,
      facebook: editForm.value.facebook,
      linkedin: editForm.value.linkedin
    }

    editing.value = false
    pendingValidation.value = true
    apiSuccess.value = 'Vos modifications ont été soumises avec succès.'
  } catch (error) {
    apiError.value = error.response?.data?.message || 'Erreur lors de la soumission. Veuillez réessayer.'
  } finally {
    submitting.value = false
  }
}

function handleLogout() {
  authFormateurStore.logout()
  router.push({ name: 'Connexion' })
}

async function loadProfil() {
  loading.value = true
  try {
    // TODO: API call
    // const response = await axios.get('/api/v1/formateurs/profil')
    // profil.value = response.data
    // pendingValidation.value = response.data.pendingValidation || false
    await new Promise(resolve => setTimeout(resolve, 400))
  } catch (error) {
    console.error('Erreur lors du chargement du profil:', error)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadProfil()
})
</script>

<style scoped>
.profil-centre-page {
  font-family: var(--eo-font-family);
}

.page-title {
  font-size: var(--eo-font-size-3xl);
  font-weight: 700;
  color: var(--eo-gray-900);
  margin-bottom: var(--eo-spacing-xl);
}

/* ===== VALIDATION BANNER ===== */
.validation-banner {
  display: flex;
  align-items: flex-start;
  gap: var(--eo-spacing-md);
  background: var(--eo-warning-light);
  border: 1px solid var(--eo-warning);
  border-radius: var(--eo-radius-lg);
  padding: var(--eo-spacing-lg);
  margin-bottom: var(--eo-spacing-xl);
  color: var(--eo-gray-800);
}

.validation-banner i {
  font-size: 1.3rem;
  color: var(--eo-warning);
  flex-shrink: 0;
}

.validation-banner p {
  font-size: var(--eo-font-size-sm);
  color: var(--eo-gray-600);
  margin-top: var(--eo-spacing-xs);
}

/* ===== CARDS ===== */
.card-section {
  background: white;
  border-radius: var(--eo-radius-xl);
  box-shadow: var(--eo-shadow-sm);
  padding: var(--eo-spacing-xl);
  margin-bottom: var(--eo-spacing-lg);
}

.card-section-title {
  font-size: var(--eo-font-size-lg);
  font-weight: 700;
  color: var(--eo-gray-800);
  margin-bottom: var(--eo-spacing-lg);
}

/* ===== AVATAR CARD ===== */
.avatar-card {
  text-align: center;
}

.avatar-circle {
  width: 100px;
  height: 100px;
  border-radius: 50%;
  background: var(--eo-secondary);
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto var(--eo-spacing-md);
  overflow: hidden;
}

.avatar-initial {
  font-size: 2.5rem;
  font-weight: 700;
  color: white;
}

.avatar-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.centre-name {
  font-size: var(--eo-font-size-xl);
  font-weight: 700;
  color: var(--eo-gray-900);
  margin-bottom: var(--eo-spacing-sm);
}

.plan-badge {
  display: inline-block;
  padding: var(--eo-spacing-xs) var(--eo-spacing-md);
  border-radius: var(--eo-radius-full);
  font-size: var(--eo-font-size-sm);
  font-weight: 600;
}

.plan-badge.premium {
  background: var(--eo-secondary);
  color: white;
}

.plan-badge.basic {
  background: var(--eo-gray-200);
  color: var(--eo-gray-700);
}

.btn-outline-change-logo {
  border: 1px solid var(--eo-gray-300);
  color: var(--eo-gray-700);
  font-size: var(--eo-font-size-sm);
  border-radius: var(--eo-radius-lg);
  padding: var(--eo-spacing-sm) var(--eo-spacing-lg);
  transition: all var(--eo-transition-base);
}

.btn-outline-change-logo:hover {
  border-color: var(--eo-secondary);
  color: var(--eo-secondary);
}

/* ===== ABONNEMENT CARD ===== */
.abonnement-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: var(--eo-spacing-sm) 0;
}

.abonnement-label {
  font-size: var(--eo-font-size-sm);
  color: var(--eo-secondary);
  font-weight: 500;
}

.abonnement-value {
  font-size: var(--eo-font-size-sm);
  color: var(--eo-gray-800);
}

/* ===== INFO CARD ===== */
.form-label {
  font-size: var(--eo-font-size-sm);
  font-weight: 600;
  color: var(--eo-gray-700);
  margin-bottom: var(--eo-spacing-xs);
}

.form-control {
  border: 1px solid var(--eo-gray-200);
  border-radius: var(--eo-radius-md);
  padding: var(--eo-spacing-sm) var(--eo-spacing-md);
  font-size: var(--eo-font-size-base);
  color: var(--eo-gray-800);
  transition: border-color var(--eo-transition-base);
}

.form-control:focus {
  border-color: var(--eo-secondary);
  box-shadow: 0 0 0 0.2rem rgba(198, 156, 80, 0.2);
}

.form-control:disabled {
  background: var(--eo-gray-50);
  color: var(--eo-gray-700);
  cursor: default;
}

/* ===== BUTTONS ===== */
.btn-edit {
  background: var(--eo-secondary);
  color: white;
  font-weight: 600;
  font-size: var(--eo-font-size-sm);
  border-radius: var(--eo-radius-lg);
  padding: var(--eo-spacing-sm) var(--eo-spacing-lg);
  border: none;
  transition: all var(--eo-transition-base);
}

.btn-edit:hover {
  background: var(--eo-secondary-dark);
  color: white;
  transform: translateY(-1px);
  box-shadow: var(--eo-shadow-md);
}

.btn-submit {
  background: var(--eo-secondary);
  color: white;
  font-weight: 600;
  border: none;
  border-radius: var(--eo-radius-lg);
  padding: var(--eo-spacing-md) var(--eo-spacing-xl);
  transition: all var(--eo-transition-base);
}

.btn-submit:hover:not(:disabled) {
  background: var(--eo-secondary-dark);
  color: white;
  transform: translateY(-1px);
  box-shadow: var(--eo-shadow-md);
}

.btn-submit:disabled {
  opacity: 0.7;
  color: white;
}

.btn-outline-cancel {
  border: 1px solid var(--eo-gray-300);
  color: var(--eo-gray-600);
  font-weight: 500;
  border-radius: var(--eo-radius-lg);
  padding: var(--eo-spacing-md) var(--eo-spacing-xl);
  transition: all var(--eo-transition-base);
}

.btn-outline-cancel:hover {
  background: var(--eo-gray-100);
  border-color: var(--eo-gray-400);
  color: var(--eo-gray-800);
}

/* ===== RESPONSIVE ===== */
@media (max-width: 768px) {
  .d-flex.gap-3 {
    flex-direction: column;
  }

  .d-flex.gap-3 .btn {
    width: 100%;
  }
}
</style>
