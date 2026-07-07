<template>
  <div>
    <div class="bg-white border-bottom py-4 mb-5">
      <div class="container-xl">
        <div class="search-pill-container">
          <div class="flex-grow-1 search-field-wrapper">
            <span class="material-symbols-outlined search-field-icon">search</span>

            <input
              v-model="searchQuery"
              class="search-pill-input"
              placeholder="Formation, compétence, domaine..."
              type="text"
              @keyup.enter="handleSearch"
            />
          </div>

          <div class="search-pill-divider"></div>

          <div class="search-field-wrapper" style="min-width: 180px;">
            <span class="material-symbols-outlined search-field-icon">location_on</span>

            <select
              v-model="selectedCity"
              class="form-select search-pill-select"
              @change="handleSearch"
            >
              <option value="all">Toutes les villes</option>

              <option
                v-for="ville in villesDisponibles"
                :key="ville"
                :value="ville"
              >
                {{ ville }}
              </option>
            </select>
          </div>

          <button class="btn-pill-submit" @click="handleSearch">
            <span class="material-symbols-outlined" style="font-size: 18px;">search</span>
            <span>Rechercher</span>
          </button>
        </div>
      </div>
    </div>

    <div class="container-xl mb-5">
      <div class="row g-4">
        <aside class="col-lg-3">
          <div class="card card-filter p-4 mb-4">
            <div class="d-flex justify-content-between align-items-center mb-4">
              <h2 class="h6 fw-bold m-0 d-flex align-items-center gap-2">
                <i class="bi bi-sliders"></i>
                Filtres
              </h2>

              <a
                class="text-decoration-none small text-muted"
                href="#"
                @click.prevent="clearFilters"
              >
                Effacer tout
              </a>
            </div>

            <div class="mb-4">
              <label for="cat-select" class="filter-section-title d-block">
                Catégorie
              </label>

              <select
                id="cat-select"
                v-model="selectedCategory"
                class="form-select form-select-sm custom-select-filter"
                @change="handleSearch"
              >
                <option value="all">Toutes les catégories</option>

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
              <h3 class="filter-section-title">Prix maximum</h3>

              <input
                v-model.number="maxPrice"
                class="form-range"
                max="500000"
                min="0"
                step="50000"
                type="range"
                @change="handleSearch"
              />

              <div class="d-flex justify-content-between text-muted small mt-2">
                <span>0 Ar</span>
                <span class="fw-bold text-dark">
                  {{ formatPrice(maxPrice) }} Ar
                </span>
              </div>
            </div>

            <div>
              <h3 class="filter-section-title">Durée</h3>

              <div class="form-check mb-2">
                <input
                  id="dur-short"
                  v-model="durationFilters.short"
                  class="form-check-input"
                  type="checkbox"
                />
                <label class="form-check-label small" for="dur-short">
                  &lt; 1 mois
                </label>
              </div>

              <div class="form-check mb-2">
                <input
                  id="dur-med"
                  v-model="durationFilters.medium"
                  class="form-check-input"
                  type="checkbox"
                />
                <label class="form-check-label small" for="dur-med">
                  1 à 3 mois
                </label>
              </div>

              <div class="form-check">
                <input
                  id="dur-long"
                  v-model="durationFilters.long"
                  class="form-check-input"
                  type="checkbox"
                />
                <label class="form-check-label small" for="dur-long">
                  &gt; 3 mois
                </label>
              </div>
            </div>
          </div>
        </aside>

        <main class="col-lg-9">
          <div class="d-flex justify-content-between align-items-center mb-4">
            <p class="text-muted small m-0">
              <strong>{{ sortedFormations.length }}</strong>
              formation(s) trouvée(s)
            </p>

            <div class="d-flex align-items-center gap-2">
              <label class="small text-muted text-nowrap" for="sort-select">
                Trier par :
              </label>

              <select
                id="sort-select"
                v-model="sortBy"
                class="form-select form-select-sm"
                style="width: 160px;"
              >
                <option value="relevant">Les plus pertinentes</option>
                <option value="price-asc">Prix : les plus bas</option>
                <option value="price-desc">Prix : les plus hauts</option>
              </select>
            </div>
          </div>

          <div class="d-flex flex-column gap-3">
            <div v-if="loading" class="alert alert-info">
              Chargement des formations...
            </div>

            <div v-else-if="error" class="alert alert-danger">
              {{ error }}
            </div>

            <div v-else-if="sortedFormations.length === 0" class="alert alert-warning">
              Aucune formation trouvée.
            </div>

            <template v-else>
              <div
                v-for="formation in sortedFormations"
                :key="formation.idFormation"
                class="card card-result p-3"
              >
                <div class="row g-3 align-items-center">
                  <div class="col-md-3">
                    <div class="result-img-placeholder">
                      <span
                        class="material-symbols-outlined"
                        style="font-size: 40px; opacity: 0.4;"
                      >
                        code
                      </span>
                    </div>
                  </div>

                  <div class="col-md-5">
                    <span class="badge bg-light text-secondary mb-2" style="font-size: 11px;">
                      {{ formation.categorie || "Formation" }}
                    </span>

                    <h2 class="h5 fw-bold mb-1">
                      {{ formation.titre }}
                    </h2>

                    <p class="text-muted small mb-3">
                      {{ formation.centre || "Centre non défini" }}
                    </p>

                    <div class="d-flex flex-wrap gap-3 text-muted" style="font-size: 12px;">
                      <span>
                        <i class="bi bi-geo-alt"></i>
                        {{ formation.ville || formation.lieu || "Lieu non défini" }}
                      </span>

                      <span>
                        <i class="bi bi-clock"></i>
                        {{ formation.duree || "Durée non définie" }}
                      </span>

                      <span>
                        <i class="bi bi-people"></i>
                        12 places restantes
                      </span>
                    </div>
                  </div>

                  <div
                    class="col-md-4 text-md-end d-flex flex-md-column justify-content-between align-items-center align-items-md-end mt-3 mt-md-0"
                  >
                    <div>
                      <p class="h4 fw-bold text-brand-orange mb-0">
                        {{ formatPrice(formation.prixRemise || formation.prix) }} Ar
                      </p>

                      <div class="d-flex align-items-center justify-content-md-end gap-1 small">
                        <i class="bi bi-star-fill text-brand-gold"></i>

                        <span class="fw-bold">
                          {{ formation.noteMoyenne || 0 }}
                        </span>

                        <span class="text-muted" style="font-size: 11px;">
                          ({{ formation.nbAvis || 0 }})
                        </span>
                      </div>
                    </div>

                    <button
                      class="btn btn-dark fw-semibold px-4 py-2 mt-md-4"
                      @click="goToDetail(formation.idFormation)"
                    >
                      Voir détail
                    </button>
                  </div>
                </div>
              </div>
            </template>
          </div>
        </main>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from "vue";
