<template>
  <div class="apprenant-espace">
    <div class="container-xl py-5">
      <div class="row g-4">

        <div class="col-lg-4">
          <div class="profile-sidebar-hero mb-4">
            <div class="avatar-main">{{ initials }}</div>
            <h1 class="h5 fw-bold mb-1">{{ displayName }}</h1>
            <p class="small text-white-50 mb-3">Membre depuis {{ memberSince }}</p>
            <div class="d-flex justify-content-center gap-2">
              <span class="badge bg-white bg-opacity-10 py-2 px-3 rounded text-white" style="font-size: 12px;">
                <strong>{{ enCours }}</strong> En cours
              </span>
              <span class="badge bg-white bg-opacity-10 py-2 px-3 rounded text-white" style="font-size: 12px;">
                <strong>{{ termine }}</strong> Terminée
              </span>
            </div>
          </div>

          <div class="card card-custom p-2">
            <div class="list-group list-group-flush">
              <a href="#formations" class="list-group-item list-group-item-action border-0 py-2.5 d-flex align-items-center gap-2 small fw-medium rounded text-dark">
                <span class="material-symbols-outlined text-muted" style="font-size: 20px;">auto_stories</span> Mes formations
              </a>
              <a href="#paiements" class="list-group-item list-group-item-action border-0 py-2.5 d-flex align-items-center gap-2 small fw-medium rounded text-dark">
                <span class="material-symbols-outlined text-muted" style="font-size: 20px;">payments</span> Factures & Reçus
              </a>
              <a href="#parametres" class="list-group-item list-group-item-action border-0 py-2.5 d-flex align-items-center gap-2 small fw-medium rounded text-dark">
                <span class="material-symbols-outlined text-muted" style="font-size: 20px;">manage_accounts</span> Informations personnelles
              </a>
            </div>
          </div>
        </div>

        <div class="col-lg-8">

          <section id="formations" class="mb-4">
            <div class="card card-custom p-4">
              <h2 class="h6 fw-bold mb-4 d-flex align-items-center gap-2">
                <span class="material-symbols-outlined text-primary-custom">auto_stories</span> Mes Formations
              </h2>

              <div v-if="inscriptions.length === 0" class="text-center py-4 text-muted">
                <span class="material-symbols-outlined" style="font-size: 3rem; color: #c59d5f;">auto_stories</span>
                <p class="mt-2 small">Aucune inscription pour le moment</p>
              </div>

              <div v-else class="d-flex flex-column gap-4">
                <div v-for="ins in inscriptions" :key="ins.id" class="p-3 border rounded">
                  <div class="row align-items-center g-3">
                    <div class="col-md-7">
                      <span v-if="ins.categorie" class="badge bg-light text-secondary mb-1" style="font-size: 10px;">{{ ins.categorie }}</span>
                      <h3 class="h6 fw-bold mb-1">{{ ins.formation }}</h3>
                      <p class="small text-muted mb-0">Par {{ ins.ecole }}</p>
                    </div>
                    <div class="col-md-5">
                      <div class="d-flex justify-content-between small text-muted mb-1" style="font-size: 11px;">
                        <span>Progression</span>
                        <span class="fw-bold text-dark">{{ ins.progression || 0 }}%</span>
                      </div>
                      <div class="progress progress-custom">
                        <div class="progress-bar progress-bar-custom" :style="{ width: (ins.progression || 0) + '%' }"></div>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </section>

          <section id="parametres" class="mb-4">
            <div class="card card-custom p-4">
              <h2 class="h6 fw-bold mb-4 d-flex align-items-center gap-2">
                <span class="material-symbols-outlined text-primary-custom">manage_accounts</span> Informations personnelles
              </h2>

              <div v-if="!editing">
                <div class="row g-3 mb-3">
                  <div class="col-md-6">
                    <label class="form-label-custom">Nom Complet</label>
                    <div class="fw-medium">{{ authStore.user?.prenom || '' }} {{ authStore.user?.nom || '' }}</div>
                  </div>
                  <div class="col-md-6">
                    <label class="form-label-custom">Adresse Email</label>
                    <div class="fw-medium">{{ authStore.user?.email || '—' }}</div>
                  </div>
                </div>
                <div class="row g-3 mb-3">
                  <div class="col-md-6">
                    <label class="form-label-custom">Téléphone</label>
                    <div class="fw-medium">{{ authStore.user?.telephone || 'Non renseigné' }}</div>
                  </div>
                  <div class="col-md-6">
                    <label class="form-label-custom">Ville</label>
                    <div class="fw-medium">{{ authStore.user?.ville || 'Non renseigné' }}</div>
                  </div>
                </div>
                <button class="btn btn-dark-custom d-flex align-items-center justify-content-center gap-2 py-2 px-4" @click="toggleEdit">
                  <span class="material-symbols-outlined" style="font-size: 18px;">edit</span>
                  Modifier mes informations
                </button>
              </div>

              <div v-else>
                <form @submit.prevent="saveProfile">
                  <div class="row g-3 mb-3">
                    <div class="col-md-6">
                      <label class="form-label-custom" for="p-prenom">Prénom</label>
                      <input v-model="editForm.prenom" class="form-control form-control-sm py-2" id="p-prenom" type="text" />
                    </div>
                    <div class="col-md-6">
                      <label class="form-label-custom" for="p-nom">Nom</label>
                      <input v-model="editForm.nom" class="form-control form-control-sm py-2" id="p-nom" type="text" />
                    </div>
                  </div>

                  <div class="row g-3 mb-3">
                    <div class="col-md-6">
                      <label class="form-label-custom" for="p-email">Adresse Email</label>
                      <input :value="authStore.user?.email" class="form-control form-control-sm py-2" id="p-email" type="email" disabled />
                      <small class="text-muted" style="font-size: 11px;">L'email ne peut pas être modifié</small>
                    </div>
                    <div class="col-md-6">
                      <label class="form-label-custom" for="p-tel">Téléphone</label>
                      <input v-model="editForm.telephone" class="form-control form-control-sm py-2" id="p-tel" type="text" placeholder="034 00 000 00" />
                    </div>
                  </div>

                  <div class="row g-3 mb-3">
                    <div class="col-md-6">
                      <label class="form-label-custom" for="p-password">Nouveau mot de passe</label>
                      <input v-model="editForm.password" type="password" class="form-control form-control-sm py-2" id="p-password" placeholder="Laisser vide pour conserver" />
                    </div>
                    <div class="col-md-6">
                      <label class="form-label-custom" for="p-confirm">Confirmer le mot de passe</label>
                      <input v-model="editForm.confirmPassword" type="password" class="form-control form-control-sm py-2" id="p-confirm" placeholder="Confirmer" />
                    </div>
                  </div>

                  <div v-if="apiError" class="alert alert-danger py-2 small mb-3">{{ apiError }}</div>

                  <div class="d-flex gap-2">
                    <button class="btn btn-dark-custom d-flex align-items-center justify-content-center gap-2 py-2 px-4" type="submit" :disabled="submitting">
                      <span class="material-symbols-outlined" style="font-size: 18px;">check_circle</span>
                      <span v-if="submitting">Enregistrement...</span>
                      <span v-else>Enregistrer mes informations</span>
                    </button>
                    <button class="btn btn-outline-secondary py-2 px-4" type="button" @click="toggleEdit">
                      Annuler
                    </button>
                  </div>
                </form>
              </div>
            </div>
          </section>

          <section id="paiements" class="mb-4">
            <div class="card card-custom p-4">
              <h2 class="h6 fw-bold mb-4 d-flex align-items-center gap-2">
                <span class="material-symbols-outlined text-primary-custom">payments</span> Factures & Reçus
              </h2>

              <div v-if="filteredRecus.length === 0" class="text-center py-4 text-muted">
                <span class="material-symbols-outlined" style="font-size: 3rem; color: #c59d5f;">receipt_long</span>
                <p class="mt-2 small">Aucun reçu trouvé</p>
              </div>

              <div v-else class="table-responsive">
                <table class="table table-sm align-middle mb-0" style="font-size: 0.9rem;">
                  <thead>
                    <tr class="text-muted" style="font-size: 0.75rem; text-transform: uppercase;">
                      <th>Formation</th>
                      <th>Date</th>
                      <th>Montant</th>
                      <th>Statut</th>
                      <th class="text-end">Action</th>
                    </tr>
                  </thead>
                  <tbody>
                    <tr v-for="recu in filteredRecus" :key="recu.id">
                      <td class="fw-medium">{{ recu.formation }}</td>
                      <td class="text-muted">{{ formatDate(recu.date) }}</td>
                      <td class="fw-bold">{{ formatPrice(recu.montant) }} Ar</td>
                      <td><span class="status-badge" :class="recu.statut === 'paye' ? 'status-paid' : 'status-pending'">{{ recu.statut === 'paye' ? 'Payé' : 'En attente' }}</span></td>
                      <td class="text-end">
                        <button class="btn btn-sm btn-outline-secondary py-1 px-2 d-inline-flex align-items-center gap-1" style="font-size: 11px;" :class="{ disabled: recu.statut !== 'paye' }" @click="downloadRecu(recu)">
                          <span class="material-symbols-outlined" style="font-size: 14px;">download</span> Reçu
                        </button>
                      </td>
                    </tr>
                  </tbody>
                </table>
              </div>
            </div>
          </section>

        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useAuthStore } from '../../stores/authStore'

