<template>
  <div class="recherche-container">
    <div class="recherche-header">
      <div class="container">
        <div class="search-bar">
          <div class="input-group">
            <span class="input-group-text">
              <i class="bi bi-search"></i>
            </span>
            <input
              type="text"
              class="form-control"
              v-model="searchQuery"
              placeholder="Rechercher une formation..."
              @keyup.enter="performSearch"
            />
            <button class="btn btn-primary" @click="performSearch">
              Rechercher
            </button>
          </div>
        </div>
      </div>
    </div>

    <div class="recherche-content">
      <div class="container">
        <div class="row">
          <!-- Filtres -->
          <div class="col-lg-3 mb-4">
            <PanneauFiltres @apply="handleApplyFilters" @reset="handleResetFilters" />
          </div>

          <!-- Résultats -->
          <div class="col-lg-9">
            <div class="results-header">
              <div class="results-info">
                <h2 class="results-title">
                  {{ results.length }} formation{{ results.length > 1 ? 's' : '' }} trouvée{{ results.length > 1 ? 's' : '' }}
                </h2>
                <p class="results-subtitle" v-if="searchQuery || hasActiveFilters">
                  pour "{{ searchQuery }}" {{ hasActiveFilters ? 'avec filtres actifs' : '' }}
                </p>
              </div>

              <div class="results-controls">
                <!-- Tri -->
                <div class="sort-dropdown">
                  <select class="form-select" v-model="sortBy" @change="sortResults">
                    <option value="pertinence">Pertinence</option>
                    <option value="prix-asc">Prix croissant</option>
                    <option value="prix-desc">Prix décroissant</option>
                    <option value="date">Date de début</option>
                    <option value="duree">Durée</option>
                  </select>
                </div>

                <!-- Vue grille/liste -->
                <div class="view-toggle">
                  <button
                    class="btn btn-outline-secondary"
                    :class="{ active: viewMode === 'grid' }"
                    @click="viewMode = 'grid'"
                  >
                    <i class="bi bi-grid-fill"></i>
                  </button>
                  <button
                    class="btn btn-outline-secondary"
                    :class="{ active: viewMode === 'list' }"
                    @click="viewMode = 'list'"
                  >
                    <i class="bi bi-list-ul"></i>
                  </button>
                </div>
              </div>
            </div>

            <!-- Aucun résultat -->
            <div v-if="results.length === 0" class="no-results">
              <div class="no-results-icon">
                <i class="bi bi-search"></i>
              </div>
              <h3>Aucune formation trouvée</h3>
              <p>Essayez de modifier vos critères de recherche ou de réinitialiser les filtres</p>
              <button class="btn btn-primary" @click="resetAll">
                <i class="bi bi-arrow-counterclockwise me-2"></i>
                Réinitialiser tout
              </button>
            </div>

            <!-- Grille de résultats -->
            <div v-else class="results-grid" :class="viewMode">
              <div
                v-for="formation in paginatedResults"
                :key="formation.id"
                class="formation-card"
              >
                <div class="formation-image">
                  <div class="formation-category">{{ formation.category }}</div>
                  <div class="formation-badge" v-if="formation.placesDisponibles">
                    <i class="bi bi-check-circle-fill"></i>
                    Places dispo
                  </div>
                </div>
                <div class="formation-content">
                  <h3 class="formation-title">{{ formation.title }}</h3>
                  <p class="formation-description">{{ formation.description }}</p>
                  
                  <div class="formation-meta">
                    <span class="meta-item">
                      <i class="bi bi-geo-alt-fill"></i>
                      {{ formation.ville }}
                    </span>
                    <span class="meta-item">
                      <i class="bi bi-clock-fill"></i>
                      {{ formation.duree }}
                    </span>
                    <span class="meta-item">
                      <i class="bi bi-calendar-fill"></i>
                      {{ formation.dateDebut }}
                    </span>
                  </div>

                  <div class="formation-footer">
                    <div class="formation-price">
                      <span class="price-amount">{{ formatPrice(formation.prix) }}</span>
                      <span class="price-label">Ar</span>
                    </div>
                    <router-link :to="`/formations/${formation.id}`" class="btn btn-primary">
                      Voir détails
                    </router-link>
                  </div>
                </div>
              </div>
            </div>

            <!-- Pagination -->
            <div v-if="totalPages > 1" class="pagination">
              <nav>
                <ul class="pagination">
                  <li class="page-item" :class="{ disabled: currentPage === 1 }">
                    <button class="page-link" @click="currentPage--" :disabled="currentPage === 1">
                      <i class="bi bi-chevron-left"></i>
                    </button>
                  </li>
                  <li
                    v-for="page in visiblePages"
                    :key="page"
                    class="page-item"
                    :class="{ active: currentPage === page }"
                  >
                    <button class="page-link" @click="currentPage = page">
                      {{ page }}
                    </button>
                  </li>
                  <li class="page-item" :class="{ disabled: currentPage === totalPages }">
                    <button class="page-link" @click="currentPage++" :disabled="currentPage === totalPages">
                      <i class="bi bi-chevron-right"></i>
                    </button>
                  </li>
                </ul>
              </nav>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from "vue";
