<template>
  <div class="container py-4">
    <h1 class="fw-bold mb-1">Finances</h1>
    <p class="text-muted mb-4">Suivez vos revenus et l'historique des virements.</p>

    <!-- Résumé -->
    <div class="row g-3 mb-4">
      <div class="col-12 col-sm-6 col-xl-3">
        <div class="card border-0 shadow-sm h-100">
          <div class="card-body">
            <div class="d-flex align-items-center gap-3">
              <div class="rounded-3 p-3" style="background: rgba(198, 156, 80, 0.1);">
                <svg fill="none" height="24" stroke="#c69c50" stroke-linecap="round" stroke-linejoin="round" stroke-width="2" width="24" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
                  <line x1="12" y1="1" x2="12" y2="23"></line><path d="M17 5H9.5a3.5 3.5 0 0 0 0 7h5a3.5 3.5 0 0 1 0 7H6"></path>
                </svg>
              </div>
              <div>
                <p class="text-muted small mb-0">Revenus bruts</p>
                <h4 class="fw-bold mb-0">{{ formatAriary(summary.revenusBruts) }}</h4>
              </div>
            </div>
          </div>
        </div>
      </div>

      <div class="col-12 col-sm-6 col-xl-3">
        <div class="card border-0 shadow-sm h-100">
          <div class="card-body">
            <div class="d-flex align-items-center gap-3">
              <div class="rounded-3 p-3" style="background: rgba(220, 53, 69, 0.1);">
                <svg fill="none" height="24" stroke="#dc3545" stroke-linecap="round" stroke-linejoin="round" stroke-width="2" width="24" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
                  <line x1="12" y1="1" x2="12" y2="23"></line><path d="M17 5H9.5a3.5 3.5 0 0 0 0 7h5a3.5 3.5 0 0 1 0 7H6"></path>
                </svg>
              </div>
              <div>
                <p class="text-muted small mb-0">Commissions prélevées</p>
                <h4 class="fw-bold mb-0">{{ formatAriary(summary.commissions) }}</h4>
              </div>
            </div>
          </div>
        </div>
      </div>

      <div class="col-12 col-sm-6 col-xl-3">
        <div class="card border-0 shadow-sm h-100">
          <div class="card-body">
            <div class="d-flex align-items-center gap-3">
              <div class="rounded-3 p-3" style="background: rgba(25, 135, 84, 0.1);">
                <svg fill="none" height="24" stroke="#198754" stroke-linecap="round" stroke-linejoin="round" stroke-width="2" width="24" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
                  <polyline points="22 12 18 12 15 21 9 3 6 12 2 12"></polyline>
                </svg>
              </div>
              <div>
                <p class="text-muted small mb-0">Revenus nets</p>
                <h4 class="fw-bold mb-0">{{ formatAriary(summary.revenusNets) }}</h4>
              </div>
            </div>
          </div>
        </div>
      </div>

      <div class="col-12 col-sm-6 col-xl-3">
        <div class="card border-0 shadow-sm h-100">
          <div class="card-body">
            <div class="d-flex align-items-center gap-3">
              <div class="rounded-3 p-3" style="background: rgba(13, 110, 253, 0.1);">
                <svg fill="none" height="24" stroke="#0d6efd" stroke-linecap="round" stroke-linejoin="round" stroke-width="2" width="24" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
                  <rect x="3" y="4" width="18" height="18" rx="2" ry="2"></rect><line x1="16" y1="2" x2="16" y2="6"></line><line x1="8" y1="2" x2="8" y2="6"></line><line x1="3" y1="10" x2="21" y2="10"></line>
                </svg>
              </div>
              <div>
                <p class="text-muted small mb-0">Prochain virement</p>
                <h4 class="fw-bold mb-0">{{ formatAriary(summary.prochainVirementMontant) }}</h4>
                <small class="text-muted">{{ summary.prochainVirementDate }}</small>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Détail par formation -->
    <div class="card border-0 shadow-sm mb-4">
      <div class="card-header bg-transparent border-bottom py-3">
        <h5 class="fw-bold mb-0">Détail par formation</h5>
      </div>
      <div class="card-body p-0">
        <div v-if="loadingFormations" class="text-center py-5">
          <div class="spinner-border text-secondary" role="status">
            <span class="visually-hidden">Chargement...</span>
          </div>
        </div>

        <div v-else-if="formations.length === 0" class="text-center py-5 text-muted">
          Aucune formation avec inscriptions.
        </div>

        <div v-else class="table-responsive">
          <table class="table table-hover align-middle mb-0">
            <thead class="table-light">
              <tr>
                <th class="ps-4">Formation</th>
                <th>Participants</th>
                <th>Recette brute</th>
                <th>Commission E-Ofana</th>
                <th class="pe-4">Reversement net</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="f in formations" :key="f.id">
                <td class="ps-4 fw-semibold">{{ f.titre }}</td>
                <td>{{ f.participants }}</td>
                <td class="fw-semibold">{{ formatAriary(f.recetteBrute) }}</td>
                <td class="text-secondary">{{ formatAriary(f.commission) }}</td>
                <td class="pe-4 fw-semibold text-success">{{ formatAriary(f.reversementNet) }}</td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>

    <!-- Historique des virements -->
    <div class="card border-0 shadow-sm">
      <div class="card-header bg-transparent border-bottom py-3">
        <h5 class="fw-bold mb-0">Historique des virements</h5>
      </div>
      <div class="card-body p-0">
        <div v-if="loading" class="text-center py-5">
          <div class="spinner-border text-secondary" role="status">
            <span class="visually-hidden">Chargement...</span>
          </div>
        </div>

        <div v-else-if="virements.length === 0" class="text-center py-5 text-muted">
          Aucun virement pour le moment.
        </div>

        <div v-else class="table-responsive">
          <table class="table table-hover align-middle mb-0">
            <thead class="table-light">
              <tr>
                <th class="ps-4">Date</th>
                <th>Montant</th>
                <th>Opérateur Mobile Money</th>
                <th>Statut</th>
                <th class="pe-4 text-end">Justificatif</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="virement in virements" :key="virement.id">
                <td class="ps-4">{{ virement.date }}</td>
                <td class="fw-semibold">{{ formatAriary(virement.montant) }}</td>
                <td>{{ virement.operateur }}</td>
                <td>
                  <span class="badge" :class="statutClass(virement.statut)">{{ virement.statut }}</span>
                </td>
                <td class="pe-4 text-end">
                  <button
                    class="btn btn-sm btn-outline-primary"
                    @click="telechargerJustificatif(virement)"
                    :disabled="!virement.justificatif"
                  >
                    <svg fill="none" height="14" stroke="currentColor" stroke-linecap="round" stroke-linejoin="round" stroke-width="2" width="14" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg" class="me-1">
                      <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"></path><polyline points="7 10 12 15 17 10"></polyline><line x1="12" y1="15" x2="12" y2="3"></line>
                    </svg>
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
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import api from '../../api/axios'

