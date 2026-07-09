<!-- <script scoped>
import '../../styles/styleCarteF.css';
</script> -->
<template>
  <div class="container py-4">

    <!-- HERO -->
    <div class="text-center mb-4">
      <h1 class="fw-bold">Trouvez la formation qu'il vous faut</h1>

      <div class="input-group mt-3 w-75 mx-auto">
        <input
          v-model="search"
          type="text"
          class="form-control"
          placeholder="Rechercher une formation..."
        />
        <button class="btn btn-primary" @click="rechercher">
          Rechercher
        </button>
      </div>
    </div>

    <!-- LOADING -->
    <div v-if="loading" class="text-center">
      <div class="spinner-border text-primary"></div>
    </div>

    <!-- FORMATIONS -->
    <div class="row g-3" v-else>
      <div
        v-for="f in formationsFiltrees"
        :key="f.id"
        class="col-12 col-md-6 col-lg-4"
      >
        <CarteFormation :formation="f" @click="voirDetail(f.id)" />
      </div>

      <div v-if="formationsFiltrees.length === 0" class="text-center mt-4">
        Aucun résultat trouvé
      </div>
    </div>

</div>
</template>

<script setup>
import { ref, computed, onMounted } from "vue";
import { useRouter } from "vue-router";
import CarteFormation from "../../composants/CarteFormation.vue";
import { requeteAvecAuth } from "../../utilitaires";


const router = useRouter();

const formations = ref([]);
const search = ref("");
const loading = ref(false);


async function chargerFormations() {
  loading.value = true;

  try {
    const data = await requeteAvecAuth("/api/formations");
    formations.value = data;
  } catch (e) {
    console.error("Erreur chargement formations", e);
  } finally {
    loading.value = false;
  }
}

// async function chargerFormations() {
//   loading.value = true;

//   try {
//     // Simuler un délai pour le chargement
//     await new Promise(resolve => setTimeout(resolve, 1000));
//     formations.value = mockFormations;
//   } catch (e) {
//     console.error("Erreur chargement formations", e);
//   } finally {
//     loading.value = false;
//   }
// }


const formationsFiltrees = computed(() => {
  if (!search.value) return formations.value;

  return formations.value.filter(f =>
    f.titre?.toLowerCase().includes(search.value.toLowerCase())
  );
});

function rechercher() {
  // UX simple → filtre déjà réactif
}

function voirDetail(id) {
  router.push(`/formations/${id}`);
}

onMounted(() => {
  chargerFormations();
});
</script>