import PanneauFiltres from "../../composants/PanneauFiltres.vue";
import { obtenirFormations } from "../../api/formations";

const searchQuery = ref("");
const sortBy = ref("pertinence");
const viewMode = ref("grid");
const currentPage = ref(1);
const resultsPerPage = 10;

const activeFilters = ref({});
const results = ref([]);
const allResults = ref([]);
const loading = ref(false);

const getCategoryKey = (category) => {
  const value = String(category || "").toLowerCase();

  if (value.includes("développement") || value.includes("developpement")) {
    return "dev";
  }

  if (value.includes("design")) {
    return "design";
  }

  if (value.includes("marketing")) {
    return "marketing";
  }

  if (value.includes("data")) {
    return "data";
  }

  if (value.includes("business")) {
    return "business";
  }

  if (value.includes("langue")) {
    return "langues";
  }

  if (value.includes("soft")) {
    return "soft-skills";
  }

  return value;
};

const hasActiveFilters = computed(() => {
  return Object.keys(activeFilters.value).some((key) => {
    const value = activeFilters.value[key];

    if (Array.isArray(value)) {
      return value.length > 0;
    }

    return value !== "" && value !== false && value !== null && value !== undefined;
  });
});

const totalPages = computed(() => {
  return Math.ceil(results.value.length / resultsPerPage);
});

const paginatedResults = computed(() => {
  const start = (currentPage.value - 1) * resultsPerPage;
  const end = start + resultsPerPage;

  return results.value.slice(start, end);
});

const visiblePages = computed(() => {
  const pages = [];
  const maxVisible = 5;

  let start = Math.max(1, currentPage.value - Math.floor(maxVisible / 2));
  let end = Math.min(totalPages.value, start + maxVisible - 1);

  if (end - start < maxVisible - 1) {
    start = Math.max(1, end - maxVisible + 1);
  }

  for (let i = start; i <= end; i++) {
    pages.push(i);
  }

  return pages;
});

const formatPrice = (price) => {
  return Number(price || 0).toLocaleString("fr-FR");
};

const chargerFormations = async () => {
  loading.value = true;

  try {
    const data = await obtenirFormations();

    console.log("Formations reçues dans VueRecherche =", data);

    allResults.value = data.map((formation) => {
      const category = formation.category || formation.categorie || "Formation";

      return {
        ...formation,

        id: formation.idFormation || formation.id,

        title: formation.title || formation.titre || "Formation sans titre",
        titre: formation.titre || formation.title || "Formation sans titre",

        category,
        categorie: category,
        categoryKey: getCategoryKey(category),

        description: formation.description || "",

        ville: formation.ville || formation.lieu || "Antananarivo",
        lieu: formation.lieu || formation.ville || "Antananarivo",

        duree: formation.duree || "Durée non définie",
        dateDebut: formation.dateDebut || "À définir",

        prix: Number(
          formation.prixFinal ||
            formation.prixRemise ||
            formation.prix ||
            0
        ),

        placesDisponibles: true,
        pertinence: 100,
      };
    });

    results.value = [...allResults.value];
    sortResults();
  } catch (error) {
    console.error("Erreur chargement formations backend :", error);
    results.value = [];
    allResults.value = [];
  } finally {
    loading.value = false;
  }
};

const performSearch = () => {
  currentPage.value = 1;
  applyLocalFilters();
  sortResults();
};

const handleApplyFilters = (filters) => {
  activeFilters.value = filters || {};
  currentPage.value = 1;
  applyLocalFilters();
  sortResults();
};

const handleResetFilters = () => {
  activeFilters.value = {};
  currentPage.value = 1;
  results.value = [...allResults.value];
  sortResults();
};

