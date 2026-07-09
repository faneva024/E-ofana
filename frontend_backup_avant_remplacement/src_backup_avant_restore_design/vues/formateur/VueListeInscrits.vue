<template>
  <div class="liste-inscrits-page">
        <div class="d-flex justify-content-between align-items-center mb-4">
          <h1 class="page-title mb-0">Liste des inscrits</h1>
          <button class="btn btn-export" @click="exportPDF" :disabled="exporting">
            <span v-if="exporting" class="spinner-border spinner-border-sm me-2"></span>
            <i v-else class="bi bi-download me-2"></i>
            Exporter CSV
          </button>
        </div>

        <!-- Filtres -->
        <div class="filters-bar mb-4">
          <div class="row g-3 align-items-end">
            <div class="col-md-4">
              <select class="form-select" v-model="selectedFormation">
                <option value="">Toutes les formations</option>
                <option v-for="f in formations" :key="f.id" :value="f.id">
                  {{ f.nom }}
                </option>
              </select>
            </div>
            <div class="col-md-4">
              <div class="search-input-wrapper">
                <i class="bi bi-search search-icon"></i>
                <input
                  type="text"
                  class="form-control search-input"
                  v-model="searchQuery"
                  placeholder="Rechercher par nom..."
                />
              </div>
            </div>
          </div>
        </div>

        <!-- Loading -->
        <div v-if="loading" class="text-center py-5">
          <div class="spinner-border" role="status" style="color: var(--eo-secondary);">
            <span class="visually-hidden">Chargement...</span>
          </div>
        </div>

        <!-- Table -->
        <div v-else class="card-section">
          <div v-if="filteredInscrits.length === 0" class="no-results">
            <div class="no-results-icon">
              <i class="bi bi-people"></i>
            </div>
            <h4>Aucun inscrit trouvé</h4>
            <p>Aucun inscrit ne correspond à vos critères de recherche.</p>
          </div>

          <div v-else>
            <div class="table-responsive">
              <table class="table table-inscrits">
                <thead>
                  <tr>
                    <th>NOM</th>
                    <th>EMAIL</th>
                    <th>DATE D'INSCRIPTION</th>
                    <th>STATUT PAIEMENT</th>
                    <th>ACTIONS</th>
                  </tr>
                </thead>
                <tbody>
                  <tr v-for="inscrit in paginatedInscrits" :key="inscrit.id">
                    <td class="inscrit-nom">{{ inscrit.nom }} {{ inscrit.prenom }}</td>
                    <td class="inscrit-email">{{ inscrit.email }}</td>
                    <td>{{ formatDate(inscrit.dateInscription) }}</td>
                    <td>
                      <span class="status-badge" :class="getStatutClass(inscrit.statutPaiement)">
                        {{ getStatutLabel(inscrit.statutPaiement) }}
                      </span>
                    </td>
                    <td>
                      <button class="btn btn-sm btn-recu" @click="downloadRecu(inscrit)">
                        <i class="bi bi-download me-1"></i>
                        Reçu
                      </button>
                    </td>
                  </tr>
                </tbody>
              </table>
            </div>

            <!-- Pagination -->
            <div v-if="totalPages > 1" class="pagination-bar">
              <span class="pagination-info">
                {{ (currentPage - 1) * perPage + 1 }}–{{ Math.min(currentPage * perPage, filteredInscrits.length) }} sur {{ filteredInscrits.length }}
              </span>
              <div class="pagination-controls">
                <button class="btn btn-sm btn-page" :disabled="currentPage === 1" @click="currentPage--">
                  <i class="bi bi-chevron-left"></i>
                </button>
                <button
                  v-for="page in visiblePages"
                  :key="page"
                  class="btn btn-sm btn-page"
                  :class="{ active: page === currentPage }"
                  @click="currentPage = page"
                >
                  {{ page }}
                </button>
                <button class="btn btn-sm btn-page" :disabled="currentPage === totalPages" @click="currentPage++">
                  <i class="bi bi-chevron-right"></i>
                </button>
              </div>
            </div>
          </div>
        </div>
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthFormateurStore } from '../../stores/authFormateurStore'
import axios from 'axios'

const router = useRouter()
const authFormateurStore = useAuthFormateurStore()

const loading = ref(false)
const exporting = ref(false)
const searchQuery = ref('')
const selectedFormation = ref('')
const currentPage = ref(1)
const perPage = 10

const formations = ref([
  { id: 1, nom: 'Développement Web Full Stack' },
  { id: 2, nom: 'Marketing Digital' },
  { id: 3, nom: 'Gestion de Projet' }
])

