<template>
  <div class="mon-espace-container">
    <div class="espace-header">
      <div class="header-content">
        <div class="user-welcome">
          <div class="avatar-section">
            <div class="avatar">
              <i class="bi bi-person-fill"></i>
            </div>
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
        <div class="row">
          <!-- Sidebar -->
          <div class="col-lg-3 mb-4">
            <div class="sidebar-card">
              <div class="sidebar-menu">
                <div
                  class="menu-item"
                  :class="{ active: activeTab === 'overview' }"
                  @click="activeTab = 'overview'"
                >
                  <i class="bi bi-grid-fill me-2"></i>
                  Vue d'ensemble
                </div>
                <div
                  class="menu-item"
                  :class="{ active: activeTab === 'inscriptions' }"
                  @click="activeTab = 'inscriptions'"
                >
                  <i class="bi bi-journal-check me-2"></i>
                  Mes inscriptions
                </div>
                <div
                  class="menu-item"
                  :class="{ active: activeTab === 'recus' }"
                  @click="activeTab = 'recus'"
                >
                  <i class="bi bi-receipt me-2"></i>
                  Mes reçus
                </div>
                <div
                  class="menu-item"
                  :class="{ active: activeTab === 'courses' }"
                  @click="activeTab = 'courses'"
                >
                  <i class="bi bi-book-fill me-2"></i>
                  Mes formations
                </div>
                <div
                  class="menu-item"
                  :class="{ active: activeTab === 'certificates' }"
                  @click="activeTab = 'certificates'"
                >
                  <i class="bi bi-award-fill me-2"></i>
                  Certificats
                </div>
                <div
                  class="menu-item"
                  :class="{ active: activeTab === 'profile' }"
                  @click="activeTab = 'profile'"
                >
                  <i class="bi bi-person-gear me-2"></i>
                  Mon profil
                </div>
                <div
                  class="menu-item"
                  :class="{ active: activeTab === 'settings' }"
                  @click="activeTab = 'settings'"
                >
                  <i class="bi bi-gear-fill me-2"></i>
                  Paramètres
                </div>
              </div>

              <div class="sidebar-footer">
                <button class="btn btn-outline-danger w-100" @click="handleLogout">
                  <i class="bi bi-box-arrow-right me-2"></i>
                  Déconnexion
                </button>
              </div>
            </div>
          </div>

          <!-- Main Content -->
          <div class="col-lg-9">
            <!-- Overview Tab -->
            <div v-if="activeTab === 'overview'" class="content-section">
              <div class="stats-grid">
                <div class="stat-card">
                  <div class="stat-icon courses">
                    <i class="bi bi-book-fill"></i>
                  </div>
                  <div class="stat-content">
                    <h3 class="stat-value">3</h3>
                    <p class="stat-label">Formations en cours</p>
                  </div>
                </div>
                <div class="stat-card">
                  <div class="stat-icon completed">
                    <i class="bi bi-check-circle-fill"></i>
                  </div>
                  <div class="stat-content">
                    <h3 class="stat-value">12</h3>
                    <p class="stat-label">Formations terminées</p>
                  </div>
                </div>
                <div class="stat-card">
                  <div class="stat-icon certificates">
                    <i class="bi bi-award-fill"></i>
                  </div>
                  <div class="stat-content">
                    <h3 class="stat-value">8</h3>
                    <p class="stat-label">Certificats obtenus</p>
                  </div>
                </div>
                <div class="stat-card">
                  <div class="stat-icon hours">
                    <i class="bi bi-clock-fill"></i>
                  </div>
                  <div class="stat-content">
                    <h3 class="stat-value">45h</h3>
                    <p class="stat-label">Temps de formation</p>
                  </div>
                </div>
              </div>

              <div class="recent-activity">
                <h3 class="section-title">
                  <i class="bi bi-clock-history me-2"></i>
                  Activité récente
                </h3>
                <div class="activity-list">
                  <div class="activity-item">
                    <div class="activity-icon">
                      <i class="bi bi-play-circle-fill"></i>
                    </div>
                    <div class="activity-content">
                      <h4 class="activity-title">Développement Web avec Vue.js</h4>
                      <p class="activity-meta">Il y a 2 heures • Leçon 5/20</p>
                    </div>
                    <div class="activity-progress">
                      <div class="progress" style="height: 6px;">
                        <div class="progress-bar" style="width: 25%"></div>
                      </div>
                      <small>25%</small>
                    </div>
                  </div>
                  <div class="activity-item">
                    <div class="activity-icon">
                      <i class="bi bi-play-circle-fill"></i>
                    </div>
                    <div class="activity-content">
                      <h4 class="activity-title">Introduction à Python</h4>
                      <p class="activity-meta">Hier • Leçon 12/15</p>
                    </div>
                    <div class="activity-progress">
                      <div class="progress" style="height: 6px;">
                        <div class="progress-bar" style="width: 80%"></div>
                      </div>
                      <small>80%</small>
                    </div>
                  </div>
                  <div class="activity-item">
                    <div class="activity-icon completed">
                      <i class="bi bi-check-circle-fill"></i>
                    </div>
                    <div class="activity-content">
                      <h4 class="activity-title">Bases de JavaScript</h4>
                      <p class="activity-meta">Il y a 3 jours • Terminé</p>
                    </div>
                    <div class="activity-badge">
                      <span class="badge bg-success">Certificat</span>
                    </div>
                  </div>
                </div>
              </div>
            </div>

            <!-- Inscriptions Tab -->
            <div v-if="activeTab === 'inscriptions'" class="content-section">
              <h3 class="section-title">
                <i class="bi bi-journal-check me-2"></i>
                Mes inscriptions
              </h3>

              <div v-if="loadingInscriptions" class="text-center py-5">
                <div class="spinner-border text-primary" role="status">
                  <span class="visually-hidden">Chargement...</span>
                </div>
              </div>

              <div v-else-if="inscriptions.length === 0" class="no-results">
                <div class="no-results-icon">
                  <i class="bi bi-book"></i>
                </div>
                <h4>Aucune inscription</h4>
                <p>Vous n'êtes inscrit à aucune formation pour le moment</p>
                <router-link to="/recherche" class="btn btn-primary">
                  <i class="bi bi-search me-2"></i>
                  Rechercher une formation
                </router-link>
              </div>

              <div v-else class="table-responsive">
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
                        <div class="fw-bold">{{ ins.formation }}</div>
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
                          <button class="btn btn-sm btn-outline-primary" @click="viewDetails(ins)" title="Voir détails">
                            <i class="bi bi-eye"></i>
                          </button>
                          <button v-if="ins.paiement_confirme" class="btn btn-sm btn-outline-success" @click="downloadRecu(ins)" title="Télécharger reçu">
                            <i class="bi bi-download"></i>
                          </button>
                          <button v-if="ins.statut !== 'termine'" class="btn btn-sm btn-outline-danger" @click="confirmCancel(ins)" title="Annuler">
                            <i class="bi bi-x-lg"></i>
                          </button>
                        </div>
                      </td>
                    </tr>
                  </tbody>
                </table>
              </div>
            </div>

            <!-- Recus Tab -->
            <div v-if="activeTab === 'recus'" class="content-section">
              <h3 class="section-title">
                <i class="bi bi-receipt me-2"></i>
                Mes reçus
              </h3>

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
                <h4>Aucun reçu trouvé</h4>
                <p>Aucun reçu ne correspond à vos critères</p>
              </div>

              <div v-else class="table-responsive">
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
                        <button class="btn btn-sm btn-success" :disabled="recu.statut !== 'paye'" @click="downloadRecu(recu)">
                          <i class="bi bi-download me-1"></i>
                          Télécharger
                        </button>
                      </td>
                    </tr>
                  </tbody>
                </table>
              </div>
            </div>

            <!-- Courses Tab -->
            <div v-if="activeTab === 'courses'" class="content-section">
              <h3 class="section-title">
                <i class="bi bi-book-fill me-2"></i>
                Mes formations
              </h3>
              <div class="courses-grid">
                <div class="course-card">
                  <div class="course-image">
                    <div class="course-category">Développement</div>
                  </div>
                  <div class="course-content">
                    <h4 class="course-title">Développement Web avec Vue.js</h4>
                    <p class="course-description">Apprenez à créer des applications web modernes avec Vue 3</p>
                    <div class="course-meta">
                      <span><i class="bi bi-clock me-1"></i> 20h</span>
                      <span><i class="bi bi-play-circle me-1"></i> 20 leçons</span>
                    </div>
                    <div class="course-progress">
                      <div class="progress" style="height: 8px;">
                        <div class="progress-bar" style="width: 25%"></div>
                      </div>
                      <small>25% complété</small>
                    </div>
                    <button class="btn btn-primary w-100 mt-3">Continuer</button>
                  </div>
                </div>
                <div class="course-card">
                  <div class="course-image">
                    <div class="course-category">Data Science</div>
                  </div>
                  <div class="course-content">
                    <h4 class="course-title">Introduction à Python</h4>
                    <p class="course-description">Les fondamentaux de la programmation en Python</p>
                    <div class="course-meta">
                      <span><i class="bi bi-clock me-1"></i> 15h</span>
                      <span><i class="bi bi-play-circle me-1"></i> 15 leçons</span>
                    </div>
                    <div class="course-progress">
                      <div class="progress" style="height: 8px;">
                        <div class="progress-bar" style="width: 80%"></div>
                      </div>
                      <small>80% complété</small>
                    </div>
                    <button class="btn btn-primary w-100 mt-3">Continuer</button>
                  </div>
                </div>
              </div>
            </div>

            <!-- Certificates Tab -->
            <div v-if="activeTab === 'certificates'" class="content-section">
              <h3 class="section-title">
                <i class="bi bi-award-fill me-2"></i>
                Mes certificats
              </h3>
              <div class="certificates-grid">
                <div class="certificate-card">
                  <div class="certificate-icon">
                    <i class="bi bi-award-fill"></i>
                  </div>
                  <div class="certificate-content">
                    <h4 class="certificate-title">Bases de JavaScript</h4>
                    <p class="certificate-date">Obtenu le 15 Janvier 2026</p>
                    <button class="btn btn-outline-primary">
                      <i class="bi bi-download me-2"></i>
                      Télécharger
                    </button>
                  </div>
                </div>
                <div class="certificate-card">
                  <div class="certificate-icon">
                    <i class="bi bi-award-fill"></i>
                  </div>
                  <div class="certificate-content">
                    <h4 class="certificate-title">HTML & CSS Fundamentals</h4>
                    <p class="certificate-date">Obtenu le 10 Décembre 2025</p>
                    <button class="btn btn-outline-primary">
                      <i class="bi bi-download me-2"></i>
                      Télécharger
                    </button>
                  </div>
                </div>
              </div>
            </div>

            <!-- Profile Tab -->
            <div v-if="activeTab === 'profile'" class="content-section">
              <h3 class="section-title">
                <i class="bi bi-person-gear me-2"></i>
                Mon profil
              </h3>

              <div v-if="loadingProfile" class="text-center py-5">
                <div class="spinner-border text-primary" role="status">
                  <span class="visually-hidden">Chargement...</span>
                </div>
              </div>

              <div v-else>
                <div v-if="!editing" class="profile-card">
                  <div class="row">
                    <div class="col-md-6 mb-3">
                      <label class="form-label fw-bold">Prénom</label>
                      <div class="form-control-plaintext profile-field">{{ authStore.user?.prenom || '—' }}</div>
                    </div>
                    <div class="col-md-6 mb-3">
                      <label class="form-label fw-bold">Nom</label>
                      <div class="form-control-plaintext profile-field">{{ authStore.user?.nom || '—' }}</div>
                    </div>
                  </div>
                  <div class="row">
                    <div class="col-md-6 mb-3">
                      <label class="form-label fw-bold">Email</label>
                      <div class="form-control-plaintext profile-field">{{ authStore.user?.email || '—' }}</div>
                    </div>
                    <div class="col-md-6 mb-3">
                      <label class="form-label fw-bold">Téléphone</label>
                      <div class="form-control-plaintext profile-field">{{ authStore.user?.telephone || 'Non renseigné' }}</div>
                    </div>
                  </div>
                  <button class="btn btn-primary" @click="toggleEdit">
                    <i class="bi bi-pencil me-2"></i>
                    Modifier
                  </button>
                </div>

                <div v-else class="profile-card">
                  <form @submit.prevent="saveProfile">
                    <div class="row">
                      <div class="col-md-6 mb-3">
                        <label class="form-label">Prénom *</label>
                        <input type="text" class="form-control" v-model="editForm.prenom" :class="{ 'is-invalid': profileErrors.prenom }" />
                        <div class="invalid-feedback" v-if="profileErrors.prenom">{{ profileErrors.prenom }}</div>
                      </div>
                      <div class="col-md-6 mb-3">
                        <label class="form-label">Nom *</label>
                        <input type="text" class="form-control" v-model="editForm.nom" :class="{ 'is-invalid': profileErrors.nom }" />
                        <div class="invalid-feedback" v-if="profileErrors.nom">{{ profileErrors.nom }}</div>
                      </div>
                    </div>
                    <div class="row">
                      <div class="col-md-6 mb-3">
                        <label class="form-label">Email</label>
                        <input type="email" class="form-control" :value="authStore.user?.email" disabled />
                        <small class="form-text text-muted">L'email ne peut pas être modifié</small>
                      </div>
                      <div class="col-md-6 mb-3">
                        <label class="form-label">Téléphone</label>
                        <input type="tel" class="form-control" v-model="editForm.telephone" placeholder="+261 34 00 000 00" />
                      </div>
                    </div>
                    <div class="row">
                      <div class="col-md-6 mb-3">
                        <label class="form-label">Nouveau mot de passe</label>
                        <input type="password" class="form-control" v-model="editForm.password" placeholder="Laisser vide pour conserver" :class="{ 'is-invalid': profileErrors.password }" />
                        <div class="invalid-feedback" v-if="profileErrors.password">{{ profileErrors.password }}</div>
                      </div>
                      <div class="col-md-6 mb-3">
                        <label class="form-label">Confirmer le mot de passe</label>
                        <input type="password" class="form-control" v-model="editForm.confirmPassword" placeholder="Confirmer" :class="{ 'is-invalid': profileErrors.confirmPassword }" />
                        <div class="invalid-feedback" v-if="profileErrors.confirmPassword">{{ profileErrors.confirmPassword }}</div>
                      </div>
                    </div>

                    <div v-if="apiError" class="alert alert-danger">{{ apiError }}</div>

                    <div class="d-flex gap-3 mt-3">
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

            <!-- Settings Tab -->
            <div v-if="activeTab === 'settings'" class="content-section">
              <h3 class="section-title">
                <i class="bi bi-gear-fill me-2"></i>
                Paramètres
              </h3>
              <div class="settings-card">
                <div class="setting-item">
                  <div class="setting-info">
                    <h4>Notifications par email</h4>
                    <p>Recevoir des notifications sur votre progression</p>
                  </div>
                  <div class="form-check form-switch">
                    <input class="form-check-input" type="checkbox" checked />
                  </div>
                </div>
                <div class="setting-item">
                  <div class="setting-info">
                    <h4>Mode sombre</h4>
                    <p>Activer le thème sombre</p>
                  </div>
                  <div class="form-check form-switch">
                    <input class="form-check-input" type="checkbox" />
                  </div>
                </div>
                <div class="setting-item">
                  <div class="setting-info">
                    <h4>Langue</h4>
                    <p>Choisir la langue de l'interface</p>
                  </div>
                  <select class="form-select" style="width: auto;">
                    <option>Français</option>
                    <option>English</option>
                    <option>Malagasy</option>
                  </select>
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
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/authStore'
import {
  getMesInscriptions,
  getMesRecus,
  getMesStats,
  telechargerRecuApprenant
} from '../api/apprenantEspace'

