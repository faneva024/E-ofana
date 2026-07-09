<template>
  <div class="dashboard-page">
    <div class="dashboard-hero card border-0 shadow-sm mb-4">
      <div class="card-body p-4 p-lg-5">
        <div class="d-flex flex-column flex-lg-row justify-content-between gap-3 align-items-lg-end">
          <div>
            <p class="eyebrow mb-2">Espace formateur</p>
            <h1 class="dashboard-title mb-2">Tableau de bord</h1>
            <p class="text-muted mb-0">
              Suivez vos visites, vos inscriptions et la performance de vos formations en temps réel.
            </p>
          </div>

          <div class="text-lg-end text-muted small">
            <div class="fw-semibold text-dark">Données du mois en cours</div>
            <div>{{ currentMonthLabel }}</div>
          </div>
        </div>
      </div>
    </div>

    <div v-if="errorMessage" class="alert alert-danger border-0 shadow-sm" role="alert">
      {{ errorMessage }}
    </div>

    <div class="row g-3 g-xl-4 mb-4">
      <div class="col-12 col-md-6 col-xl-3" v-for="card in summaryCards" :key="card.key">
        <CarteStatistique
          :label="card.label"
          :value="card.value"
          :hint="card.hint"
          :tone="card.tone"
          :loading="loading"
        >
          <template #icon>
            <span v-html="card.icon" aria-hidden="true"></span>
          </template>
        </CarteStatistique>
      </div>
    </div>

    <div class="row g-4 align-items-start">
      <div class="col-12 col-xl-8">
        <div class="card border-0 shadow-sm h-100">
          <div class="card-body p-3 p-lg-4">
            <div class="d-flex flex-column flex-md-row justify-content-between align-items-md-center gap-2 mb-3">
              <div>
                <h2 class="section-title mb-1">Mes formations</h2>
                <p class="text-muted mb-0">Cliquez sur une formation pour consulter ses statistiques détaillées.</p>
              </div>
              <span class="badge rounded-pill text-bg-light border align-self-start align-self-md-center">
                {{ formations.length }} formation{{ formations.length > 1 ? 's' : '' }}
              </span>
            </div>

            <div v-if="loading" class="py-5 text-center text-muted">
              <div class="spinner-border text-secondary mb-3" role="status" aria-hidden="true"></div>
              <div>Chargement du tableau de bord...</div>
            </div>

            <template v-else>
              <div v-if="!formations.length" class="empty-state py-5 text-center">
                <div class="empty-state-icon mb-3">0</div>
                <h3 class="h5 fw-semibold mb-2">Aucune formation disponible</h3>
                <p class="text-muted mb-0">Les statistiques apparaîtront dès qu’une formation sera remontée par l’API.</p>
              </div>

              <div v-else>
                <div class="table-responsive d-none d-lg-block">
                  <table class="table align-middle dashboard-table mb-0">
                    <thead>
                      <tr>
                        <th>Formation</th>
                        <th class="text-center">Visites</th>
                        <th class="text-center">Inscrits / Réservations</th>
                        <th class="text-center">Places restantes</th>
                        <th class="text-end">Revenus générés</th>
                      </tr>
                    </thead>
                    <tbody>
                      <tr
                        v-for="formation in formations"
                        :key="formation.id"
                        :class="{ active: selectedFormationId === formation.id }"
                        role="button"
                        tabindex="0"
                        @click="selectFormation(formation)"
                        @keydown.enter.prevent="selectFormation(formation)"
                      >
                        <td>
                          <div class="fw-semibold text-dark">{{ formation.title }}</div>
                          <div class="text-muted small">{{ formation.subtitle }}</div>
                        </td>
                        <td class="text-center fw-semibold">{{ formatNumber(formation.visites) }}</td>
                        <td class="text-center fw-semibold">{{ formatCountPair(formation.inscrits, formation.reservations) }}</td>
                        <td class="text-center">
                          <span class="badge rounded-pill" :class="placesRemainingClass(formation.placesRestantes)">
                            {{ formatNumber(formation.placesRestantes) }}
                          </span>
                        </td>
                        <td class="text-end fw-semibold">{{ formatCurrency(formation.revenusGeneres) }}</td>
                      </tr>
                    </tbody>
                  </table>
                </div>

                <div class="d-lg-none row g-3">
                  <div class="col-12" v-for="formation in formations" :key="formation.id">
                    <button
                      type="button"
                      class="formation-card-mobile text-start w-100"
                      :class="{ active: selectedFormationId === formation.id }"
                      @click="selectFormation(formation)"
                    >
                      <div class="d-flex justify-content-between gap-3 mb-2">
                        <div>
                          <div class="fw-semibold text-dark">{{ formation.title }}</div>
                          <div class="text-muted small">{{ formation.subtitle }}</div>
                        </div>
                        <span class="badge rounded-pill" :class="placesRemainingClass(formation.placesRestantes)">
                          {{ formatNumber(formation.placesRestantes) }} places
                        </span>
                      </div>

                      <div class="row g-2 small text-muted">
                        <div class="col-6">Visites<br><span class="fw-semibold text-dark">{{ formatNumber(formation.visites) }}</span></div>
                        <div class="col-6">Inscrits / Réservations<br><span class="fw-semibold text-dark">{{ formatCountPair(formation.inscrits, formation.reservations) }}</span></div>
                        <div class="col-12">Revenus générés<br><span class="fw-semibold text-dark">{{ formatCurrency(formation.revenusGeneres) }}</span></div>
                      </div>
                    </button>
                  </div>
                </div>
              </div>
            </template>
          </div>
        </div>
      </div>

      <div class="col-12 col-xl-4">
        <div class="card border-0 shadow-sm h-100 detail-panel">
          <div class="card-body p-3 p-lg-4">
            <div class="d-flex justify-content-between align-items-start gap-3 mb-3">
              <div>
                <h2 class="section-title mb-1">Détails statistiques</h2>
                <p class="text-muted mb-0">{{ selectedFormation?.title || 'Sélectionnez une formation' }}</p>
              </div>
              <span v-if="selectedFormation" class="badge rounded-pill text-bg-primary-subtle text-primary border border-primary-subtle">
                Sélectionnée
              </span>
            </div>

            <template v-if="selectedFormation">
              <div class="detail-hero mb-4">
                <div class="detail-hero-metric">{{ formatPercent(selectedFormation.tauxRemplissage) }}</div>
                <div class="text-muted small">Taux de remplissage</div>
              </div>

              <div class="row g-3 mb-4">
                <div class="col-6">
                  <div class="mini-metric">
                    <div class="mini-metric-label">Visites</div>
                    <div class="mini-metric-value">{{ formatNumber(selectedFormation.visites) }}</div>
                  </div>
                </div>
                <div class="col-6">
                  <div class="mini-metric">
                    <div class="mini-metric-label">Inscrits</div>
                    <div class="mini-metric-value">{{ formatNumber(selectedFormation.inscrits) }}</div>
                  </div>
                </div>
                <div class="col-6">
                  <div class="mini-metric">
                    <div class="mini-metric-label">Réservations</div>
                    <div class="mini-metric-value">{{ formatNumber(selectedFormation.reservations) }}</div>
                  </div>
                </div>
                <div class="col-6">
                  <div class="mini-metric">
                    <div class="mini-metric-label">Places restantes</div>
                    <div class="mini-metric-value">{{ formatNumber(selectedFormation.placesRestantes) }}</div>
                  </div>
                </div>
              </div>

              <div class="detail-list">
                <div class="detail-row">
                  <span>Revenus générés</span>
                  <strong>{{ formatCurrency(selectedFormation.revenusGeneres) }}</strong>
                </div>
                <div class="detail-row">
                  <span>Chiffre d'affaires net</span>
                  <strong>{{ formatCurrency(selectedFormation.chiffreAffairesNet) }}</strong>
                </div>
                <div class="detail-row">
                  <span>Taux d'inscription</span>
                  <strong>{{ formatPercent(selectedFormation.tauxInscription) }}</strong>
                </div>
                <div class="detail-row">
                  <span>Places totales</span>
                  <strong>{{ formatNumber(selectedFormation.totalPlaces) }}</strong>
                </div>
              </div>
            </template>

            <div v-else class="empty-state small py-5 text-center">
              <div class="empty-state-icon mb-3">i</div>
              <p class="mb-0 text-muted">Cliquez sur une formation à gauche pour afficher ses détails.</p>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import api from '../../api/axios'
