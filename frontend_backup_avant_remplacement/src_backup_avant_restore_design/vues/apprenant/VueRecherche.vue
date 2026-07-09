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
          <div class="col-lg-3 mb-4">
            <div class="filters-card">
              <div class="filters-title">
                <i class="bi bi-funnel-fill me-2"></i>
                Filtres
              </div>

              <button class="btn-reset" @click="resetAll">
                <i class="bi bi-arrow-counterclockwise me-1"></i>
                Réinitialiser
              </button>

              <hr />

              <div class="mb-4">
                <label class="filter-label">Catégorie</label>
                <select v-model="selectedCategory" class="form-select">
                  <option value="">Toutes les catégories</option>
                  <option
                    v-for="categorie in categoriesDisponibles"
                    :key="categorie"
                    :value="categorie"
                  >
                    {{ categorie }}
                  </option>
                </select>
              </div>

              <div class="mb-4">
                <label class="filter-label">Lieu / Ville</label>
                <select v-model="selectedCity" class="form-select">
                  <option value="">Toutes les villes</option>
                  <option
                    v-for="ville in villesDisponibles"
                    :key="ville"
                    :value="ville"
                  >
                    {{ ville }}
                  </option>
                </select>
              </div>

              <div class="mb-4">
                <label class="filter-label">Prix maximum</label>
                <input
                  type="range"
                  class="form-range"
                  min="0"
                  max="500000"
                  step="50000"
                  v-model.number="maxPrice"
                />
                <div class="d-flex justify-content-between small text-muted">
                  <span>0 Ar</span>
                  <strong>{{ formatPrice(maxPrice) }} Ar</strong>
                </div>
              </div>

              <div>
                <label class="filter-label">Places disponibles</label>
                <div class="form-check">
                  <input
                    id="places"
                    v-model="onlyAvailable"
                    class="form-check-input"
                    type="checkbox"
                  />
                  <label class="form-check-label" for="places">
                    Afficher seulement les places disponibles
                  </label>
                </div>
              </div>
            </div>
          </div>

          <div class="col-lg-9">
            <div class="results-header">
              <div>
                <h2 class="results-title">
                  {{ sortedResults.length }}
                  formation{{ sortedResults.length > 1 ? "s" : "" }}
                  trouvée{{ sortedResults.length > 1 ? "s" : "" }}
                </h2>

                <p class="results-subtitle" v-if="searchQuery">
                  Recherche : "{{ searchQuery }}"
                </p>
              </div>

              <div class="results-controls">
                <select class="form-select" v-model="sortBy">
                  <option value="pertinence">Pertinence</option>
                  <option value="prix-asc">Prix croissant</option>
                  <option value="prix-desc">Prix décroissant</option>
                </select>
              </div>
            </div>

            <div v-if="loading" class="alert alert-info">
              Chargement des formations...
            </div>

            <div v-else-if="error" class="alert alert-danger">
              {{ error }}
            </div>

            <div v-else-if="sortedResults.length === 0" class="no-results">
              <div class="no-results-icon">
                <i class="bi bi-search"></i>
              </div>

              <h3>Aucune formation trouvée</h3>
              <p>Modifie les critères de recherche ou réinitialise les filtres.</p>

              <button class="btn btn-primary" @click="resetAll">
                Réinitialiser tout
              </button>
            </div>

            <div v-else class="results-grid">
              <div
                v-for="formation in sortedResults"
                :key="formation.idFormation"
                class="formation-card"
              >
                <div class="formation-image">
                  <div class="formation-category">
                    {{ formation.categorie || "Formation" }}
                  </div>

                  <div
                    class="formation-badge"
                    v-if="formation.placesDisponibles"
                  >
                    <i class="bi bi-check-circle-fill"></i>
                    Places dispo
                  </div>
                </div>

                <div class="formation-content">
                  <h3 class="formation-title">
                    {{ formation.titre }}
                  </h3>

                  <p class="formation-description">
                    {{ formation.description }}
                  </p>

                  <div class="formation-meta">
                    <span class="meta-item">
                      <i class="bi bi-geo-alt-fill"></i>
                      {{ formation.ville || formation.lieu || "Lieu non défini" }}
                    </span>

                    <span class="meta-item">
                      <i class="bi bi-clock-fill"></i>
                      {{ formation.duree || "Durée non définie" }}
                    </span>

                    <span class="meta-item">
                      <i class="bi bi-people-fill"></i>
                      {{ formation.placesRestantes || 0 }} places
                    </span>
                  </div>

                  <div class="formation-footer">
                    <div class="formation-price">
                      <span class="price-amount">
                        {{ formatPrice(formation.prixRemise || formation.prix) }}
                      </span>
                      <span class="price-label">Ar</span>
                    </div>

                    <button
                      class="btn btn-primary"
                      @click="goToDetail(formation.idFormation)"
                    >
                      Voir détails
                    </button>
                  </div>
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
import { computed, onMounted, ref } from "vue";
import { useRouter } from "vue-router";
import { obtenirFormations } from "../../api/formations";