import { useRouter } from "vue-router";
import { getFormations } from "../api/formations";

const router = useRouter();

const searchQuery = ref("");
const selectedCity = ref("all");
const selectedCategory = ref("all");
const maxPrice = ref(500000);
const sortBy = ref("relevant");

const formations = ref([]);
const toutesLesFormations = ref([]);

const loading = ref(false);
const error = ref("");

const durationFilters = reactive({
  short: false,
  medium: false,
  long: false,
});

const normaliserListeFormations = (data) => {
  if (Array.isArray(data)) {
    return data;
  }

  if (data && Array.isArray(data.content)) {
    return data.content;
  }

  return [];
};

const construireParamsRecherche = () => {
  const params = {};

  if (searchQuery.value && searchQuery.value.trim() !== "") {
    params.search = searchQuery.value.trim();
  }

  if (selectedCity.value && selectedCity.value !== "all") {
    params.ville = selectedCity.value;
  }

  if (selectedCategory.value && selectedCategory.value !== "all") {
    params.categorie = selectedCategory.value;
  }

  if (maxPrice.value && maxPrice.value > 0) {
    params.prixMax = maxPrice.value;
  }

  return params;
};

const chargerToutesLesFormations = async () => {
  try {
    loading.value = true;
    error.value = "";

    const response = await getFormations();
    const data = normaliserListeFormations(response.data);

    toutesLesFormations.value = data;
    formations.value = data;

    console.log("Toutes les formations depuis la base :", data);
  } catch (e) {
    console.error(e);
    error.value = "Impossible de charger les formations depuis la base.";
  } finally {
    loading.value = false;
  }
};

const chargerFormations = async () => {
  try {
    loading.value = true;
    error.value = "";

    const params = construireParamsRecherche();

    const response = await getFormations(params);
    formations.value = normaliserListeFormations(response.data);

    console.log("Paramètres envoyés au backend :", params);
    console.log("Formations filtrées depuis le backend :", formations.value);
  } catch (e) {
    console.error(e);
    error.value = "Impossible de charger les formations depuis la base.";
  } finally {
    loading.value = false;
  }
};

const villesDisponibles = computed(() => {
  const villes = toutesLesFormations.value
    .map((formation) => formation.ville || formation.lieu)
    .filter((ville) => ville && ville.trim() !== "");

  return [...new Set(villes)];
});

const categoriesDisponibles = computed(() => {
  const categories = toutesLesFormations.value
    .map((formation) => formation.categorie)
    .filter((categorie) => categorie && categorie.trim() !== "");

  return [...new Set(categories)];
});

const filteredFormations = computed(() => {
  return formations.value.filter((formation) => {
    const duree = formation.duree || "";

    const hasDurationFilter =
      durationFilters.short ||
      durationFilters.medium ||
      durationFilters.long;

    let matchDuration = true;

    if (hasDurationFilter) {
      const dureeLower = duree.toLowerCase();

      matchDuration =
        (durationFilters.short &&
          (dureeLower.includes("semaine") || dureeLower.includes("jour"))) ||
        (durationFilters.medium &&
          (dureeLower.includes("1 mois") ||
            dureeLower.includes("2 mois") ||
            dureeLower.includes("3 mois"))) ||
        (durationFilters.long &&
          (dureeLower.includes("4 mois") ||
            dureeLower.includes("5 mois") ||
            dureeLower.includes("6 mois") ||
            dureeLower.includes("an")));
    }

    return matchDuration;
  });
});