const applyLocalFilters = () => {
  let filtered = [...allResults.value];

  if (searchQuery.value) {
    const q = searchQuery.value.toLowerCase();

    filtered = filtered.filter((formation) => {
      return (
        String(formation.title || "").toLowerCase().includes(q) ||
        String(formation.titre || "").toLowerCase().includes(q) ||
        String(formation.description || "").toLowerCase().includes(q) ||
        String(formation.category || "").toLowerCase().includes(q) ||
        String(formation.categorie || "").toLowerCase().includes(q) ||
        String(formation.ville || "").toLowerCase().includes(q)
      );
    });
  }

  if (activeFilters.value.categories?.length > 0) {
    filtered = filtered.filter((formation) => {
      return activeFilters.value.categories.includes(formation.categoryKey);
    });
  }

  if (activeFilters.value.ville) {
    filtered = filtered.filter((formation) => {
      return formation.ville === activeFilters.value.ville;
    });
  }

  if (activeFilters.value.prixMin) {
    filtered = filtered.filter((formation) => {
      return Number(formation.prix || 0) >= Number(activeFilters.value.prixMin);
    });
  }

  if (activeFilters.value.prixMax) {
    filtered = filtered.filter((formation) => {
      return Number(formation.prix || 0) <= Number(activeFilters.value.prixMax);
    });
  }

  if (activeFilters.value.duree) {
    filtered = filtered.filter((formation) => {
      const mois = parseInt(formation.duree);

      if (activeFilters.value.duree === "courte") return mois < 1;
      if (activeFilters.value.duree === "moyenne") return mois >= 1 && mois <= 3;
      if (activeFilters.value.duree === "longue") return mois > 3;

      return true;
    });
  }

  if (activeFilters.value.centre) {
    filtered = filtered.filter((formation) => {
      return String(formation.centre || "").includes(String(activeFilters.value.centre));
    });
  }

  if (activeFilters.value.dateDebut) {
    filtered = filtered.filter((formation) => {
      return String(formation.dateDebut || "").includes(String(activeFilters.value.dateDebut));
    });
  }

  if (activeFilters.value.placesDisponibles) {
    filtered = filtered.filter((formation) => formation.placesDisponibles);
  }

  results.value = filtered;
};

const sortResults = () => {
  switch (sortBy.value) {
    case "prix-asc":
      results.value.sort((a, b) => Number(a.prix || 0) - Number(b.prix || 0));
      break;

    case "prix-desc":
      results.value.sort((a, b) => Number(b.prix || 0) - Number(a.prix || 0));
      break;

    case "date":
      results.value.sort((a, b) =>
        String(a.dateDebut || "").localeCompare(String(b.dateDebut || ""))
      );
      break;

    case "duree":
      results.value.sort((a, b) =>
        String(a.duree || "").localeCompare(String(b.duree || ""))
      );
      break;

    default:
      results.value.sort(
        (a, b) => Number(b.pertinence || 0) - Number(a.pertinence || 0)
      );
  }
};

const resetAll = () => {
  searchQuery.value = "";
  activeFilters.value = {};
  sortBy.value = "pertinence";
  currentPage.value = 1;
  results.value = [...allResults.value];
  sortResults();
};

onMounted(() => {
  chargerFormations();
});
</script>

<style scoped>
.recherche-container {
  min-height: 100vh;
  background: var(--eo-gray-50);
}

.recherche-header {
  background: white;
  padding: var(--eo-spacing-xl) 0;
  box-shadow: var(--eo-shadow-sm);
  position: sticky;
  top: 0;
  z-index: var(--eo-z-sticky);
}

.search-bar {
  max-width: 800px;
  margin: 0 auto;
}

.input-group-text {
  background-color: var(--eo-gray-100);
  border-color: var(--eo-gray-300);
  color: var(--eo-primary);
}

.form-control {
  border-color: var(--eo-gray-300);
  padding: var(--eo-spacing-md);
}

.form-control:focus {
  border-color: var(--eo-primary);
  box-shadow: 0 0 0 0.2rem rgba(0, 102, 204, 0.25);
}

.btn-primary {
  background-color: var(--eo-primary);
  border-color: var(--eo-primary);
  color: white !important; /* <--- AJOUTÉ : Force le texte en blanc dès le départ */
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

.recherche-content {
  padding: var(--eo-spacing-2xl) 0;
}

.results-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: var(--eo-spacing-xl);
  flex-wrap: wrap;
  gap: var(--eo-spacing-md);
}

.results-title {
  font-size: var(--eo-font-size-2xl);
  font-weight: 700;
  margin: 0;
  color: var(--eo-gray-800);
  font-family: var(--eo-font-family);
}

.results-subtitle {
  font-size: var(--eo-font-size-sm);
  color: var(--eo-gray-500);
  margin: var(--eo-spacing-xs) 0 0 0;
}

.results-controls {
  display: flex;
  gap: var(--eo-spacing-md);
  align-items: center;
}

.sort-dropdown {
  min-width: 200px;
}

.form-select {
  border-color: var(--eo-gray-300);
}

.form-select:focus {
  border-color: var(--eo-primary);
  box-shadow: 0 0 0 0.2rem rgba(0, 102, 204, 0.25);
}

