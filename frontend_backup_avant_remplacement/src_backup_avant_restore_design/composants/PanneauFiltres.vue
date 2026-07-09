<template>
  <div class="panneau-filtres">
    <div class="filtres-header">
      <h3 class="filtres-title">
        <i class="bi bi-funnel-fill me-2"></i>
        Filtres
      </h3>
      <button class="btn btn-link btn-sm" @click="resetFilters">
        <i class="bi bi-arrow-counterclockwise me-1"></i>
        Réinitialiser
      </button>
    </div>

    <div class="filtres-content">
      <!-- Catégorie -->
      <div class="filtre-section">
        <h4 class="filtre-section-title">Catégorie</h4>
        <div class="checkbox-group">
          <div v-for="cat in categories" :key="cat.id" class="form-check">
            <input
              type="checkbox"
              class="form-check-input"
              :id="`cat-${cat.id}`"
              v-model="filters.categories"
              :value="cat.id"
            />
            <label class="form-check-label" :for="`cat-${cat.id}`">
              {{ cat.name }}
            </label>
          </div>
        </div>
      </div>

      <!-- Lieu / Ville -->
      <div class="filtre-section">
        <h4 class="filtre-section-title">Lieu / Ville</h4>
        <select class="form-select" v-model="filters.ville">
          <option value="">Toutes les villes</option>
          <option v-for="ville in villes" :key="ville" :value="ville">
            {{ ville }}
          </option>
        </select>
      </div>

      <!-- Prix -->
      <div class="filtre-section">
        <h4 class="filtre-section-title">Prix (Ar)</h4>
        <div class="price-range">
          <div class="price-inputs">
            <div class="input-group">
              <span class="input-group-text">Min</span>
              <input
                type="number"
                class="form-control"
                v-model="filters.prixMin"
                placeholder="0"
                min="0"
              />
            </div>
            <div class="input-group">
              <span class="input-group-text">Max</span>
              <input
                type="number"
                class="form-control"
                v-model="filters.prixMax"
                placeholder="∞"
                min="0"
              />
            </div>
          </div>
          <input
            type="range"
            class="form-range"
            v-model="filters.prixMax"
            min="0"
            max="1000000"
            step="10000"
          />
        </div>
      </div>

      <!-- Durée -->
      <div class="filtre-section">
        <h4 class="filtre-section-title">Durée</h4>
        <div class="radio-group">
          <div v-for="duree in durees" :key="duree.id" class="form-check">
            <input
              type="radio"
              class="form-check-input"
              :id="`duree-${duree.id}`"
              v-model="filters.duree"
              :value="duree.id"
            />
            <label class="form-check-label" :for="`duree-${duree.id}`">
              {{ duree.label }}
            </label>
          </div>
        </div>
      </div>

      <!-- Centre de formation -->
      <div class="filtre-section">
        <h4 class="filtre-section-title">Centre de formation</h4>
        <select class="form-select" v-model="filters.centre">
          <option value="">Tous les centres</option>
          <option v-for="centre in centres" :key="centre.id" :value="centre.id">
            {{ centre.name }}
          </option>
        </select>
      </div>

      <!-- Date de début -->
      <div class="filtre-section">
        <h4 class="filtre-section-title">Date de début</h4>
        <input
          type="date"
          class="form-control"
          v-model="filters.dateDebut"
        />
      </div>

      <!-- Places disponibles -->
      <div class="filtre-section">
        <div class="form-check">
          <input
            type="checkbox"
            class="form-check-input"
            id="placesDispo"
            v-model="filters.placesDisponibles"
          />
          <label class="form-check-label" for="placesDispo">
            Places disponibles uniquement
          </label>
        </div>
      </div>

      <!-- Bouton Appliquer -->
      <div class="filtre-actions">
        <button class="btn btn-primary w-100" @click="applyFilters">
          <i class="bi bi-check-lg me-2"></i>
          Appliquer les filtres
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, watch } from 'vue'

const emit = defineEmits(['apply', 'reset'])

const filters = reactive({
  categories: [],
  ville: '',
  prixMin: '',
  prixMax: '',
  duree: '',
  centre: '',
  dateDebut: '',
  placesDisponibles: false
})

