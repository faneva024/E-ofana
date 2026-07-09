<template>
  <div class="card h-100 formation-card shadow-sm">
    <!-- En-tête de la carte avec le statut -->
    <div class="card-header-status d-flex justify-content-between align-items-center p-3">
      <span class="status-badge" :class="statusClass">
        <span class="material-symbols-outlined status-icon">{{ statusIcon }}</span>
        <span>{{ formation.statut }}</span>
      </span>
      <span class="date-text d-flex align-items-center gap-1">
        <span class="material-symbols-outlined fs-6 text-muted">calendar_today</span>
        {{ formation.dateDebut }}
      </span>
    </div>

    <!-- Corps de la carte -->
    <div class="card-body d-flex flex-column p-3 pt-0">
      <h5 class="card-title fw-bold text-dark mb-2 text-truncate-2" :title="formation.titre">
        {{ formation.titre }}
      </h5>
      
      <!-- Jauge de remplissage des places -->
      <div class="mb-4 mt-2">
        <div class="d-flex justify-content-between text-muted small mb-1">
          <span>Places occupées</span>
          <span class="fw-semibold text-dark">{{ formation.placesOccupees }}/{{ formation.placesTotales }}</span>
        </div>
        <div class="progress custom-progress" style="height: 6px;">
          <div 
            class="progress-bar" 
            role="progressbar" 
            :style="{ width: percentFull + '%' }"
            :class="progressBarClass"
            :aria-valuenow="formation.placesOccupees" 
            :aria-valuemin="0" 
            :aria-valuemax="formation.placesTotales"
          ></div>
        </div>
      </div>

      <!-- Actions en bas poussées au maximum vers le bas -->
      <div class="mt-auto d-flex gap-2">
        <!-- Bouton Modifier -->
        <router-link 
          :to="{ name: 'FormateurModification', params: { id: formation.id } }" 
          class="btn btn-outline-action d-flex align-items-center justify-content-center flex-1 py-2 text-decoration-none"
        >
          <span class="material-symbols-outlined fs-5">edit</span>
          <span>Modifier</span>
        </router-link>
        
        <!-- Bouton Supprimer -->
        <button 
          @click="$emit('delete', formation.id)" 
          class="btn btn-delete-action d-flex align-items-center justify-content-center p-2"
          title="Supprimer la formation"
        >
          <span class="material-symbols-outlined fs-5">delete</span>
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'

// 1. Déclaration des propriétés reçues du composant parent
const props = defineProps({
  formation: {
    type: Object,
    required: true,
    // Exemple attendu : { id: 1, titre: '...', dateDebut: '...', placesOccupees: 10, placesTotales: 30, statut: 'Validée' }
  }
})

// 2. Déclaration de l'événement émis pour la suppression
defineEmits(['delete'])

// 3. Calcul du pourcentage de remplissage
const percentFull = computed(() => {
  if (!props.formation.placesTotales) return 0
  return Math.min((props.formation.placesOccupees / props.formation.placesTotales) * 100, 100)
})

// 4. Classes et icônes dynamiques selon le statut
const statusClass = computed(() => {
  const s = props.formation.statut
  if (s === 'Validée') return 'badge-valid'
  if (s === 'En attente') return 'badge-pending'
  if (s === 'Rejetée') return 'badge-rejected'
  return 'badge-muted'
})

const statusIcon = computed(() => {
  const s = props.formation.statut
  if (s === 'Validée') return 'check_circle'
  if (s === 'En attente') return 'schedule'
  if (s === 'Rejetée') return 'cancel'
  return 'help'
})

// Couleur de la jauge selon le taux de remplissage
const progressBarClass = computed(() => {
  if (percentFull.value >= 100) return 'bg-danger'
  if (percentFull.value >= 75) return 'bg-warning'
  return 'bg-success'
})
</script>

<style scoped>
.formation-card {
  border: 1px solid #eef0f2;
  border-radius: 12px;
  background-color: #ffffff;
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}

.formation-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.05) !important;
}

/* Forcer l'affichage sur 2 lignes max du titre */
.text-truncate-2 {
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;  
  overflow: hidden;
  height: 48px; /* Assure un alignement parfait si le titre fait 1 ou 2 lignes */
}

.date-text {
  font-size: 0.85rem;
  color: #6c757d;
}

/* Badges personnalisés */
.status-badge {
  display: inline-flex;
  align-items: center;
  gap: 0.35rem;
  padding: 0.35rem 0.75rem;
  border-radius: 20px;
  font-size: 0.8rem;
  font-weight: 600;
}

.status-icon {
  font-size: 16px;
}

.badge-valid { background-color: #e8f5e9; color: #2e7d32; }
.badge-pending { background-color: #fff3e0; color: #ef6c00; }
.badge-rejected { background-color: #ffebee; color: #c62828; }
.badge-muted { background-color: #f5f5f5; color: #616161; }

/* Jauge de progression */
.custom-progress {
  background-color: #f1f3f5;
  border-radius: 10px;
}

/* Boutons d'action */
.flex-1 {
  flex: 1;
}

.btn-outline-action {
  border: 1px solid #121212;
  color: #121212;
  background: transparent;
  font-weight: 600;
  font-size: 0.9rem;
  border-radius: 8px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 0.5rem;
  transition: all 0.2s;
}

.btn-outline-action:hover {
  background-color: #121212;
  color: #ffffff;
}

.btn-delete-action {
  background-color: #ffebee;
  color: #c62828;
  border: none;
  border-radius: 8px;
  transition: all 0.2s;
}

.btn-delete-action:hover {
  background-color: #ffcdd2;
}
</style>