const inscrits = ref([
  { id: 1, nom: 'RAKOTO', prenom: 'Jean', email: 'jrakoto@email.mg', dateInscription: '2026-06-10', statutPaiement: 'paye', formationId: 1 },
  { id: 2, nom: 'RABE', prenom: 'Marie', email: 'mrabe@email.mg', dateInscription: '2026-06-11', statutPaiement: 'paye', formationId: 1 },
  { id: 3, nom: 'RAJAO', prenom: 'Paul', email: 'prajao@email.mg', dateInscription: '2026-06-12', statutPaiement: 'en_attente', formationId: 2 },
  { id: 4, nom: 'ANDRIA', prenom: 'Soa', email: 'sandria@email.mg', dateInscription: '2026-06-13', statutPaiement: 'paye', formationId: 1 },
  { id: 5, nom: 'RAMPY', prenom: 'Luc', email: 'lrampy@email.mg', dateInscription: '2026-06-14', statutPaiement: 'annule', formationId: 3 }
])

watch([searchQuery, selectedFormation], () => {
  currentPage.value = 1
})

const filteredInscrits = computed(() => {
  return inscrits.value.filter(i => {
    if (selectedFormation.value && i.formationId !== selectedFormation.value) return false
    if (searchQuery.value) {
      const q = searchQuery.value.toLowerCase()
      const fullName = (i.nom + ' ' + i.prenom).toLowerCase()
      if (!fullName.includes(q) && !i.email.toLowerCase().includes(q)) return false
    }
    return true
  })
})

const totalPages = computed(() => Math.ceil(filteredInscrits.value.length / perPage))

const paginatedInscrits = computed(() => {
  const start = (currentPage.value - 1) * perPage
  return filteredInscrits.value.slice(start, start + perPage)
})

const visiblePages = computed(() => {
  const pages = []
  const start = Math.max(1, currentPage.value - 2)
  const end = Math.min(totalPages.value, start + 4)
  for (let i = start; i <= end; i++) pages.push(i)
  return pages
})

function getStatutClass(statut) {
  switch (statut) {
    case 'paye': return 'status-paye'
    case 'en_attente': return 'status-attente'
    case 'annule': return 'status-annule'
    default: return ''
  }
}

function getStatutLabel(statut) {
  switch (statut) {
    case 'paye': return 'Payé'
    case 'en_attente': return 'En attente'
    case 'annule': return 'Annulé'
    default: return statut
  }
}

function formatDate(dateStr) {
  if (!dateStr) return '—'
  return new Date(dateStr + 'T00:00:00').toLocaleDateString('fr-FR', {
    day: 'numeric',
    month: 'long',
    year: 'numeric'
  })
}

function downloadRecu(inscrit) {
  console.log('Télécharger reçu pour:', inscrit.nom, inscrit.prenom)
  // TODO: API call GET /api/v1/formations/{id}/inscrits/{inscritId}/recu
}

async function exportPDF() {
  exporting.value = true
  try {
    // TODO: API call
    // const formationId = selectedFormation.value || 'all'
    // const response = await axios.get(`/api/v1/formations/${formationId}/inscrits/export-pdf`, { responseType: 'blob' })
    // const url = window.URL.createObjectURL(new Blob([response.data]))
    // const link = document.createElement('a')
    // link.href = url
    // link.setAttribute('download', 'liste-inscrits.pdf')
    // document.body.appendChild(link)
    // link.click()
    // link.remove()
    await new Promise(resolve => setTimeout(resolve, 1000))
    console.log('Export PDF déclenché')
  } catch (error) {
    console.error('Erreur export:', error)
  } finally {
    exporting.value = false
  }
}

function handleLogout() {
  authFormateurStore.logout()
  router.push({ name: 'Connexion' })
}

async function loadInscrits() {
  loading.value = true
  try {
    // TODO: API call
    // const id = selectedFormation.value || ''
    // const response = await axios.get(`/api/v1/formations/${id}/inscrits`)
    // inscrits.value = response.data
    await new Promise(resolve => setTimeout(resolve, 400))
  } catch (error) {
    console.error('Erreur lors du chargement:', error)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadInscrits()
})
</script>

<style scoped>
.liste-inscrits-page {
  font-family: var(--eo-font-family);
}

.page-title {
  font-size: var(--eo-font-size-3xl);
  font-weight: 700;
  color: var(--eo-gray-900);
}

/* ===== FILTERS ===== */
.filters-bar .form-select {
  border: 1px solid var(--eo-gray-200);
  border-radius: var(--eo-radius-md);
  padding: var(--eo-spacing-sm) var(--eo-spacing-md);
  color: var(--eo-gray-700);
}

.filters-bar .form-select:focus {
  border-color: var(--eo-secondary);
  box-shadow: 0 0 0 0.2rem rgba(198, 156, 80, 0.2);
}

.search-input-wrapper {
  position: relative;
}

.search-icon {
  position: absolute;
  left: 12px;
  top: 50%;
  transform: translateY(-50%);
  color: var(--eo-gray-400);
}