import CarteStatistique from '../../composants/CarteStatistique.vue'

const loading = ref(false)
const errorMessage = ref('')
const dashboard = ref(null)
const formations = ref([])
const selectedFormationId = ref(null)

const currentMonthLabel = computed(() => {
  return new Intl.DateTimeFormat('fr-FR', { month: 'long', year: 'numeric' }).format(new Date())
})

const summary = computed(() => {
  const source = dashboard.value || {}
  return source.resume || source.resumeStatistiques || source.summary || source.stats || {}
})

const selectedFormation = computed(() => {
  if (!formations.value.length) return null
  return formations.value.find((item) => item.id === selectedFormationId.value) || formations.value[0]
})

const summaryCards = computed(() => ([
  {
    key: 'visites',
    label: 'Nombre total de visites',
    value: formatNumber(summary.value.totalVisites),
    hint: 'Toutes formations confondues',
    tone: 'light',
    icon: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M1 12s4-7 11-7 11 7 11 7-4 7-11 7S1 12 1 12Z"/><circle cx="12" cy="12" r="3"/></svg>'
  },
  {
    key: 'inscrits',
    label: 'Nombre total d’inscrits',
    value: formatNumber(summary.value.totalInscrits),
    hint: 'Inscriptions confirmées',
    tone: 'success',
    icon: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/><path d="M23 21v-2a4 4 0 0 0-3-3.87"/><path d="M16 3.13a4 4 0 0 1 0 7.75"/></svg>'
  },
  {
    key: 'taux',
    label: 'Taux d’inscription',
    value: formatPercent(summary.value.tauxInscription),
    hint: 'Moyenne du tableau de bord',
    tone: 'warning',
    icon: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M3 3v18h18"/><path d="m7 14 3-3 4 4 7-7"/></svg>'
  },
  {
    key: 'ca',
    label: 'Chiffre d’affaires net du mois',
    value: formatCurrency(summary.value.chiffreAffairesNetMois),
    hint: 'Après commission',
    tone: 'primary-subtle',
    icon: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M12 1v22"/><path d="M17 5H9.5a3.5 3.5 0 0 0 0 7h5a3.5 3.5 0 0 1 0 7H7"/></svg>'
  }
]))