console.log('✅ MON ESPACE RACINE DYNAMIQUE CHARGÉ')

const router = useRouter()
const authStore = useAuthStore()

const activeTab = ref('inscriptions')
const editing = ref(false)
const submitting = ref(false)
const apiError = ref('')

const loadingInscriptions = ref(false)
const loadingRecus = ref(false)
const loadingProfile = ref(false)

const errors = ref({})

const stats = ref({
  totalInscriptions: 0,
  formationsEnCours: 0,
  formationsTerminees: 0,
  certificats: 0,
  tempsFormation: '0h',
  totalPaye: 0
})

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

const inscriptions = ref([])
const recus = ref([])

const getUtilisateurConnecte = () => {
  if (authStore.user) {
    return authStore.user
  }

  const auth = localStorage.getItem('auth')
  const utilisateur = localStorage.getItem('utilisateur')

  if (auth) {
    try {
      const parsed = JSON.parse(auth)

      if (parsed?.user) {
        return parsed.user
      }
    } catch (error) {
      console.error('Erreur lecture auth localStorage', error)
    }
  }

  if (utilisateur) {
    try {
      return JSON.parse(utilisateur)
    } catch (error) {
      console.error('Erreur lecture utilisateur localStorage', error)
    }
  }

  return null
}

const getIdUser = () => {
  const utilisateur = getUtilisateurConnecte()

  if (utilisateur?.idUser) {
    return Number(utilisateur.idUser)
  }

  if (utilisateur?.id) {
    return Number(utilisateur.id)
  }

  const token = localStorage.getItem('token') || localStorage.getItem('authToken')

  if (token && token.startsWith('TOKEN-DEMO-')) {
    const id = Number(token.replace('TOKEN-DEMO-', ''))

    if (!Number.isNaN(id)) {
      return id
    }
  }

  return 1
}