.view-toggle {
  display: flex;
  gap: var(--eo-spacing-xs);
}

.view-toggle .btn {
  padding: var(--eo-spacing-sm);
  border-radius: var(--eo-radius-md);
}

.view-toggle .btn.active {
  background-color: var(--eo-primary);
  border-color: var(--eo-primary);
  color: white;
}

.no-results {
  text-align: center;
  padding: var(--eo-spacing-3xl) var(--eo-spacing-xl);
  background: white;
  border-radius: var(--eo-radius-xl);
  box-shadow: var(--eo-shadow-md);
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

.results-grid {
  display: grid;
  gap: var(--eo-spacing-lg);
}

.results-grid.grid {
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
}

.results-grid.list {
  grid-template-columns: 1fr;
}

.formation-card {
  background: white;
  border-radius: var(--eo-radius-xl);
  overflow: hidden;
  box-shadow: var(--eo-shadow-md);
  transition: all var(--eo-transition-base);
  display: flex;
  flex-direction: column;
}

.formation-card:hover {
  box-shadow: var(--eo-shadow-lg);
  transform: translateY(-4px);
}

.results-grid.list .formation-card {
  flex-direction: row;
}

.formation-image {
  height: 150px;
  background: linear-gradient(135deg, var(--eo-primary) 0%, var(--eo-primary-light) 100%);
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
}

.results-grid.list .formation-image {
  width: 200px;
  height: auto;
  flex-shrink: 0;
}

.formation-category {
  background: rgba(255, 255, 255, 0.9);
  padding: var(--eo-spacing-sm) var(--eo-spacing-md);
  border-radius: var(--eo-radius-full);
  font-size: var(--eo-font-size-sm);
  font-weight: 600;
  color: var(--eo-primary);
}

.formation-badge {
  position: absolute;
  top: var(--eo-spacing-md);
  right: var(--eo-spacing-md);
  background: var(--eo-success);
  color: white;
  padding: var(--eo-spacing-xs) var(--eo-spacing-sm);
  border-radius: var(--eo-radius-full);
  font-size: var(--eo-font-size-xs);
  font-weight: 600;
  display: flex;
  align-items: center;
  gap: var(--eo-spacing-xs);
}

.formation-content {
  padding: var(--eo-spacing-lg);
  flex: 1;
  display: flex;
  flex-direction: column;
}

.formation-title {
  font-size: var(--eo-font-size-lg);
  font-weight: 600;
  margin: 0 0 var(--eo-spacing-sm) 0;
  color: var(--eo-gray-800);
}

.formation-description {
  font-size: var(--eo-font-size-sm);
  color: var(--eo-gray-600);
  margin: 0 0 var(--eo-spacing-md) 0;
  flex: 1;
}

.formation-meta {
  display: flex;
  flex-wrap: wrap;
  gap: var(--eo-spacing-md);
  margin-bottom: var(--eo-spacing-md);
}

.meta-item {
  font-size: var(--eo-font-size-sm);
  color: var(--eo-gray-500);
  display: flex;
  align-items: center;
  gap: var(--eo-spacing-xs);
}

.formation-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: var(--eo-spacing-md);
  border-top: 1px solid var(--eo-gray-200);
}

.formation-price {
  display: flex;
  flex-direction: column;
}

.price-amount {
  font-size: var(--eo-font-size-xl);
  font-weight: 700;
  color: var(--eo-primary);
}

.price-label {
  font-size: var(--eo-font-size-xs);
  color: var(--eo-gray-500);
}

.pagination {
  margin-top: var(--eo-spacing-2xl);
  display: flex;
  justify-content: center;
}

.page-link {
  color: var(--eo-primary);
  border-color: var(--eo-gray-300);
}

.page-link:hover {
  background-color: var(--eo-primary);
  border-color: var(--eo-primary);
  color: white;
}

.page-item.active .page-link {
  background-color: var(--eo-primary);
  border-color: var(--eo-primary);
  color: white;
}

.page-item.disabled .page-link {
  color: var(--eo-gray-400);
  pointer-events: none;
}

@media (max-width: 992px) {
  .results-header {
    flex-direction: column;
    align-items: flex-start;
  }
  
  .results-controls {
    width: 100%;
    justify-content: space-between;
  }
  
  .results-grid.list .formation-card {
    flex-direction: column;
  }
  
  .results-grid.list .formation-image {
    width: 100%;
    height: 150px;
  }
}

@media (max-width: 768px) {
  .results-grid.grid {
    grid-template-columns: 1fr;
  }
  
  .results-controls {
    flex-direction: column;
    align-items: stretch;
  }
  
  .sort-dropdown {
    width: 100%;
  }
}
</style>
