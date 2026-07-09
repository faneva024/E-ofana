<template>
  <div class="mon-espace-container">
    <div class="espace-header">
      <div class="container">
        <div class="user-welcome">
          <div class="avatar-section">
            <div class="avatar">{{ initials }}</div>
          </div>
          <div class="welcome-text">
            <h1 class="welcome-title">Bienvenue, {{ displayName }} !</h1>
            <p class="welcome-subtitle">Gérez votre espace apprenant</p>
            <div class="welcome-actions">
              <router-link to="/" class="btn btn-light btn-sm">
                <i class="bi bi-house-door me-1"></i>
                Accueil
              </router-link>
              <router-link to="/recherche" class="btn btn-outline-light btn-sm">
                <i class="bi bi-search me-1"></i>
                Recherche
              </router-link>
            </div>
          </div>
        </div>
      </div>
    </div>

    <div class="espace-content">
      <div class="container">
        <ul class="nav nav-tabs" id="espaceTabs" role="tablist">
          <li class="nav-item" role="presentation">
            <button
              class="nav-link"
              :class="{ active: activeTab === 'inscriptions' }"
              @click="activeTab = 'inscriptions'"
              type="button"
            >
              <i class="bi bi-book-fill me-2"></i>
              Mes inscriptions
            </button>
          </li>
          <li class="nav-item" role="presentation">
            <button
              class="nav-link"
              :class="{ active: activeTab === 'recus' }"
              @click="activeTab = 'recus'"
              type="button"
            >
              <i class="bi bi-receipt me-2"></i>
              Mes reçus
            </button>
          </li>
          <li class="nav-item" role="presentation">
            <button
              class="nav-link"
              :class="{ active: activeTab === 'profil' }"
              @click="activeTab = 'profil'"
              type="button"
            >
              <i class="bi bi-person-gear me-2"></i>
              Mon profil
            </button>
          </li>
        </ul>

        <div class="tab-content">
          <!-- Onglet Mes inscriptions -->
          <div v-show="activeTab === 'inscriptions'" class="tab-pane fade show">
            <div class="content-section">
              <div v-if="loadingInscriptions" class="text-center py-5">
                <div class="spinner-border text-primary" role="status">
                  <span class="visually-hidden">Chargement...</span>
                </div>
              </div>

              <div v-else-if="inscriptions.length === 0" class="no-results">
                <div class="no-results-icon">
                  <i class="bi bi-book"></i>
                </div>
                <h3>Aucune inscription</h3>
                <p>Vous n'êtes inscrit à aucune formation pour le moment</p>
                <router-link to="/recherche" class="btn btn-primary">
                  <i class="bi bi-search me-2"></i>
                  Rechercher une formation
                </router-link>
              </div>

              <div v-else>
                <div class="table-responsive">
                  <table class="table table-hover">
                    <thead>
                      <tr>
                        <th>Formation</th>
                        <th>Date</th>
                        <th>Statut</th>
                        <th>Actions</th>
                      </tr>
                    </thead>
                    <tbody>
                      <tr v-for="ins in inscriptions" :key="ins.id">
                        <td>
                          <div class="formation-name">{{ ins.formation }}</div>
                          <small class="text-muted">{{ ins.ecole }}</small>
                        </td>
                        <td>{{ formatDate(ins.date) }}</td>
                        <td>
                          <span class="badge" :class="getStatusClass(ins.statut)">
                            {{ getStatusLabel(ins.statut) }}
                          </span>
                        </td>
                        <td>
                          <div class="btn-group">
                            <button
                              class="btn btn-sm btn-outline-primary"
                              @click="viewDetails(ins)"
                              title="Voir détails"
                            >
                              <i class="bi bi-eye"></i>
                            </button>
                            <button
                              v-if="ins.paiement_confirme"
                              class="btn btn-sm btn-outline-success"
                              @click="downloadRecu(ins)"
                              title="Télécharger reçu"
                            >
                              <i class="bi bi-download"></i>
                            </button>
                            <button
                              v-if="ins.statut !== 'termine'"
                              class="btn btn-sm btn-outline-danger"
                              @click="confirmCancel(ins)"
                              title="Annuler"
                            >
                              <i class="bi bi-x-lg"></i>
                            </button>
                          </div>
                        </td>
                      </tr>
                    </tbody>
                  </table>
                </div>
              </div>
            </div>
          </div>

          <!-- Onglet Mes reçus -->
          <div v-show="activeTab === 'recus'" class="tab-pane fade show">
            <div class="content-section">
              <div class="filters-bar mb-4">
                <div class="row g-3">
                  <div class="col-md-4">
                    <select class="form-select" v-model="recuFilters.formation">
                  <option value="">Toutes les formations</option>
                  <option v-for="formation in uniqueFormations" :key="formation" :value="formation">
                    {{ formation }}
                  </option>
                </select>
              </div>
              <div class="col-md-4">
                <input type="date" class="form-control" v-model="recuFilters.date" />
              </div>
              <div class="col-md-4">
                <button class="btn btn-outline-secondary w-100" @click="resetRecuFilters">
                  <i class="bi bi-arrow-counterclockwise me-2"></i>
                  Réinitialiser
                </button>
              </div>
                </div>
              </div>

              <div v-if="loadingRecus" class="text-center py-5">
                <div class="spinner-border text-primary" role="status">
                  <span class="visually-hidden">Chargement...</span>
                </div>
              </div>

              <div v-else-if="filteredRecus.length === 0" class="no-results">
                <div class="no-results-icon">
                  <i class="bi bi-receipt"></i>
                </div>
                <h3>Aucun reçu trouvé</h3>
                <p>Aucun reçu ne correspond à vos critères</p>
              </div>

              <div v-else>
                <div class="table-responsive">
                  <table class="table table-hover">
                    <thead>
                      <tr>
                        <th>Référence</th>
                        <th>Formation</th>
                        <th>Date</th>
                        <th>Montant</th>
                        <th>Méthode</th>
                        <th>Statut</th>
                        <th>Action</th>
                      </tr>
                    </thead>
                    <tbody>
                      <tr v-for="recu in filteredRecus" :key="recu.id">
                        <td><code>{{ recu.reference }}</code></td>
                        <td>{{ recu.formation }}</td>
                        <td>{{ formatDate(recu.date) }}</td>
                        <td class="fw-bold">{{ formatPrice(recu.montant) }} Ar</td>
                        <td>{{ recu.methode }}</td>
                        <td>
                          <span class="badge" :class="recu.statut === 'paye' ? 'bg-success' : 'bg-warning'">
                            {{ recu.statut === 'paye' ? 'Payé' : 'En attente' }}
                          </span>
                        </td>
                        <td>
                          <button
                            class="btn btn-sm btn-primary"
                            :disabled="recu.statut !== 'paye'"
                            @click="downloadRecu(recu)"
                          >
                            <i class="bi bi-download me-1"></i>
                            Télécharger
                          </button>
                        </td>
                      </tr>
                    </tbody>
                  </table>
                </div>
              </div>
            </div>
          </div>

          <!-- Onglet Mon profil -->
          <div v-show="activeTab === 'profil'" class="tab-pane fade show">
            <div class="content-section">
              <div v-if="loadingProfile" class="text-center py-5">
                <div class="spinner-border text-primary" role="status">
                  <span class="visually-hidden">Chargement...</span>
                </div>
              </div>

              <div v-else>
                <div v-if="!editing" class="profile-display">
                  <div class="row">
                    <div class="col-md-6 mb-3">
                      <label class="form-label">Prénom</label>
                      <div class="form-control-plaintext">{{ authStore.user?.prenom || '—' }}</div>
                    </div>
                    <div class="col-md-6 mb-3">
                      <label class="form-label">Nom</label>
                      <div class="form-control-plaintext">{{ authStore.user?.nom || '—' }}</div>
                    </div>
                  </div>
                  <div class="row">
                    <div class="col-md-6 mb-3">
                      <label class="form-label">Email</label>
                      <div class="form-control-plaintext">{{ authStore.user?.email || '—' }}</div>
                    </div>
                    <div class="col-md-6 mb-3">
                      <label class="form-label">Téléphone</label>
                      <div class="form-control-plaintext">{{ authStore.user?.telephone || 'Non renseigné' }}</div>
                    </div>
                  </div>
                  <button class="btn btn-primary" @click="toggleEdit">
                    <i class="bi bi-pencil me-2"></i>
                    Modifier
                  </button>
                </div>

                <div v-else class="profile-edit">
                  <form @submit.prevent="saveProfile">
                    <div class="row">
                      <div class="col-md-6 mb-3">
                        <label class="form-label">Prénom *</label>
                        <input
                          type="text"
                          class="form-control"
                          v-model="editForm.prenom"
                          :class="{ 'is-invalid': errors.prenom }"
                        />
                        <div class="invalid-feedback" v-if="errors.prenom">{{ errors.prenom }}</div>
                      </div>
                      <div class="col-md-6 mb-3">
                        <label class="form-label">Nom *</label>
                        <input
                          type="text"
                          class="form-control"
                          v-model="editForm.nom"
                          :class="{ 'is-invalid': errors.nom }"
                        />
                        <div class="invalid-feedback" v-if="errors.nom">{{ errors.nom }}</div>
                      </div>
                    </div>
                    <div class="row">
                      <div class="col-md-6 mb-3">
                        <label class="form-label">Email</label>
                        <input
                          type="email"
                          class="form-control"
                          :value="authStore.user?.email"
                          disabled
                        />
                        <small class="form-text text-muted">L'email ne peut pas être modifié</small>
                      </div>
                      <div class="col-md-6 mb-3">
                        <label class="form-label">Téléphone</label>
                        <input
                          type="tel"
                          class="form-control"
                          v-model="editForm.telephone"
                          placeholder="+261 34 00 000 00"
                        />
                      </div>
                    </div>
                    <div class="row">
                      <div class="col-md-6 mb-3">
                        <label class="form-label">Nouveau mot de passe</label>
                        <input
                          type="password"
                          class="form-control"
                          v-model="editForm.password"
                          placeholder="Laisser vide pour conserver"
                          :class="{ 'is-invalid': errors.password }"
                        />
                        <div class="invalid-feedback" v-if="errors.password">{{ errors.password }}</div>
                      </div>
                      <div class="col-md-6 mb-3">
                        <label class="form-label">Confirmer le mot de passe</label>
                        <input
                          type="password"
                          class="form-control"
                          v-model="editForm.confirmPassword"
                          placeholder="Confirmer"
                          :class="{ 'is-invalid': errors.confirmPassword }"
                        />
                        <div class="invalid-feedback" v-if="errors.confirmPassword">{{ errors.confirmPassword }}</div>
                      </div>
                    </div>

                    <div v-if="apiError" class="alert alert-danger">{{ apiError }}</div>

                    <div class="form-actions">
                      <button type="submit" class="btn btn-primary" :disabled="submitting">
                        <span v-if="submitting" class="spinner-border spinner-border-sm me-2"></span>
                        <i v-else class="bi bi-check-lg me-2"></i>
                        Enregistrer
                      </button>
                      <button type="button" class="btn btn-outline-secondary" @click="toggleEdit">
                        Annuler
                      </button>
                    </div>
                  </form>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useAuthStore } from '../../stores/authStore'