const router = useRouter();

const searchQuery = ref("");
const selectedCity = ref("");
const selectedCategory = ref("");
const maxPrice = ref(500000);
const sortBy = ref("pertinence");
const onlyAvailable = ref(false);

const results = ref([]);
const loading = ref(false);
const error = ref("");

const chargerFormations = async () => {
  try {
    loading.value = true;
    error.value = "";

    results.value = await obtenirFormations();

    console.log("Formations depuis backend :", results.value);
  } catch (e) {
    console.error("Erreur chargement formations :", e);
    error.value =
      "Impossible de charger les formations depuis le backend. Vérifie que Spring Boot est lancé sur 8081.";
  } finally {
    loading.value = false;
  }
};

const normaliserTexte = (valeur) =>
  String(valeur || "")
    .normalize("NFD")
    .replace(/[\u0300-\u036f]/g, "")
    .trim()
    .toLowerCase();

const villesDisponibles = computed(() => {
  const villes = results.value
    .map((formation) => formation.ville || formation.lieu)
    .filter(Boolean);

  return [...new Set(villes)].sort();
});

const categoriesDisponibles = computed(() => {
  const categories = results.value
    .map((formation) => formation.categorie)
    .filter(Boolean);

  return [...new Set(categories)].sort();
});

const filteredResults = computed(() => {
  const terme = normaliserTexte(searchQuery.value);
  const ville = normaliserTexte(selectedCity.value);
  const categorie = normaliserTexte(selectedCategory.value);

  return results.value.filter((formation) => {
    const prix = Number(formation.prixRemise || formation.prix || 0);

    const matchSearch =
      !terme ||
      [
        formation.titre,
        formation.description,
        formation.categorie,
        formation.centre,
        formation.ville,
        formation.lieu,
      ].some((valeur) => normaliserTexte(valeur).includes(terme));

    const matchVille =
      !ville ||
      [formation.ville, formation.lieu].some(
        (valeur) => normaliserTexte(valeur) === ville
      );

    const matchCategorie =
      !categorie ||
      normaliserTexte(formation.categorie) === categorie;

    const matchPrix = prix <= maxPrice.value;

    const matchPlaces =
      !onlyAvailable.value || Number(formation.placesRestantes || 0) > 0;

    return (
      matchSearch &&
      matchVille &&
      matchCategorie &&
      matchPrix &&
      matchPlaces
    );
  });
});

const sortedResults = computed(() => {
  const liste = [...filteredResults.value];

  if (sortBy.value === "prix-asc") {
    return liste.sort(
      (a, b) =>
        Number(a.prixRemise || a.prix || 0) -
        Number(b.prixRemise || b.prix || 0)
    );
  }

  if (sortBy.value === "prix-desc") {
    return liste.sort(
      (a, b) =>
        Number(b.prixRemise || b.prix || 0) -
        Number(a.prixRemise || a.prix || 0)
    );
  }

  return liste;
});

const performSearch = () => {
  searchQuery.value = searchQuery.value.trim();
};