const authStore = useAuthStore()
const editing = ref(false)
const submitting = ref(false)
const apiError = ref('')

const editForm = ref({
  prenom: authStore.user?.prenom || '',
  nom: authStore.user?.nom || '',
  telephone: authStore.user?.telephone || '',
  password: '',
  confirmPassword: ''
})

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

const memberSince = computed(() => {
  if (authStore.user?.date_creation) {
    return new Date(authStore.user.date_creation).toLocaleDateString('fr-FR', { month: 'long', year: 'numeric' })
  }
  return 'Juin 2026'
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

const enCours = computed(() => inscriptions.value.filter(i => i.statut !== 'termine').length)
const termine = computed(() => inscriptions.value.filter(i => i.statut === 'termine').length)

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

const filteredRecus = computed(() => recus.value)

function formatPrice(value) {
  return new Intl.NumberFormat('fr-FR').format(value)
}

function formatDate(date) {
  return new Date(date + 'T00:00:00').toLocaleDateString('fr-FR', {
    day: 'numeric', month: 'long', year: 'numeric'
  })
}

function downloadRecu(item) {
  console.log('Téléchargement du reçu:', item.reference || item.id)
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
  }
  editing.value = !editing.value
}

function saveProfile() {
  apiError.value = ''

  if (!editForm.value.prenom.trim() || !editForm.value.nom.trim()) {
    apiError.value = 'Le prénom et le nom sont requis.'
    return
  }

  if (editForm.value.password || editForm.value.confirmPassword) {
    if (editForm.value.password.length < 6) {
      apiError.value = 'Le mot de passe doit contenir au moins 6 caractères.'
      return
    }
    if (editForm.value.password !== editForm.value.confirmPassword) {
      apiError.value = 'Les mots de passe ne correspondent pas.'
      return
    }
  }

  submitting.value = true
  setTimeout(() => {
    if (authStore.user) {
      authStore.user.prenom = editForm.value.prenom
      authStore.user.nom = editForm.value.nom
      authStore.user.telephone = editForm.value.telephone
    }
    submitting.value = false
    editing.value = false
    editForm.value.password = ''
    editForm.value.confirmPassword = ''
  }, 600)
}
</script>

<style scoped>
.apprenant-espace {
  font-family: 'Hanken Grotesk', sans-serif;
  background-color: #fbf9f8;
  color: #1a1a1a;
  min-height: 100vh;
}

.card-custom {
  background: #ffffff;
  border-radius: 8px;
  border: 1px solid rgba(0, 0, 0, 0.05);
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.02);
}