const initials = computed(() => {
  const p = authStore.user?.prenom || ''
  const n = authStore.user?.nom || ''

  if (p && n) {
    return (p[0] + n[0]).toUpperCase()
  }

  if (authStore.user?.email) {
    return authStore.user.email[0].toUpperCase()
  }

  return 'U'
})

const displayName = computed(() => {
  const p = authStore.user?.prenom || ''
  const n = authStore.user?.nom || ''

  if (p && n) {
    return p + ' ' + n
  }

  return authStore.user?.email?.split('@')[0] || 'Utilisateur'
})

const uniqueFormations = computed(() => {
  return [...new Set(recus.value.map((recu) => recu.formation))]
})

const filteredRecus = computed(() => {
  return recus.value.filter((recu) => {
    if (recuFilters.value.formation && recu.formation !== recuFilters.value.formation) {
      return false
    }

    if (recuFilters.value.date) {
      const recuDate = toInputDate(recu.date)

      if (recuDate !== recuFilters.value.date) {
        return false
      }
    }

    return true
  })
})

const toInputDate = (date) => {
  if (!date) {
    return ''
  }

  const d = new Date(date)

  if (Number.isNaN(d.getTime())) {
    return ''
  }

  return d.toISOString().slice(0, 10)
}

const normaliserInscription = (item) => {
  const statutOriginal = String(item.statut || item.statutOriginal || '').toLowerCase()

  let statut = 'inscrit'

  if (statutOriginal.includes('reserve')) {
    statut = 'reserve'
  } else if (statutOriginal.includes('termine')) {
    statut = 'termine'
  } else if (statutOriginal.includes('annule')) {
    statut = 'annule'
  } else if (statutOriginal.includes('attente')) {
    statut = 'enAttente'
  }

  return {
    id: item.idInscription || item.id,
    idInscription: item.idInscription || item.id,
    idFormation: item.idFormation,

    formation: item.formation || item.titre || 'Formation',
    ecole: item.ecole || item.centre || 'Centre',
    centre: item.centre || item.ecole || 'Centre',
    categorie: item.categorie || 'Formation',

    date: item.date || item.createdAt || item.dateDebut,
    statut,
    paiement_confirme: statut !== 'enAttente',

    progression: Number(item.progression || 0),

    prix: Number(item.montantPaye || item.montant || item.prix || 0),
    montant: Number(item.montantPaye || item.montant || item.prix || 0),

    duree: item.duree || '',

    numeroRecu: item.numeroRecu,
    operateur: item.operateur,
    transactionId: item.transactionId
  }
}

