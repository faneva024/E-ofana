<template>
  <div class="container-xl py-5">
    <button class="btn btn-outline-secondary mb-4" @click="retour">
      ← Retour
    </button>

    <div v-if="loading" class="alert alert-info">
      Chargement du détail...
    </div>

    <div v-else-if="error" class="alert alert-danger">
      {{ error }}
    </div>

    <div v-else-if="formation" class="card p-4">
      <div class="row g-4">
        <div class="col-md-4">
          <div class="detail-img-placeholder">
            <span
              class="material-symbols-outlined"
              style="font-size: 60px; opacity: 0.5;"
            >
              school
            </span>
          </div>
        </div>

        <div class="col-md-8">
          <span class="badge bg-light text-secondary mb-3">
            {{ formation.categorie || "Formation" }}
          </span>

          <h1 class="fw-bold mb-3">
            {{ formation.titre }}
          </h1>

          <p class="text-muted">
            {{ formation.description }}
          </p>

          <div class="row mt-4">
            <div class="col-md-6 mb-3">
              <strong>Centre :</strong>
              <p>{{ formation.centre || "Centre non défini" }}</p>
            </div>

            <div class="col-md-6 mb-3">
              <strong>Lieu :</strong>
              <p>{{ formation.ville || formation.lieu || "Lieu non défini" }}</p>
            </div>

            <div class="col-md-6 mb-3">
              <strong>Durée :</strong>
              <p>{{ formation.duree || "Durée non définie" }}</p>
            </div>

            <div class="col-md-6 mb-3">
              <strong>Prix :</strong>
              <p class="h4 text-brand-orange">
                {{ formatPrice(formation.prixRemise || formation.prix) }} Ar
              </p>
            </div>
          </div>

          <button
            class="btn btn-dark px-4 py-2 mt-3"
            @click="handleInscription"
            :disabled="inscriptionLoading"
          >
            {{ inscriptionLoading ? "Inscription en cours..." : "S'inscrire" }}
          </button>

          <div v-if="messageInscription" class="alert alert-success mt-3">
            {{ messageInscription }}
          </div>

          <div v-if="erreurInscription" class="alert alert-danger mt-3">
            {{ erreurInscription }}
          </div>
        </div>
      </div>
    </div>

    <div v-else class="alert alert-warning">
      Aucune formation trouvée.
    </div>
  </div>
</template>

<script setup>
import { onMounted, ref } from "vue";
import { useRoute, useRouter } from "vue-router";
import { getFormationById } from "../api/formations";
import { inscrireFormation } from "../api/inscription";

const route = useRoute();
const router = useRouter();

const formation = ref(null);
const loading = ref(false);
const error = ref("");

const inscriptionLoading = ref(false);
const messageInscription = ref("");
const erreurInscription = ref("");

const chargerDetail = async () => {
  try {
    loading.value = true;
    error.value = "";

    const response = await getFormationById(route.params.id);
    formation.value = response.data;

    console.log("Détail formation depuis la base :", formation.value);
  } catch (e) {
    console.error(e);
    error.value = "Impossible de charger le détail de la formation depuis la base.";
  } finally {
    loading.value = false;
  }
};

const formatPrice = (value) => {
  if (!value) return "0";
  return new Intl.NumberFormat("fr-FR").format(value);
};

const getUtilisateurConnecte = () => {
  const utilisateur = localStorage.getItem("utilisateur");

  if (!utilisateur) {
    return null;
  }

  try {
    return JSON.parse(utilisateur);
  } catch (error) {
    console.error("Utilisateur localStorage invalide :", error);
    return null;
  }
};

const handleInscription = async () => {
  messageInscription.value = "";
  erreurInscription.value = "";

  const utilisateur = getUtilisateurConnecte();

  if (!utilisateur || !utilisateur.idUser) {
    router.push({
      path: "/connexion",
      query: {
        formationId: route.params.id,
      },
    });
    return;
  }

  try {
    inscriptionLoading.value = true;

    const response = await inscrireFormation({
      idUser: utilisateur.idUser,
      idFormation: Number(route.params.id),
    });

    messageInscription.value =
      response.data.message || "Inscription réussie.";
  } catch (e) {
    console.error("Erreur inscription formation :", e);

    if (e.response && e.response.data && e.response.data.message) {
      erreurInscription.value = e.response.data.message;
    } else {
      erreurInscription.value = "Impossible de faire l'inscription.";
    }
  } finally {
    inscriptionLoading.value = false;
  }
};

const retour = () => {
  router.push("/formations");
};

onMounted(() => {
  chargerDetail();
});
</script>

<style scoped>
.detail-img-placeholder {
  width: 100%;
  min-height: 260px;
  background: linear-gradient(135deg, #0f2a41, #1e3a8a);
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #ffffff;
}

.text-brand-orange {
  color: #d97706;
}

.btn-dark {
  background-color: #1a1a1a;
  border: none;
  border-radius: 8px;
}

.btn-dark:disabled {
  opacity: 0.7;
  cursor: not-allowed;
}
</style>