.profile-sidebar-hero {
  background: linear-gradient(135deg, #121212, #262626);
  color: white;
  border-radius: 8px;
  padding: 2rem 1.5rem;
  text-align: center;
}

.avatar-main {
  width: 90px;
  height: 90px;
  background-color: #c59d5f;
  color: #121212;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 800;
  font-size: 2rem;
  margin: 0 auto 1rem auto;
  border: 4px solid rgba(255, 255, 255, 0.1);
}

.form-label-custom {
  font-size: 0.8rem;
  text-transform: uppercase;
  letter-spacing: 0.05em;
  color: #64748b;
  font-weight: 700;
  margin-bottom: 0.5rem;
}

.form-control:focus,
.form-select:focus {
  border-color: #c59d5f;
  box-shadow: 0 0 0 0.18rem rgba(197, 157, 95, 0.15);
}

.progress-custom {
  height: 6px;
  border-radius: 10px;
  background-color: #f1f5f9;
}
.progress-bar-custom {
  background-color: #c59d5f;
  border-radius: 10px;
}

.status-badge {
  font-size: 0.75rem;
  font-weight: 600;
  padding: 0.25rem 0.6rem;
  border-radius: 4px;
  display: inline-block;
}
.status-paid { background-color: #ecfdf5; color: #10b981; }
.status-pending { background-color: #fffbe6; color: #f59e0b; }

.btn-dark-custom {
  background-color: #1a1a1a;
  color: white;
  border: none;
  font-weight: 600;
  border-radius: 8px;
  transition: background-color 0.2s;
}
.btn-dark-custom:hover {
  background-color: #2d3748;
  color: white;
}

.btn-outline-secondary {
  border-radius: 8px;
}

.border {
  border-color: rgba(0, 0, 0, 0.05) !important;
}

.badge.bg-light {
  background-color: #f1f5f9 !important;
  color: #64748b !important;
  font-weight: 600;
}

.table th {
  font-weight: 600;
  letter-spacing: 0.05em;
  border-bottom: 1px solid rgba(0, 0, 0, 0.05);
}

.table td {
  border-bottom: 1px solid rgba(0, 0, 0, 0.05);
  vertical-align: middle;
}

.list-group-item {
  padding: 0.6rem 0.75rem;
}

.list-group-item:hover {
  background-color: #f8f5f0;
}

@media (max-width: 575.98px) {
  .profile-sidebar-hero {
    padding: 1.5rem 1rem;
  }
  .avatar-main {
    width: 70px;
    height: 70px;
    font-size: 1.5rem;
  }
}
</style>