const normaliserRecu = (item) => {
  const statutOriginal = String(item.statut || item.statutOriginal || '').toLowerCase()

  let statut = 'en_attente'

  if (
    statutOriginal.includes('paye') ||
    statutOriginal.includes('payé') ||
    statutOriginal.includes('confirme')
  ) {
    statut = 'paye'
  }

  return {
    id: item.idPaiement || item.id || item.idInscription,
    idPaiement: item.idPaiement,
    idInscription: item.idInscription,

    formation: item.formation || 'Formation',
    centre: item.centre || 'Centre',

    date: item.date || item.datePaiement || item.dateInscription,

    montant: Number(item.montant || 0),

    reference: item.reference || item.numeroRecu || `RECU-${item.idInscription}`,
    numeroRecu: item.numeroRecu,
    numeroTransaction: item.numeroTransaction,

    methode: item.methode || 'Mobile Money',
    operateur: item.operateur || '',

    statut
  }
}

const getStatusClass = (statut) => {
  switch (statut) {
    case 'inscrit':
      return 'bg-primary'

    case 'reserve':
      return 'bg-warning'

    case 'termine':
      return 'bg-success'

    case 'enAttente':
      return 'bg-warning'

    case 'annule':
      return 'bg-danger'

    default:
      return 'bg-secondary'
  }
}

