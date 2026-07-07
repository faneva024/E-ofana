<template>

  <!-- Section Hero -->
  <section class="hero-section text-center text-white">
    <div class="container">
      <div class="d-inline-block badge-top mb-4">
        🎓 La plateforme #1 de formations à Madagascar
      </div>

      <h1 class="hero-title mb-3 mx-auto">
        Développez vos compétences avec <br>
        <span class="text-primary-custom">E-OFANA</span>
      </h1>

      <p class="text-white text-opacity-75 mb-5 mx-auto fs-5 description-text">
        Trouvez la formation qui correspond à vos ambitions parmi des centaines d'offres dans tout Madagascar.
      </p>

      <!-- Barre de recherche -->
      <div class="search-container-pill mb-5">
        <div class="flex-grow-1 d-flex align-items-center style-input-wrapper">
          <div class="search-wrapper-field w-100">
            <span class="material-symbols-outlined search-icon-inside">search</span>
            <input v-model="searchQuery" class="search-clean-input" placeholder="Formation, compétence, domaine..."
              type="text">
          </div>
        </div>

        <div class="search-inline-divider"></div>

        <div class="d-flex align-items-center style-select-wrapper select-min-width">
          <div class="search-wrapper-field w-100">
            <span class="material-symbols-outlined search-icon-inside">location_on</span>
            <select v-model="selectedCity" class="form-select search-clean-select">
              <option value="">Toutes les villes</option>
              <option v-for="city in cities" :key="city" :value="city">{{ city }}</option>
            </select>
          </div>
        </div>

        <button class="btn-search-pill-submit" @click="handleSearch">
          <span class="material-symbols-outlined button-search-icon">search</span>
          <span>Rechercher</span>
        </button>
      </div>

      <!-- Tags Défilants -->
      <div class="no-scrollbar pb-2" data-purpose="category-scroll">
        <a v-for="category in categories" :key="category.name" href="#" class="category-tag">
          <span class="material-symbols-outlined tag-icon">sell</span>
          {{ category.name }}
        </a>
      </div>
    </div>
  </section>

  <!-- Section Statistiques -->
  <section class="stats-bar">
    <div class="container px-4">
      <div class="row text-center g-4 g-md-0">
        <div class="col-6 col-md-3 border-end border-dark border-opacity-10">
          <div class="stat-number">500+</div>
          <div class="stat-label">Formations</div>
        </div>
        <div class="col-6 col-md-3 border-end-md border-dark border-opacity-10">
          <div class="stat-number">120+</div>
          <div class="stat-label">Centres partenaires</div>
        </div>
        <div class="col-6 col-md-3 border-end border-dark border-opacity-10">
          <div class="stat-number">15 000+</div>
          <div class="stat-label">Apprenants</div>
        </div>
        <div class="col-6 col-md-3">
          <div class="stat-number">18</div>
          <div class="stat-label">Régions couvertes</div>
        </div>
      </div>
    </div>
  </section>

  <!-- Section Formations en Vedette -->
  <section class="py-5 container px-4">
    <div class="d-flex justify-content-between align-items-end mb-4">
      <div>
        <h2 class="h3 fw-bold text-dark m-0">Formations en vedette</h2>
        <p class="text-muted small mb-0 mt-1">Les opportunités les plus demandées du moment</p>
      </div>
      <RouterLink
        to="/formations"
        class="text-primary-custom fw-bold text-decoration-none d-flex align-items-center gap-1 small"
        >
         Voir toutes <span class="material-symbols-outlined all-courses-icon">arrow_forward</span>
      </RouterLink>
    </div>

    <!-- Liste de Cartes Dynamique -->
    <div v-if="loading" class="text-center py-5">
  Chargement des formations...
</div>

<div v-else-if="error" class="alert alert-danger">
  {{ error }}
</div>

<div v-else-if="courses.length === 0" class="alert alert-info">
  Aucune formation disponible pour le moment.
</div>

<div v-else class="row g-4">
      <div v-for="course in courses" :key="course.id" class="col-12 col-md-6 col-lg-4">
        <div class="course-card">
          <div :class="['course-card-img', course.gradientClass, 'text-white']">
            <span class="course-badge">{{ course.category }}</span>
            <span class="material-symbols-outlined course-bg-icon">{{ course.icon }}</span>
          </div>
          <div class="p-4 d-flex flex-column flex-grow-1">
            <h3 class="course-title">{{ course.title }}</h3>
            <p class="small text-muted mb-3">{{ course.school }}</p>

            <div class="d-flex flex-wrap gap-2 mb-4 mt-auto">
              <span class="badge bg-light text-dark d-flex align-items-center gap-1 py-2 px-2 badge-custom">
                <span class="material-symbols-outlined text-muted badge-icon">location_on</span> {{ course.location }}
              </span>
              <span class="badge bg-light text-dark d-flex align-items-center gap-1 py-2 px-2 badge-custom">
                <span class="material-symbols-outlined text-muted badge-icon">schedule</span> {{ course.duration }}
              </span>
            </div>

            <div class="pt-3 border-top d-flex justify-content-between align-items-center mb-3">
              <div>
                <span class="h5 fw-bold text-primary-custom mb-0">{{ formatPrice(course.price) }} Ar</span>
              </div>
              <div class="d-flex align-items-center gap-1 text-warning fw-bold small">
                <span class="material-symbols-outlined star-icon">star</span> {{ course.rating }}
              </div>
            </div>

            <button class="btn-card-action">Découvrir la formation</button>
          </div>
        </div>
      </div>
    </div>
  </section>