const loading = ref(true)
const loadingFormations = ref(true)

const summary = reactive({
  revenusBruts: 0,
  commissions: 0,
  revenusNets: 0,
  prochainVirementMontant: 0,
  prochainVirementDate: ''
})

const virements = ref([])
const formations = ref([])

const formatAriary = (value) => {
  if (value == null || isNaN(value)) return '0 Ar'
  return Number(value).toLocaleString('fr-MG') + ' Ar'
}

const statutClass = (statut) => {
  switch ((statut || '').toLowerCase()) {
    case 'effectué':
    case 'effectue':
      return 'bg-success'
    case 'en attente':
      return 'bg-warning text-dark'
    case 'échec':
    case 'echec':
      return 'bg-danger'
    default:
      return 'bg-secondary'
  }
}

const telechargerJustificatif = (virement) => {
  if (!virement.justificatif) return
  const link = document.createElement('a')
  link.href = virement.justificatif
  link.download = `justificatif-virement-${virement.id}.pdf`
  link.target = '_blank'
  link.click()
}

onMounted(async () => {
  try {
    const [resFinances, resVirements, resFormations] = await Promise.all([
      api.get('/v1/formateurs/finances'),
      api.get('/v1/formateurs/finances/virements'),
      api.get('/v1/formateurs/finances/formations')
    ])

    const data = resFinances.data
    summary.revenusBruts = data.revenusBruts ?? 0
    summary.commissions = data.commissions ?? 0
    summary.revenusNets = data.revenusNets ?? 0
    summary.prochainVirementMontant = data.prochainVirementMontant ?? 0
    summary.prochainVirementDate = data.prochainVirementDate ?? ''

    const list = resVirements.data
    virements.value = (Array.isArray(list) ? list : []).sort((a, b) => {
      return new Date(b.date) - new Date(a.date)
    })

    formations.value = Array.isArray(resFormations.data) ? resFormations.data : []
  } catch (err) {
    console.error('Erreur chargement finances', err)
  } finally {
    loading.value = false
    loadingFormations.value = false
  }
})
</script>