onMounted(loadDashboard)

async function loadDashboard() {
  loading.value = true
  errorMessage.value = ''

  try {
    const { data } = await api.get('/v1/formateurs/tableau-de-bord')
    dashboard.value = data || {}

    const rawFormations = Array.isArray(data)
      ? data
      : data?.formations || data?.mesFormations || data?.items || data?.data || []

    formations.value = rawFormations.map(normalizeFormation).filter(Boolean)
    selectedFormationId.value = formations.value[0]?.id ?? null
  } catch (error) {
    errorMessage.value = error.response?.data?.message || 'Impossible de charger le tableau de bord formateur.'
    dashboard.value = null
    formations.value = []
  } finally {
    loading.value = false
  }
}

function normalizeFormation(item, index = 0) {
  if (!item) return null

  return {
    id: item.id ?? item.formationId ?? index,
    title: item.titre || item.title || item.nom || 'Formation sans titre',
    subtitle: item.categorie || item.category || item.description || '',
    visites: numberOrFallback(item.visites ?? item.totalVisites ?? item.nbVisites),
    inscrits: numberOrFallback(item.inscrits ?? item.nbInscrits ?? item.totalInscrits),
    reservations: numberOrFallback(item.reservations ?? item.nbReservations ?? item.totalReservations),
    placesRestantes: numberOrFallback(item.placesRestantes ?? item.placesDisponibles ?? item.placesRestantesFormations),
    revenusGeneres: numberOrFallback(item.revenusGeneres ?? item.chiffreAffaires ?? item.revenus ?? item.totalRevenus),
    totalPlaces: numberOrFallback(item.totalPlaces ?? item.nombrePlaces),
    tauxInscription: percentOrFallback(item.tauxInscription ?? item.tauxInscriptionFormation),
    tauxRemplissage: percentOrFallback(item.tauxRemplissage ?? item.tauxOccupation ?? item.tauxRemplissageFormation),
    chiffreAffairesNet: numberOrFallback(item.chiffreAffairesNet ?? item.net ?? item.netFormation)
  }
}

function numberOrFallback(value) {
  const parsed = Number(value)
  return Number.isFinite(parsed) ? parsed : 0
}

function percentOrFallback(value) {
  if (value === null || value === undefined || value === '') return null

  const parsed = Number(value)
  if (!Number.isFinite(parsed)) return null

  return parsed > 1 ? parsed / 100 : parsed
}

function formatNumber(value) {
  const parsed = Number(value)
  return Number.isFinite(parsed)
    ? new Intl.NumberFormat('fr-FR').format(parsed)
    : '0'
}

function formatCurrency(value) {
  const parsed = Number(value)
  return Number.isFinite(parsed)
    ? new Intl.NumberFormat('fr-FR', {
      style: 'currency',
      currency: 'MGA',
      maximumFractionDigits: 0
    }).format(parsed)
    : new Intl.NumberFormat('fr-FR', {
      style: 'currency',
      currency: 'MGA',
      maximumFractionDigits: 0
    }).format(0)
}