import axios from 'axios'

const authStore = useAuthStore()

const activeTab = ref('inscriptions')
const editing = ref(false)
const submitting = ref(false)
const apiError = ref('')

const loadingInscriptions = ref(false)
const loadingRecus = ref(false)
const loadingProfile = ref(false)

const errors = ref({})

const editForm = ref({
  prenom: authStore.user?.prenom || '',
  nom: authStore.user?.nom || '',
  telephone: authStore.user?.telephone || '',
  password: '',
  confirmPassword: ''
})

const recuFilters = ref({
  formation: '',
  date: ''
})

const inscriptions = ref([
  {
    id: 1,
    formation: 'Développement Web Full Stack',
    ecole: 'TechAcademy Antananarivo',
    categorie: 'Informatique',
    date: '2025-06-15',
    statut: 'inscrit',
    paiement_confirme: true,
    progression: 65,
    prix: 150000,
    duree: '3 mois'
  },
  {
    id: 2,
    formation: 'Marketing Digital & Réseaux Sociaux',
    ecole: 'Business School Tana',
    categorie: 'Business & Management',
    date: '2025-07-01',
    statut: 'reserve',
    paiement_confirme: false,
    progression: 12,
    prix: 80000,
    duree: '6 semaines'
  },
  {
    id: 3,
    formation: "Gestion d'Exploitation Agricole",
    ecole: 'Institut Rural Madagascar',
    categorie: 'Agriculture',
    date: '2025-03-10',
    statut: 'termine',
    paiement_confirme: true,
    progression: 100,
    prix: 120000,
    duree: '4 mois'
  }
])