.search-input {
  padding-left: 36px;
  border: 1px solid var(--eo-gray-200);
  border-radius: var(--eo-radius-md);
}

.search-input:focus {
  border-color: var(--eo-secondary);
  box-shadow: 0 0 0 0.2rem rgba(198, 156, 80, 0.2);
}

/* ===== EXPORT BUTTON ===== */
.btn-export {
  background: var(--eo-primary);
  color: white;
  font-weight: 600;
  font-size: var(--eo-font-size-sm);
  border: none;
  border-radius: var(--eo-radius-lg);
  padding: var(--eo-spacing-sm) var(--eo-spacing-xl);
  transition: all var(--eo-transition-base);
}

.btn-export:hover:not(:disabled) {
  background: var(--eo-gray-800);
  color: white;
  transform: translateY(-1px);
  box-shadow: var(--eo-shadow-md);
}

.btn-export:disabled {
  opacity: 0.7;
  color: white;
}

/* ===== TABLE CARD ===== */
.card-section {
  background: white;
  border-radius: var(--eo-radius-xl);
  box-shadow: var(--eo-shadow-sm);
  overflow: hidden;
}

.table-inscrits {
  margin: 0;
}

.table-inscrits thead th {
  background: transparent;
  font-size: 0.7rem;
  font-weight: 700;
  color: var(--eo-gray-500);
  text-transform: uppercase;
  letter-spacing: 0.05em;
  padding: var(--eo-spacing-lg) var(--eo-spacing-xl);
  border-bottom: 1px solid var(--eo-gray-200);
  white-space: nowrap;
}

.table-inscrits tbody td {
  padding: var(--eo-spacing-lg) var(--eo-spacing-xl);
  vertical-align: middle;
  border-bottom: 1px solid var(--eo-gray-100);
  font-size: var(--eo-font-size-sm);
  color: var(--eo-gray-700);
}

.table-inscrits tbody tr:last-child td {
  border-bottom: none;
}

.table-inscrits tbody tr:hover {
  background: var(--eo-gray-50);
}

.inscrit-nom {
  font-weight: 700;
  color: var(--eo-gray-900) !important;
}

.inscrit-email {
  color: var(--eo-gray-500) !important;
}

/* ===== STATUS BADGES ===== */
.status-badge {
  font-size: var(--eo-font-size-sm);
  font-weight: 600;
  padding: 2px 0;
}

.status-paye {
  color: var(--eo-success);
}

.status-attente {
  color: var(--eo-secondary);
}

.status-annule {
  color: var(--eo-danger);
}

/* ===== RECU BUTTON ===== */
.btn-recu {
  color: var(--eo-gray-600);
  font-size: var(--eo-font-size-sm);
  font-weight: 500;
  border: none;
  background: none;
  padding: var(--eo-spacing-xs) var(--eo-spacing-sm);
  transition: color var(--eo-transition-base);
}

.btn-recu:hover {
  color: var(--eo-secondary);
}

/* ===== PAGINATION ===== */
.pagination-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: var(--eo-spacing-md) var(--eo-spacing-xl);
  border-top: 1px solid var(--eo-gray-100);
}

.pagination-info {
  font-size: var(--eo-font-size-sm);
  color: var(--eo-gray-500);
}

.pagination-controls {
  display: flex;
  gap: 4px;
}

.btn-page {
  min-width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 1px solid var(--eo-gray-200);
  border-radius: var(--eo-radius-md);
  color: var(--eo-gray-600);
  font-size: var(--eo-font-size-sm);
  background: white;
  transition: all var(--eo-transition-base);
}

.btn-page:hover:not(:disabled) {
  border-color: var(--eo-secondary);
  color: var(--eo-secondary);
}

.btn-page.active {
  background: var(--eo-secondary);
  border-color: var(--eo-secondary);
  color: white;
}

.btn-page:disabled {
  opacity: 0.4;
  cursor: not-allowed;
}

/* ===== NO RESULTS ===== */
.no-results {
  text-align: center;
  padding: var(--eo-spacing-3xl) var(--eo-spacing-xl);
}

.no-results-icon {
  font-size: 4rem;
  color: var(--eo-gray-300);
  margin-bottom: var(--eo-spacing-lg);
}

.no-results h4 {
  color: var(--eo-gray-700);
  margin-bottom: var(--eo-spacing-sm);
}

.no-results p {
  color: var(--eo-gray-500);
}

/* ===== RESPONSIVE ===== */
@media (max-width: 768px) {
  .d-flex.justify-content-between {
    flex-direction: column;
    gap: var(--eo-spacing-md);
  }

  .btn-export {
    width: 100%;
  }

  .table-inscrits thead th,
  .table-inscrits tbody td {
    padding: var(--eo-spacing-md);
  }

  .pagination-bar {
    flex-direction: column;
    gap: var(--eo-spacing-md);
  }
}
</style>