const sortedFormations = computed(() => {
  const result = [...filteredFormations.value];

  if (sortBy.value === "price-asc") {
    return result.sort(
      (a, b) => (a.prixRemise || a.prix || 0) - (b.prixRemise || b.prix || 0)
    );
  }

  if (sortBy.value === "price-desc") {
    return result.sort(
      (a, b) => (b.prixRemise || b.prix || 0) - (a.prixRemise || a.prix || 0)
    );
  }

  return result;
});

const formatPrice = (value) => {
  if (!value) return "0";
  return new Intl.NumberFormat("fr-FR").format(value);
};

const handleSearch = () => {
  chargerFormations();
};

const clearFilters = () => {
  searchQuery.value = "";
  selectedCity.value = "all";
  selectedCategory.value = "all";
  maxPrice.value = 500000;
  durationFilters.short = false;
  durationFilters.medium = false;
  durationFilters.long = false;

  chargerFormations();
};

const goToDetail = (id) => {
  router.push(`/formations/${id}`);
};

onMounted(() => {
  chargerToutesLesFormations();
});
</script>

<style scoped>
:root {
  --primary-color: #c59d5f;
  --surface-color: #fbf9f8;
  --surface-dim: #dbdad9;
  --dark-color: #1a1a1a;
  --brand-dark: #1a1a1a;
  --brand-gold: #c49a3d;
  --brand-orange: #d97706;
  --border-radius-custom: 8px;
}

.search-pill-container {
  background-color: #ffffff;
  border-radius: 99px;
  padding: 0.35rem;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  border: 1px solid rgba(0, 0, 0, 0.05);
  display: flex;
  align-items: center;
}

.search-field-wrapper {
  display: flex;
  align-items: center;
  padding: 0 1.25rem;
  height: 44px;
}

.search-field-icon {
  color: #94a3b8;
  margin-right: 0.75rem;
  font-size: 20px;
  flex-shrink: 0;
}

.search-pill-input {
  border: none;
  width: 100%;
  font-size: 0.95rem;
  color: #1e293b;
  background: transparent;
}

.search-pill-input:focus {
  outline: none;
}

.search-pill-divider {
  width: 1px;
  height: 24px;
  background-color: #e2e8f0;
  flex-shrink: 0;
}

.search-pill-select {
  border: none;
  background: transparent;
  font-size: 0.95rem;
  color: #1e293b;
  font-weight: 500;
  width: 100%;
  cursor: pointer;
}

.search-pill-select:focus {
  outline: none;
  box-shadow: none;
}

.btn-pill-submit {
  background-color: #111111;
  color: #ffffff;
  font-weight: 600;
  font-size: 0.95rem;
  border: none;
  border-radius: 99px;
  height: 44px;
  padding: 0 1.5rem;
  display: inline-flex;
  align-items: center;
  gap: 0.5rem;
  transition: background-color 0.2s;
}

.btn-pill-submit:hover {
  background-color: #2d3748;
}

.filter-section-title {
  font-size: 0.85rem;
  text-transform: uppercase;
  letter-spacing: 0.05em;
  color: #64748b;
  font-weight: 700;
  margin-bottom: 1rem;
}

.card-filter {
  background: #ffffff;
  border-radius: 8px;
  border: 1px solid rgba(0, 0, 0, 0.05);
}

.custom-select-filter {
  background-color: #ffffff;
  border-color: rgba(0, 0, 0, 0.12);
  color: #1a1a1a;
}

.card-result {
  background: #ffffff;
  border-radius: 8px;
  border: 1px solid rgba(0, 0, 0, 0.05);
  transition: transform 0.2s, box-shadow 0.2s;
}

.card-result:hover {
  transform: translateY(-2px);
  box-shadow: 0 10px 20px rgba(0, 0, 0, 0.04);
}

.result-img-placeholder {
  width: 100%;
  height: 140px;
  background: linear-gradient(135deg, #0f2a41, #1e3a8a);
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #ffffff;
}

.form-check-input:checked {
  background-color: #c59d5f;
  border-color: #c59d5f;
}

.btn-dark {
  background-color: #1a1a1a;
  border: none;
  border-radius: 8px;
}

.btn-dark:hover {
  background-color: #2d3748;
}

.text-brand-orange {
  color: #d97706;
}

.text-brand-gold {
  color: #c49a3d;
}

@media (max-width: 767.98px) {
  .search-pill-container {
    flex-direction: column;
    border-radius: 16px;
    padding: 0.5rem;
  }

  .search-field-wrapper {
    width: 100%;
    padding: 0 0.5rem;
  }

  .search-pill-divider {
    display: none;
  }

  .btn-pill-submit {
    width: 100%;
    justify-content: center;
    margin-top: 0.5rem;
    border-radius: 8px;
  }
}
</style>