const recus = ref([
  {
    id: 1,
    formation: 'Développement Web Full Stack',
    date: '2025-06-15',
    montant: 150000,
    reference: 'REC-2025-001',
    methode: 'Mobile Money',
    statut: 'paye'
  },
  {
    id: 2,
    formation: "Gestion d'Exploitation Agricole",
    date: '2025-03-10',
    montant: 120000,
    reference: 'REC-2025-002',
    methode: 'Virement bancaire',
    statut: 'paye'
  },
  {
    id: 3,
    formation: 'Marketing Digital & Réseaux Sociaux',
    date: '2025-07-01',
    montant: 80000,
    reference: 'REC-2025-003',
    methode: 'Mobile Money',
    statut: 'en_attente'
  }
])

const initials = computed(() => {
  const p = authStore.user?.prenom || ''
  const n = authStore.user?.nom || ''
  if (p && n) return (p[0] + n[0]).toUpperCase()
  if (authStore.user?.email) return authStore.user.email[0].toUpperCase()
  return 'U'
})

const displayName = computed(() => {
  const p = authStore.user?.prenom || ''
  const n = authStore.user?.nom || ''
  if (p && n) return p + ' ' + n
  return authStore.user?.email?.split('@')[0] || 'Utilisateur'
})

const uniqueFormations = computed(() => {
  return [...new Set(recus.value.map(r => r.formation))]
})