</template>

<script setup>
import { ref, onMounted } from 'vue';
import { getFormations } from '../api/formations';

// États pour la recherche
const searchQuery = ref('');
const selectedCity = ref('');

const cities = ref(['Antananarivo', 'Toamasina', 'Antsirabe', 'Fianarantsoa']);

const categories = ref([
  { name: 'Informatique' },
  { name: 'Marketing' },
  { name: 'Finance' },
  { name: 'Management' },
  { name: 'Agriculture' },
  { name: 'Artisanat' }
]);

// Formations récupérées depuis le backend
const courses = ref([]);
const loading = ref(false);
const error = ref("");

const getIconByCategory = (category) => {
  const icons = {
    Informatique: "code",
    Marketing: "campaign",
    Finance: "payments",
    Management: "groups",
    Agriculture: "potted_plant",
    Artisanat: "construction"
  };

  return icons[category] || "school";
};

const getGradientByCategory = (category) => {
  const gradients = {
    Informatique: "img-gradient-blue",
    Marketing: "img-gradient-purple",
    Agriculture: "img-gradient-green",
    Finance: "img-gradient-purple",
    Management: "img-gradient-blue",
    Artisanat: "img-gradient-green"
  };

  return gradients[category] || "img-gradient-blue";
};

const chargerFormations = async () => {
  try {
    loading.value = true;
    error.value = "";

    const response = await getFormations();

    console.log("Formations backend :", response.data);

    courses.value = response.data.map((formation) => ({
      id: formation.idFormation,
      title: formation.titre,
      school: formation.centre || "Centre non défini",
      category: formation.categorie || "Formation",
      icon: getIconByCategory(formation.categorie),
      gradientClass: getGradientByCategory(formation.categorie),
      location: formation.ville || formation.lieu || "Lieu non défini",
      duration: formation.duree || "Durée non définie",
      price: formation.prixRemise || formation.prix || 0,
      rating: formation.noteMoyenne || 0
    }));
  } catch (err) {
    console.error(err);
    error.value = "Erreur lors du chargement des formations.";
  } finally {
    loading.value = false;
  }
};

onMounted(() => {
  chargerFormations();
});

const handleSearch = () => {
  console.log('Recherche lancée:', searchQuery.value, selectedCity.value);
};

// Formater l'affichage des prix (ex: 150000 -> 150 000)
const formatPrice = (value) => {
  return new Intl.NumberFormat('fr-FR').format(value);
};
</script>

<style scoped>
.text-primary-custom {
  color: #c59d5f !important;
}

/* Section Hero */
.hero-section {
  background-color: #1f1f1f;
  padding: 6rem 1rem 5rem 1rem;
}

.badge-top {
  background: rgba(197, 157, 95, 0.08);
  border: 1px solid rgba(197, 157, 95, 0.25);
  color: #c59d5f;
  border-radius: 50px;
  padding: 0.4rem 1.25rem;
  font-size: 0.85rem;
  font-weight: 500;
  letter-spacing: 0.02em;
}

.hero-title {
  font-weight: 900;
  font-size: 3.25rem;
  line-height: 1.2;
  letter-spacing: -0.02em;
  max-width: 850px;
}

.description-text {
  max-width: 750px;
  font-weight: 400;
}

/* Barre de recherche */
.search-container-pill {
  background-color: #ffffff;
  border-radius: 99px;
  padding: 0.35rem;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.15);
  max-width: 820px;
  margin: 0 auto;
  display: flex;
  align-items: center;
}

.search-wrapper-field {
  display: flex;
  align-items: center;
  padding: 0 1.25rem;
  height: 48px;
}

.search-icon-inside {
  color: #94a3b8;
  margin-right: 0.75rem;
  font-size: 22px;
  flex-shrink: 0;
  user-select: none;
}

.search-clean-input {
  border: none;
  width: 100%;
  font-size: 0.95rem;
  color: #1e293b;
  background: transparent;
  font-weight: 400;
}

.search-clean-input:focus {
  outline: none;
}

.search-clean-input::placeholder {
  color: #94a3b8;
}

.search-inline-divider {
  width: 1px;
  height: 28px;
  background-color: #e2e8f0;
  flex-shrink: 0;
}