const categories = [
  { id: 'dev', name: 'Développement' },
  { id: 'design', name: 'Design' },
  { id: 'marketing', name: 'Marketing Digital' },
  { id: 'data', name: 'Data Science' },
  { id: 'business', name: 'Business' },
  { id: 'langues', name: 'Langues' },
  { id: 'soft-skills', name: 'Soft Skills' }
]

const villes = [
  'Antananarivo',
  'Toamasina',
  'Antsirabe',
  'Fianarantsoa',
  'Mahajanga',
  'Toliara',
  'Diego Suarez'
]

const durees = [
  { id: 'courte', label: 'Courte (< 1 mois)' },
  { id: 'moyenne', label: 'Moyenne (1-3 mois)' },
  { id: 'longue', label: 'Longue (> 3 mois)' }
]

const centres = [
  { id: 1, name: 'E-OFANA Centre Antananarivo' },
  { id: 2, name: 'E-OFANA Centre Toamasina' },
  { id: 3, name: 'E-OFANA Centre Antsirabe' },
  { id: 4, name: 'Tech Academy Madagascar' },
  { id: 5, name: 'Digital Skills Institute' }
]

const applyFilters = () => {
  emit('apply', { ...filters })
}

const resetFilters = () => {
  filters.categories = []
  filters.ville = ''
  filters.prixMin = ''
  filters.prixMax = ''
  filters.duree = ''
  filters.centre = ''
  filters.dateDebut = ''
  filters.placesDisponibles = false
  emit('reset')
}

// Auto-apply on price change
watch(() => filters.prixMax, () => {
  applyFilters()
})
</script>

<style scoped>
.panneau-filtres {
  background: white;
  border-radius: var(--eo-radius-xl);
  box-shadow: var(--eo-shadow-md);
  padding: var(--eo-spacing-lg);
  position: sticky;
  top: var(--eo-spacing-lg);
  max-height: calc(100vh - 2 * var(--eo-spacing-lg));
  overflow-y: auto;
}

.filtres-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: var(--eo-spacing-lg);
  padding-bottom: var(--eo-spacing-md);
  border-bottom: 1px solid var(--eo-gray-200);
}

.filtres-title {
  font-size: var(--eo-font-size-lg);
  font-weight: 700;
  margin: 0;
  color: var(--eo-primary);
  font-family: var(--eo-font-family);
}

.filtres-content {
  display: flex;
  flex-direction: column;
  gap: var(--eo-spacing-lg);
}

.filtre-section {
  display: flex;
  flex-direction: column;
  gap: var(--eo-spacing-sm);
}

.filtre-section-title {
  font-size: var(--eo-font-size-base);
  font-weight: 600;
  margin: 0;
  color: var(--eo-gray-700);
}

.checkbox-group,
.radio-group {
  display: flex;
  flex-direction: column;
  gap: var(--eo-spacing-sm);
}

.form-check {
  margin: 0;
}

.form-check-input:checked {
  background-color: var(--eo-primary);
  border-color: var(--eo-primary);
}

.form-check-input:focus {
  box-shadow: 0 0 0 0.2rem rgba(0, 102, 204, 0.25);
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

.price-range {
  display: flex;
  flex-direction: column;
  gap: var(--eo-spacing-sm);
}

.price-inputs {
  display: flex;
  gap: var(--eo-spacing-sm);
}

.price-inputs .input-group {
  flex: 1;
}

.input-group-text {
  background-color: var(--eo-gray-100);
  border-color: var(--eo-gray-300);
  color: var(--eo-gray-600);
}

.form-range {
  accent-color: var(--eo-primary);
}

.filtre-actions {
  margin-top: var(--eo-spacing-md);
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

.btn-primary:hover {
  background-color: var(--eo-primary-dark);
  border-color: var(--eo-primary-dark);
  transform: translateY(-2px);
  box-shadow: var(--eo-shadow-lg);
}

.btn-link {
  color: var(--eo-primary);
  text-decoration: none;
  padding: 0;
}

.btn-link:hover {
  color: var(--eo-primary-dark);
  text-decoration: underline;
}

@media (max-width: 992px) {
  .panneau-filtres {
    position: static;
    max-height: none;
  }
}
</style>