function formatPercent(value) {
  if (value === null || value === undefined || value === '') return '0%'

  const parsed = Number(value)
  if (!Number.isFinite(parsed)) return '0%'

  const normalized = parsed > 1 ? parsed / 100 : parsed
  return `${Math.round(normalized * 100)}%`
}

function formatCountPair(inscrits, reservations) {
  return `${formatNumber(inscrits)} / ${formatNumber(reservations)}`
}

function placesRemainingClass(value) {
  const remaining = Number(value)

  if (!Number.isFinite(remaining) || remaining <= 0) return 'text-bg-danger'
  if (remaining <= 3) return 'text-bg-warning'
  return 'text-bg-success'
}

function selectFormation(formation) {
  selectedFormationId.value = formation.id
}
</script>

<style scoped>
.dashboard-page {
  display: flex;
  flex-direction: column;
  gap: 1.25rem;
}

.dashboard-hero {
  border-radius: 1.5rem;
  overflow: hidden;
  background: linear-gradient(135deg, #ffffff 0%, #fbfbfb 100%);
}

.eyebrow {
  text-transform: uppercase;
  letter-spacing: 0.16em;
  font-size: 0.72rem;
  font-weight: 700;
  color: #8c6c2f;
}

.dashboard-title,
.section-title {
  font-weight: 800;
  letter-spacing: -0.03em;
}

.dashboard-title {
  font-size: clamp(1.8rem, 3vw, 2.5rem);
}

.section-title {
  font-size: 1.2rem;
}

.section-pill {
  background: rgba(198, 156, 80, 0.12);
  border: 1px solid rgba(198, 156, 80, 0.28);
  color: #8c6c2f;
}

.dashboard-table thead th {
  font-size: 0.78rem;
  text-transform: uppercase;
  letter-spacing: 0.08em;
  color: #6c757d;
  border-bottom-color: #e9ecef;
}

.dashboard-table tbody tr {
  transition: background-color 0.2s ease, transform 0.2s ease;
}

.dashboard-table tbody tr:hover,
.dashboard-table tbody tr.active {
  background: rgba(198, 156, 80, 0.08);
}

.dashboard-table tbody td {
  padding-top: 1rem;
  padding-bottom: 1rem;
  border-color: #eef0f2;
}

.formation-card-mobile {
  border: 1px solid #e9ecef;
  border-radius: 1rem;
  background: #fff;
  padding: 1rem;
  transition: all 0.2s ease;
}

.formation-card-mobile.active,
.formation-card-mobile:hover {
  border-color: rgba(198, 156, 80, 0.35);
  box-shadow: 0 10px 24px rgba(0, 0, 0, 0.06);
  transform: translateY(-1px);
}

.detail-panel {
  position: sticky;
  top: 1rem;
}

.detail-hero {
  padding: 1.25rem;
  border-radius: 1.25rem;
  background: linear-gradient(135deg, #1a1a1a 0%, #2a2a2a 100%);
  color: #fff;
}

.detail-hero-metric {
  font-size: clamp(2rem, 5vw, 3rem);
  font-weight: 800;
  line-height: 1;
  margin-bottom: 0.25rem;
}

.mini-metric {
  background: #f8f9fa;
  border-radius: 1rem;
  padding: 0.9rem;
  height: 100%;
}

.mini-metric-label {
  font-size: 0.78rem;
  color: #6c757d;
  margin-bottom: 0.3rem;
}

.mini-metric-value {
  font-weight: 800;
  color: #111827;
  font-size: 1.05rem;
}

.detail-list {
  display: grid;
  gap: 0.75rem;
}

.detail-row {
  display: flex;
  justify-content: space-between;
  gap: 1rem;
  padding: 0.85rem 0;
  border-bottom: 1px solid #eef0f2;
}

.places-badge {
  min-width: 2.5rem;
  padding: 0.45rem 0.8rem;
  font-weight: 700;
}

.text-bg-success,
.text-bg-warning,
.text-bg-danger {
  color: #1a1a1a !important;
}

.detail-row:last-child {
  border-bottom: 0;
  padding-bottom: 0;
}

.empty-state {
  background: #f8f9fa;
  border-radius: 1.25rem;
  border: 1px dashed #dfe3e8;
}

.empty-state-icon {
  width: 3rem;
  height: 3rem;
  border-radius: 999px;
  margin: 0 auto;
  display: grid;
  place-items: center;
  background: rgba(198, 156, 80, 0.12);
  color: #8c6c2f;
  font-weight: 800;
}

@media (max-width: 1199.98px) {
  .detail-panel {
    position: static;
  }
}
</style>