const resetAll = () => {
  searchQuery.value = "";
  selectedCity.value = "";
  selectedCategory.value = "";
  maxPrice.value = 500000;
  sortBy.value = "pertinence";
  onlyAvailable.value = false;
};

const formatPrice = (price) => {
  return new Intl.NumberFormat("fr-FR").format(Number(price || 0));
};

const goToDetail = (idFormation) => {
  router.push(`/formations/${idFormation}`);
};

onMounted(() => {
  chargerFormations();
});
</script>

<style scoped>
.recherche-container {
  min-height: 100vh;
  background: #f5f5f5;
}

.recherche-header {
  background: #ffffff;
  padding: 32px 0;
  border-bottom: 1px solid #e5e7eb;
}

.search-bar {
  max-width: 800px;
  margin: 0 auto;
}

.input-group-text {
  background: #f8fafc;
}

.btn-primary {
  background-color: #111111;
  border-color: #111111;
  color: #ffffff;
  font-weight: 700;
}

.btn-primary:hover {
  background-color: #000000;
  border-color: #000000;
}

.recherche-content {
  padding: 48px 0;
}

.filters-card {
  background: #ffffff;
  border-radius: 12px;
  padding: 24px;
  border: 1px solid #e5e7eb;
  position: sticky;
  top: 24px;
}

.filters-title {
  font-size: 20px;
  font-weight: 800;
  margin-bottom: 12px;
}

.btn-reset {
  border: none;
  background: transparent;
  color: #d4a64f;
  font-weight: 700;
  padding: 0;
}

.filter-label {
  font-size: 13px;
  font-weight: 800;
  color: #374151;
  text-transform: uppercase;
  margin-bottom: 8px;
  display: block;
}

.results-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.results-title {
  font-size: 26px;
  font-weight: 800;
  margin: 0;
}

.results-subtitle {
  margin: 6px 0 0;
  color: #6b7280;
}

.results-controls {
  width: 200px;
}

.results-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 22px;
}

.formation-card {
  background: #ffffff;
  border-radius: 14px;
  overflow: hidden;
  border: 1px solid #e5e7eb;
  box-shadow: 0 10px 24px rgba(0, 0, 0, 0.05);
}

.formation-image {
  height: 170px;
  background: linear-gradient(135deg, #111111, #444444);
  position: relative;
}

.formation-category {
  position: absolute;
  top: 62px;
  left: 50%;
  transform: translateX(-50%);
  background: #ffffff;
  padding: 10px 26px;
  border-radius: 999px;
  font-weight: 800;
}

.formation-badge {
  position: absolute;
  top: 18px;
  right: 18px;
  background: #238636;
  color: #ffffff;
  padding: 7px 12px;
  border-radius: 999px;
  font-weight: 700;
  display: flex;
  align-items: center;
  gap: 6px;
}

.formation-content {
  padding: 24px;
}

.formation-title {
  font-size: 20px;
  font-weight: 800;
  margin-bottom: 10px;
}

.formation-description {
  color: #4b5563;
  min-height: 48px;
}

.formation-meta {
  display: flex;
  flex-direction: column;
  gap: 8px;
  color: #6b7280;
  margin: 18px 0;
}

.meta-item {
  display: flex;
  gap: 8px;
  align-items: center;
}

.formation-footer {
  border-top: 1px solid #e5e7eb;
  padding-top: 18px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.price-amount {
  font-size: 22px;
  font-weight: 900;
}

.price-label {
  display: block;
  color: #6b7280;
}

.no-results {
  text-align: center;
  background: #ffffff;
  padding: 48px;
  border-radius: 12px;
  border: 1px solid #e5e7eb;
}

.no-results-icon {
  font-size: 42px;
  color: #d4a64f;
  margin-bottom: 12px;
}

@media (max-width: 992px) {
  .results-grid {
    grid-template-columns: 1fr 1fr;
  }

  .results-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 16px;
  }

  .results-controls {
    width: 100%;
  }
}

@media (max-width: 768px) {
  .results-grid {
    grid-template-columns: 1fr;
  }
}
</style>