const filteredRecus = computed(() => {
  return recus.value.filter(recu => {
    if (recuFilters.value.formation && recu.formation !== recuFilters.value.formation) {
      return false
    }
    if (recuFilters.value.date && recu.date !== recuFilters.value.date) {
      return false
    }
    return true
  })
})

const getStatusClass = (statut) => {
  switch (statut) {
    case 'inscrit': return 'bg-primary'
    case 'reserve': return 'bg-warning'
    case 'termine': return 'bg-success'
    default: return 'bg-secondary'
  }
}

const getStatusLabel = (statut) => {
  switch (statut) {
    case 'inscrit': return 'Inscrit'
    case 'reserve': return 'Réservé'
    case 'termine': return 'Terminé'
    default: return statut
  }
}

function formatPrice(value) {
  return new Intl.NumberFormat('fr-FR').format(value)
}

function formatDate(date) {
  return new Date(date + 'T00:00:00').toLocaleDateString('fr-FR', {
    day: 'numeric', month: 'long', year: 'numeric'
  })
}

function viewDetails(inscription) {
  console.log('Voir détails:', inscription.id)
  // TODO: Navigate to formation details page
}

function downloadRecu(item) {
  console.log('Téléchargement du reçu:', item.reference || item.id)
  // TODO: Implement PDF download
}

function confirmCancel(inscription) {
  if (confirm(`Êtes-vous sûr de vouloir annuler l'inscription à "${inscription.formation}" ?`)) {
    cancelInscription(inscription.id)
  }
}

async function cancelInscription(id) {
  try {
    // TODO: API call to cancel inscription
    // await axios.delete(`/api/apprenants/inscriptions/${id}`)
    console.log('Inscription annulée:', id)
    inscriptions.value = inscriptions.value.filter(i => i.id !== id)
  } catch (error) {
    console.error('Erreur lors de l\'annulation:', error)
  }
}

function resetRecuFilters() {
  recuFilters.value = {
    formation: '',
    date: ''
  }
}

function toggleEdit() {
  if (editing.value) {
    editForm.value = {
      prenom: authStore.user?.prenom || '',
      nom: authStore.user?.nom || '',
      telephone: authStore.user?.telephone || '',
      password: '',
      confirmPassword: ''
    }
    apiError.value = ''
    errors.value = {}
  }
  editing.value = !editing.value
}

