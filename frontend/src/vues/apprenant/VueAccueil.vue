<template>
  <div class="container py-4">
    <section class="text-center mb-5">
      <h1 class="fw-bold">Trouvez la formation qu'il vous faut</h1>

      <form class="input-group mt-3 mx-auto search-form" @submit.prevent="rechercher">
        <input
          v-model="search"
          type="text"
          class="form-control"
          placeholder="Rechercher une formation..."
          aria-label="Rechercher une formation"
        />
        <button class="btn btn-primary" type="submit">
          Rechercher
        </button>
      </form>
    </section>

    <section>
      <div class="d-flex align-items-center justify-content-between mb-3">
        <h2 class="h3 fw-bold mb-0">Formations suggérées</h2>
      </div>

      <div v-if="loading" class="text-center py-5">
        <div class="spinner-border text-primary" role="status">
          <span class="visually-hidden">Chargement...</span>
        </div>
      </div>

      <div v-else-if="erreur" class="alert alert-danger" role="alert">
        {{ erreur }}
      </div>

      <div v-else class="row g-4">
        <div
          v-for="formation in formationsFiltrees"
          :key="formation.id || formation._id"
          class="col-12 col-md-6 col-lg-4"
        >
          <CarteFormation
            :formation="formation"
            @selection="voirDetail"
          />
        </div>

        <div v-if="formationsFiltrees.length === 0" class="col-12 text-center text-muted py-5">
          Aucun résultat trouvé
        </div>
      </div>
    </section>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from "vue";
import { useRouter } from "vue-router";
import CarteFormation from "../../composants/CarteFormation.vue";
import { obtenirFormations } from "../../api/formations.js";

const router = useRouter();

const formations = ref([]);
const search = ref("");
const loading = ref(false);
const erreur = ref("");

async function chargerFormations() {
  loading.value = true;
  erreur.value = "";

  try {
    formations.value = await obtenirFormations();
  } catch (e) {
    console.error("Erreur chargement formations", e);
    erreur.value = "Impossible de charger les formations pour le moment.";
  } finally {
    loading.value = false;
  }
}

const formationsFiltrees = computed(() => {
  const terme = search.value.trim().toLowerCase();
  if (!terme) return formations.value;

  return formations.value.filter((formation) =>
    [
      formation.titre,
      formation.nom,
      formation.lieu,
      formation.ville,
      formation.categorie
    ].some((valeur) => valeur?.toLowerCase().includes(terme))
  );
});

function rechercher() {
  search.value = search.value.trim();
}

function voirDetail(formation) {
  const id = formation.id || formation._id;
  if (id) router.push(`/formations/${id}`);
}

onMounted(() => {
  chargerFormations();
});
</script>

<style scoped>
.search-form {
  max-width: 760px;
}
</style>