.select-min-width {
  min-width: 200px;
}

.search-clean-select {
  border: none;
  background: transparent;
  font-size: 0.95rem;
  color: #1e293b;
  font-weight: 500;
  width: 100%;
  cursor: pointer;
  padding-right: 1.5rem;
}

.search-clean-select:focus {
  outline: none;
  box-shadow: none;
}

.btn-search-pill-submit {
  background-color: #111111;
  color: #ffffff;
  font-weight: 600;
  font-size: 0.95rem;
  border: none;
  border-radius: 99px;
  height: 48px;
  padding: 0 1.75rem;
  display: inline-flex;
  align-items: center;
  gap: 0.5rem;
  transition: background-color 0.2s;
  margin-left: auto;
}

.btn-search-pill-submit:hover {
  background-color: #2d3748;
}

.button-search-icon {
  font-size: 20px;
}

/* Tags de catégories défilants */
.no-scrollbar {
  overflow-x: auto;
  white-space: nowrap;
  -ms-overflow-style: none;
  scrollbar-width: none;
}

.no-scrollbar::-webkit-scrollbar {
  display: none;
}

.category-tag {
  background: rgba(255, 255, 255, 0.08);
  border: 1px solid rgba(255, 255, 255, 0.15);
  color: #e0e0e0;
  border-radius: 50px;
  padding: 0.45rem 1.25rem;
  font-size: 0.85rem;
  text-decoration: none;
  display: inline-flex;
  align-items: center;
  gap: 0.4rem;
  margin: 0 0.25rem;
  transition: all 0.2s;
}

.category-tag:hover {
  background: rgba(255, 255, 255, 0.15);
  color: #ffffff;
  border-color: rgba(255, 255, 255, 0.3);
}

.tag-icon {
  font-size: 16px;
}

/* Section Statistiques */
.stats-bar {
  background-color: #c59d5f;
  padding: 2.25rem 0;
  color: #1f1f1f;
}

.stat-number {
  font-weight: 900;
  font-size: 2.5rem;
  line-height: 1;
  margin-bottom: 0.25rem;
}

.stat-label {
  font-size: 0.85rem;
  font-weight: 500;
  opacity: 0.8;
}

/* Cartes des formations */
.course-card {
  border-radius: 16px;
  overflow: hidden;
  border: 1px solid rgba(0, 0, 0, 0.05);
  background: #ffffff;
  height: 100%;
  display: flex;
  flex-direction: column;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.02);
  transition: transform 0.3s ease, box-shadow 0.3s ease;
}

.course-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 12px 30px rgba(0, 0, 0, 0.08);
}

.course-card-img {
  height: 160px;
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
}

.img-gradient-blue {
  background: linear-gradient(135deg, #1e3a8a, #3b82f6);
}

.img-gradient-purple {
  background: linear-gradient(135deg, #5b21b6, #a855f7);
}

.img-gradient-green {
  background: linear-gradient(135deg, #065f46, #10b981);
}

.course-badge {
  position: absolute;
  top: 1rem;
  left: 1rem;
  background: rgba(255, 255, 255, 0.2);
  backdrop-filter: blur(8px);
  color: #ffffff;
  font-size: 0.75rem;
  font-weight: 600;
  padding: 0.35rem 0.85rem;
  border-radius: 50px;
  letter-spacing: 0.03em;
}

.course-bg-icon {
  font-size: 48px;
  opacity: 0.3;
}

.course-title {
  font-size: 1.2rem;
  font-weight: 700;
  line-height: 1.4;
  color: #1f1f1f;
  margin-bottom: 0.25rem;
}

.badge-custom {
  font-size: 0.75rem;
  font-weight: 500;
}

.badge-icon {
  font-size: 14px;
}

.all-courses-icon {
  font-size: 16px;
}

.star-icon {
  font-size: 16px;
}

.btn-card-action {
  border: 1px solid #e2e8f0;
  background: transparent;
  color: #4a5568;
  font-size: 0.85rem;
  font-weight: 600;
  padding: 0.5rem 1rem;
  border-radius: 8px;
  transition: all 0.2s;
  width: 100%;
}

.btn-card-action:hover {
  background-color: #1f1f1f;
  color: #ffffff;
  border-color: #1f1f1f;
}

/* Responsive Mobile */
@media (max-width: 767.98px) {
  .search-container-pill {
    flex-direction: column;
    border-radius: 20px;
    padding: 0.75rem;
    gap: 0.5rem;
  }

  .search-wrapper-field {
    width: 100%;
    border-bottom: 1px solid #f1f5f9;
    padding: 0 0.5rem;
  }

  .search-inline-divider {
    display: none;
  }

  .btn-search-pill-submit {
    width: 100%;
    justify-content: center;
    margin-top: 0.5rem;
    border-radius: 12px;
  }

  .border-end-md {
    border-right: none !important;
  }
}
</style>