async function saveProfile() {
  apiError.value = ''
  errors.value = {}

  if (!editForm.value.prenom.trim()) {
    errors.value.prenom = 'Le prénom est requis'
    return
  }

  if (!editForm.value.nom.trim()) {
    errors.value.nom = 'Le nom est requis'
    return
  }

  if (editForm.value.password || editForm.value.confirmPassword) {
    if (editForm.value.password.length < 6) {
      errors.value.password = 'Le mot de passe doit contenir au moins 6 caractères'
      return
    }
    if (editForm.value.password !== editForm.value.confirmPassword) {
      errors.value.confirmPassword = 'Les mots de passe ne correspondent pas'
      return
    }
  }

  submitting.value = true
  
  try {
    // TODO: API call to update profile
    // await axios.put('/api/apprenants/profil', {
    //   prenom: editForm.value.prenom,
    //   nom: editForm.value.nom,
    //   telephone: editForm.value.telephone,
    //   password: editForm.value.password || undefined
    // })
    
    // Simulate API call
    await new Promise(resolve => setTimeout(resolve, 500))
    
    if (authStore.user) {
      authStore.user.prenom = editForm.value.prenom
      authStore.user.nom = editForm.value.nom
      authStore.user.telephone = editForm.value.telephone
    }
    
    editing.value = false
    editForm.value.password = ''
    editForm.value.confirmPassword = ''
  } catch (error) {
    apiError.value = error.response?.data?.message || 'Erreur lors de la mise à jour du profil'
  } finally {
    submitting.value = false
  }
}

async function loadInscriptions() {
  loadingInscriptions.value = true
  try {
    // TODO: API call
    // const response = await axios.get('/api/apprenants/inscriptions')
    // inscriptions.value = response.data
    await new Promise(resolve => setTimeout(resolve, 300))
  } catch (error) {
    console.error('Erreur lors du chargement des inscriptions:', error)
  } finally {
    loadingInscriptions.value = false
  }
}

async function loadRecus() {
  loadingRecus.value = true
  try {
    // TODO: API call
    // const response = await axios.get('/api/apprenants/recus')
    // recus.value = response.data
    await new Promise(resolve => setTimeout(resolve, 300))
  } catch (error) {
    console.error('Erreur lors du chargement des reçus:', error)
  } finally {
    loadingRecus.value = false
  }
}

async function loadProfile() {
  loadingProfile.value = true
  try {
    // TODO: API call
    // const response = await axios.get('/api/apprenants/profil')
    // authStore.user = response.data
    await new Promise(resolve => setTimeout(resolve, 300))
  } catch (error) {
    console.error('Erreur lors du chargement du profil:', error)
  } finally {
    loadingProfile.value = false
  }
}

onMounted(() => {
  loadInscriptions()
  loadRecus()
  loadProfile()
})
</script>

<style scoped>
.mon-espace-container {
  min-height: 100vh;
  background: var(--eo-gray-50);
}

.espace-header {
  background: linear-gradient(135deg, var(--eo-primary) 0%, var(--eo-primary-dark) 100%);
  padding: var(--eo-spacing-2xl) 0;
  color: white;
}

.user-welcome {
  display: flex;
  align-items: center;
  gap: var(--eo-spacing-lg);
}

.avatar-section {
  flex-shrink: 0;
}

.avatar {
  width: 80px;
  height: 80px;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 2.5rem;
  font-weight: 700;
  border: 3px solid rgba(255, 255, 255, 0.3);
}

.welcome-text {
  flex: 1;
}

.welcome-actions {
  display: flex;
  gap: 0.5rem;
  flex-wrap: wrap;
  margin-top: 0.75rem;
}

.welcome-actions .btn {
  border-radius: 999px;
}

.welcome-title {
  font-size: var(--eo-font-size-3xl);
  font-weight: 700;
  margin: 0;
  font-family: var(--eo-font-family);
}

.welcome-subtitle {
  font-size: var(--eo-font-size-lg);
  margin: var(--eo-spacing-sm) 0 0 0;
  opacity: 0.9;
}

.espace-content {
  padding: var(--eo-spacing-2xl) 0;
}

.nav-tabs {
  border-bottom: 2px solid var(--eo-gray-200);
  margin-bottom: var(--eo-spacing-xl);
}

.nav-link {
  color: var(--eo-gray-600);
  font-weight: 500;
  padding: var(--eo-spacing-md) var(--eo-spacing-lg);
  border: none;
  border-bottom: 3px solid transparent;
  transition: all var(--eo-transition-base);
}

.nav-link:hover {
  color: var(--eo-primary);
  border-color: transparent;
}

.nav-link.active {
  color: var(--eo-primary);
  background: transparent;
  border-bottom-color: var(--eo-primary);
}