const getStatusLabel = (statut) => {
  switch (statut) {
    case 'inscrit':
      return 'Inscrit'

    case 'reserve':
      return 'Réservé'

    case 'termine':
      return 'Terminé'

    case 'enAttente':
      return 'En attente'

    case 'annule':
      return 'Annulé'

    default:
      return statut
  }
}

function formatPrice(value) {
  return new Intl.NumberFormat('fr-FR').format(Number(value || 0))
}

function formatDate(date) {
  if (!date) {
    return '-'
  }

  const d = new Date(date)

  if (Number.isNaN(d.getTime())) {
    return date
  }

  return d.toLocaleDateString('fr-FR', {
    day: 'numeric',
    month: 'long',
    year: 'numeric'
  })
}

function viewDetails(inscription) {
  const idFormation = inscription.idFormation || inscription.id

  if (idFormation) {
    router.push(`/formations/${idFormation}`)
  }
}

async function downloadRecu(item) {
  try {
    const idInscription = item.idInscription || item.id

    if (!idInscription) {
      alert('Aucune inscription trouvée pour ce reçu.')
      return
    }

    await telechargerRecuApprenant(idInscription)
  } catch (error) {
    console.error('Erreur téléchargement reçu :', error)
    alert('Erreur lors du téléchargement du reçu PDF.')
  }
}