.content-section {
  background: white;
  border-radius: var(--eo-radius-xl);
  box-shadow: var(--eo-shadow-md);
  padding: var(--eo-spacing-xl);
}

.no-results {
  text-align: center;
  padding: var(--eo-spacing-3xl) var(--eo-spacing-xl);
}

.no-results-icon {
  font-size: 4rem;
  color: var(--eo-gray-300);
  margin-bottom: var(--eo-spacing-lg);
}

.no-results h3 {
  font-size: var(--eo-font-size-2xl);
  color: var(--eo-gray-700);
  margin-bottom: var(--eo-spacing-sm);
}

.no-results p {
  color: var(--eo-gray-500);
  margin-bottom: var(--eo-spacing-lg);
}

.table {
  margin-bottom: 0;
}

.table th {
  font-weight: 600;
  color: var(--eo-gray-700);
  border-bottom: 2px solid var(--eo-gray-200);
  padding: var(--eo-spacing-md);
}

.table td {
  padding: var(--eo-spacing-md);
  vertical-align: middle;
  border-bottom: 1px solid var(--eo-gray-100);
}

.table-hover tbody tr:hover {
  background-color: var(--eo-gray-50);
}

.formation-name {
  font-weight: 600;
  color: var(--eo-gray-800);
}

.btn-group {
  display: flex;
  gap: var(--eo-spacing-xs);
}

.filters-bar {
  background: var(--eo-gray-50);
  padding: var(--eo-spacing-lg);
  border-radius: var(--eo-radius-lg);
}

.form-select,
.form-control {
  border-color: var(--eo-gray-300);
}

.form-select:focus,
.form-control:focus {
  border-color: var(--eo-primary);
  box-shadow: 0 0 0 0.2rem rgba(0, 102, 204, 0.25);
}

.profile-display .form-control-plaintext {
  padding: var(--eo-spacing-md);
  background: var(--eo-gray-50);
  border-radius: var(--eo-radius-md);
  font-weight: 500;
  color: var(--eo-gray-800);
}

.form-label {
  font-weight: 500;
  color: var(--eo-gray-700);
  margin-bottom: var(--eo-spacing-sm);
}

.form-actions {
  display: flex;
  gap: var(--eo-spacing-md);
  margin-top: var(--eo-spacing-lg);
  padding-top: var(--eo-spacing-lg);
  border-top: 1px solid var(--eo-gray-200);
}

.btn-primary {
  background-color: var(--eo-primary);
  border-color: var(--eo-primary);
  padding: var(--eo-spacing-md) var(--eo-spacing-xl);
  font-weight: 600;
  border-radius: var(--eo-radius-lg);
  transition: all var(--eo-transition-base);
}

.btn-primary:hover:not(:disabled) {
  background-color: var(--eo-primary-dark);
  border-color: var(--eo-primary-dark);
  transform: translateY(-2px);
  box-shadow: var(--eo-shadow-lg);
}

.btn-primary:disabled {
  opacity: 0.7;
}

.btn-outline-primary,
.btn-outline-success,
.btn-outline-danger,
.btn-outline-secondary {
  border-radius: var(--eo-radius-md);
  transition: all var(--eo-transition-base);
}

.btn-outline-primary:hover {
  background-color: var(--eo-primary);
  border-color: var(--eo-primary);
  color: white;
}

.btn-outline-success:hover {
  background-color: var(--eo-success);
  border-color: var(--eo-success);
  color: white;
}

.btn-outline-danger:hover {
  background-color: var(--eo-danger);
  border-color: var(--eo-danger);
  color: white;
}

.btn-outline-secondary:hover {
  background-color: var(--eo-gray-600);
  border-color: var(--eo-gray-600);
  color: white;
}

@media (max-width: 768px) {
  .user-welcome {
    flex-direction: column;
    text-align: center;
  }
  
  .welcome-title {
    font-size: var(--eo-font-size-2xl);
  }
  
  .nav-tabs {
    flex-wrap: nowrap;
    overflow-x: auto;
  }
  
  .nav-link {
    white-space: nowrap;
  }
  
  .table-responsive {
    font-size: var(--eo-font-size-sm);
  }
  
  .btn-group {
    flex-direction: column;
  }
  
  .form-actions {
    flex-direction: column;
  }
  
  .form-actions .btn {
    width: 100%;
  }
}
</style>