function confirmCancel(inscription) {
  if (confirm(`Êtes-vous sûr de vouloir annuler l'inscription à "${inscription.formation}" ?`)) {
    cancelInscription(inscription.id)
  }
}

async function cancelInscription(id) {
  try {
    console.log('Annulation inscription non encore connectée :', id)
    alert("L'annulation n'est pas encore connectée côté backend.")
  } catch (error) {
    console.error("Erreur lors de l'annulation :", error)
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
    await new Promise((resolve) => setTimeout(resolve, 500))

    if (authStore.user) {
      authStore.user.prenom = editForm.value.prenom
      authStore.user.nom = editForm.value.nom
      authStore.user.telephone = editForm.value.telephone

      localStorage.setItem('utilisateur', JSON.stringify(authStore.user))
      localStorage.setItem(
        'auth',
        JSON.stringify({
          user: authStore.user,
          token: authStore.token || localStorage.getItem('token')
        })
      )
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
    const idUser = getIdUser()
    const data = await getMesInscriptions(idUser)

    console.log('✅ Inscriptions API =', data)

    inscriptions.value = data.map(normaliserInscription)
  } catch (error) {
    console.error('Erreur lors du chargement des inscriptions :', error)
    inscriptions.value = []
  } finally {
    loadingInscriptions.value = false
  }
}

async function loadRecus() {
  loadingRecus.value = true

  try {
    const idUser = getIdUser()
    const data = await getMesRecus(idUser)

    console.log('✅ Reçus API =', data)

    recus.value = data.map(normaliserRecu)
  } catch (error) {
    console.error('Erreur lors du chargement des reçus :', error)
    recus.value = []
  } finally {
    loadingRecus.value = false
  }
}

async function loadStats() {
  try {
    const idUser = getIdUser()
    const data = await getMesStats(idUser)

    console.log('✅ Stats API =', data)

    stats.value = {
      totalInscriptions: Number(data.totalInscriptions || 0),
      formationsEnCours: Number(data.formationsEnCours || 0),
      formationsTerminees: Number(data.formationsTerminees || 0),
      certificats: Number(data.certificats || 0),
      tempsFormation: data.tempsFormation || '0h',
      totalPaye: Number(data.totalPaye || 0)
    }
  } catch (error) {
    console.error('Erreur lors du chargement des statistiques :', error)
  }
}

async function loadProfile() {
  loadingProfile.value = true

  try {
    const utilisateur = getUtilisateurConnecte()

    if (utilisateur) {
      editForm.value.prenom = utilisateur.prenom || ''
      editForm.value.nom = utilisateur.nom || ''
      editForm.value.telephone = utilisateur.telephone || ''
    }

    await new Promise((resolve) => setTimeout(resolve, 100))
  } catch (error) {
    console.error('Erreur lors du chargement du profil :', error)
  } finally {
    loadingProfile.value = false
  }
}

onMounted(() => {
  loadInscriptions()
  loadRecus()
  loadStats()
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

.header-content {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 var(--eo-spacing-md);
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

.sidebar-card {
  background: white;
  border-radius: var(--eo-radius-xl);
  box-shadow: var(--eo-shadow-md);
  padding: var(--eo-spacing-lg);
  position: sticky;
  top: var(--eo-spacing-lg);
}

.sidebar-menu {
  display: flex;
  flex-direction: column;
  gap: var(--eo-spacing-xs);
  margin-bottom: var(--eo-spacing-xl);
}

.menu-item {
  display: flex;
  align-items: center;
  padding: var(--eo-spacing-md);
  border-radius: var(--eo-radius-lg);
  cursor: pointer;
  transition: all var(--eo-transition-base);
  color: var(--eo-gray-600);
  font-weight: 500;
}

.menu-item:hover {
  background: var(--eo-gray-100);
  color: var(--eo-primary);
}

.menu-item.active {
  background: var(--eo-primary);
  color: white;
}

.menu-item i {
  font-size: 1.1rem;
}

.sidebar-footer {
  padding-top: var(--eo-spacing-lg);
  border-top: 1px solid var(--eo-gray-200);
}

.content-section {
  background: white;
  border-radius: var(--eo-radius-xl);
  box-shadow: var(--eo-shadow-md);
  padding: var(--eo-spacing-xl);
}

.section-title {
  color: var(--eo-primary);
  font-size: var(--eo-font-size-2xl);
  font-weight: 700;
  margin-bottom: var(--eo-spacing-xl);
  font-family: var(--eo-font-family);
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: var(--eo-spacing-lg);
  margin-bottom: var(--eo-spacing-2xl);
}

.stat-card {
  background: var(--eo-gray-50);
  border-radius: var(--eo-radius-xl);
  padding: var(--eo-spacing-lg);
  display: flex;
  align-items: center;
  gap: var(--eo-spacing-md);
  transition: all var(--eo-transition-base);
}

.stat-card:hover {
  transform: translateY(-4px);
  box-shadow: var(--eo-shadow-md);
}

.stat-icon {
  width: 60px;
  height: 60px;
  border-radius: var(--eo-radius-lg);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.5rem;
  color: white;
  flex-shrink: 0;
}

.stat-icon.courses {
  background: var(--eo-primary);
}

.stat-icon.completed {
  background: var(--eo-success);
}

.stat-icon.certificates {
  background: var(--eo-secondary);
}

.stat-icon.hours {
  background: var(--eo-info);
}

.stat-content {
  flex: 1;
}

.stat-value {
  font-size: var(--eo-font-size-2xl);
  font-weight: 700;
  margin: 0;
  color: var(--eo-gray-800);
}

.stat-label {
  font-size: var(--eo-font-size-sm);
  color: var(--eo-gray-500);
  margin: 0;
}

.recent-activity {
  margin-top: var(--eo-spacing-2xl);
}

.activity-list {
  display: flex;
  flex-direction: column;
  gap: var(--eo-spacing-md);
}

.activity-item {
  display: flex;
  align-items: center;
  gap: var(--eo-spacing-md);
  padding: var(--eo-spacing-md);
  border-radius: var(--eo-radius-lg);
  background: var(--eo-gray-50);
  transition: all var(--eo-transition-base);
}

.activity-item:hover {
  background: var(--eo-gray-100);
}

.activity-icon {
  width: 50px;
  height: 50px;
  border-radius: var(--eo-radius-lg);
  background: var(--eo-primary);
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.3rem;
  flex-shrink: 0;
}

.activity-icon.completed {
  background: var(--eo-success);
}

.activity-content {
  flex: 1;
}

.activity-title {
  font-size: var(--eo-font-size-lg);
  font-weight: 600;
  margin: 0;
  color: var(--eo-gray-800);
}

.activity-meta {
  font-size: var(--eo-font-size-sm);
  color: var(--eo-gray-500);
  margin: var(--eo-spacing-xs) 0 0 0;
}

.activity-progress {
  width: 120px;
  text-align: right;
}

.activity-progress small {
  display: block;
  margin-top: var(--eo-spacing-xs);
  font-size: var(--eo-font-size-xs);
  color: var(--eo-gray-500);
}

.activity-badge {
  flex-shrink: 0;
}

.courses-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: var(--eo-spacing-lg);
}

.course-card {
  border: 1px solid var(--eo-gray-200);
  border-radius: var(--eo-radius-xl);
  overflow: hidden;
  transition: all var(--eo-transition-base);
}

.course-card:hover {
  box-shadow: var(--eo-shadow-lg);
  transform: translateY(-4px);
}

.course-image {
  height: 150px;
  background: linear-gradient(135deg, var(--eo-primary) 0%, var(--eo-primary-light) 100%);
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
}

.course-category {
  background: rgba(255, 255, 255, 0.9);
  padding: var(--eo-spacing-sm) var(--eo-spacing-md);
  border-radius: var(--eo-radius-full);
  font-size: var(--eo-font-size-sm);
  font-weight: 600;
  color: var(--eo-primary);
}

.course-content {
  padding: var(--eo-spacing-lg);
}

.course-title {
  font-size: var(--eo-font-size-lg);
  font-weight: 600;
  margin: 0 0 var(--eo-spacing-sm) 0;
  color: var(--eo-gray-800);
}

.course-description {
  font-size: var(--eo-font-size-sm);
  color: var(--eo-gray-600);
  margin: 0 0 var(--eo-spacing-md) 0;
}

.course-meta {
  display: flex;
  gap: var(--eo-spacing-md);
  font-size: var(--eo-font-size-sm);
  color: var(--eo-gray-500);
  margin-bottom: var(--eo-spacing-md);
}

.course-progress {
  margin-bottom: var(--eo-spacing-md);
}

.course-progress small {
  display: block;
  margin-top: var(--eo-spacing-xs);
  font-size: var(--eo-font-size-xs);
  color: var(--eo-gray-500);
}

.certificates-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: var(--eo-spacing-lg);
}

.certificate-card {
  display: flex;
  align-items: center;
  gap: var(--eo-spacing-md);
  padding: var(--eo-spacing-lg);
  border: 1px solid var(--eo-gray-200);
  border-radius: var(--eo-radius-xl);
  transition: all var(--eo-transition-base);
}

.certificate-card:hover {
  box-shadow: var(--eo-shadow-md);
  border-color: var(--eo-primary);
}

.certificate-icon {
  width: 60px;
  height: 60px;
  border-radius: var(--eo-radius-lg);
  background: var(--eo-secondary);
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.5rem;
  flex-shrink: 0;
}

.certificate-content {
  flex: 1;
}

.certificate-title {
  font-size: var(--eo-font-size-lg);
  font-weight: 600;
  margin: 0 0 var(--eo-spacing-xs) 0;
  color: var(--eo-gray-800);
}

.certificate-date {
  font-size: var(--eo-font-size-sm);
  color: var(--eo-gray-500);
  margin: 0 0 var(--eo-spacing-md) 0;
}

.profile-card,
.settings-card {
  background: var(--eo-gray-50);
  border-radius: var(--eo-radius-xl);
  padding: var(--eo-spacing-xl);
}

.setting-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: var(--eo-spacing-lg) 0;
  border-bottom: 1px solid var(--eo-gray-200);
}

.setting-item:last-child {
  border-bottom: none;
}

.setting-info h4 {
  font-size: var(--eo-font-size-lg);
  font-weight: 600;
  margin: 0 0 var(--eo-spacing-xs) 0;
  color: var(--eo-gray-800);
}

.setting-info p {
  font-size: var(--eo-font-size-sm);
  color: var(--eo-gray-500);
  margin: 0;
}

.form-check-input:checked {
  background-color: var(--eo-primary);
  border-color: var(--eo-primary);
}

.btn-primary {
  background-color: var(--eo-primary) !important;
  border-color: var(--eo-primary) !important;
  color: #ffffff !important; /* Force le texte et l'icône en blanc tout le temps */
}

.btn-primary:hover, 
.btn-primary:focus, 
.btn-primary:active {
  background-color: var(--eo-primary-dark) !important;
  border-color: var(--eo-primary-dark) !important;
  color: #ffffff !important;
}

.btn-outline-primary:hover, 
.btn-outline-primary:focus, 
.btn-outline-primary:active {
  background-color: var(--eo-primary) !important;
  border-color: var(--eo-primary) !important;
  color: #ffffff !important; /* Devient blanc uniquement au survol */
}

.btn-outline-primary {
  color: var(--eo-primary) !important; /* Force le texte à la couleur primaire */
  border-color: var(--eo-primary) !important;
  background-color: transparent !important;
}



.btn-outline-primary:hover {
  background-color: var(--eo-primary);
  border-color: var(--eo-primary);
}

.progress-bar {
  background-color: var(--eo-primary);
}

.no-results {
  text-align: center;
  padding: var(--eo-spacing-3xl, 3rem) var(--eo-spacing-xl, 1.5rem);
}

.no-results-icon {
  font-size: 4rem;
  color: var(--eo-gray-300);
  margin-bottom: var(--eo-spacing-lg, 1rem);
}

.no-results h4 {
  color: var(--eo-gray-700);
  margin-bottom: var(--eo-spacing-sm, 0.5rem);
}

.no-results p {
  color: var(--eo-gray-500);
  margin-bottom: var(--eo-spacing-lg, 1rem);
}

.filters-bar {
  background: var(--eo-gray-50);
  padding: var(--eo-spacing-lg, 1rem);
  border-radius: var(--eo-radius-lg, 0.75rem);
}

.profile-field {
  padding: var(--eo-spacing-md, 0.75rem);
  background: var(--eo-gray-50);
  border-radius: var(--eo-radius-md, 0.5rem);
  font-weight: 500;
  color: var(--eo-gray-800);
}

.table th {
  font-weight: 600;
  color: var(--eo-gray-700);
}

.table td {
  vertical-align: middle;
}

.btn-group {
  display: flex;
  gap: 4px;
}

@media (max-width: 992px) {
  .sidebar-card {
    position: static;
  }
  
  .stats-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 768px) {
  .user-welcome {
    flex-direction: column;
    text-align: center;
  }
  
  .welcome-title {
    font-size: var(--eo-font-size-2xl);
  }
  
  .stats-grid {
    grid-template-columns: 1fr;
  }
  
  .courses-grid,
  .certificates-grid {
    grid-template-columns: 1fr;
  }
  
  .activity-item {
    flex-direction: column;
    text-align: center;
  }
  
  .activity-progress {
    width: 100%;
    text-align: center;
  }
}
</style>
