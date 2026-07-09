<template>
  <main class="container-xl py-5">
    <div v-if="chargement" class="alert alert-info">
      Chargement de la formation...
    </div>

    <div v-else-if="erreur" class="alert alert-danger">
      {{ erreur }}
    </div>

    <div v-else class="row g-4">
      <section class="col-lg-8">
        <div class="card shadow-sm border-0 overflow-hidden">
          <div class="detail-hero">
            <div class="detail-hero-overlay">
              <span class="badge bg-warning text-dark mb-3">
                {{ formation.categorie }}
              </span>

              <h1 class="display-6 fw-bold text-white">
                {{ formation.titre }}
              </h1>

              <p class="text-white-50 mb-0">
                {{ formation.centre }}
              </p>
            </div>
          </div>

          <div class="card-body p-4">
            <div class="row g-3 mb-4">
              <div class="col-6 col-md-3">
                <small class="text-muted d-block">Durée</small>
                <strong>{{ formation.duree }}</strong>
              </div>

              <div class="col-6 col-md-3">
                <small class="text-muted d-block">Lieu</small>
                <strong>{{ formation.lieu }}</strong>
              </div>

              <div class="col-6 col-md-3">
                <small class="text-muted d-block">Ville</small>
                <strong>{{ formation.ville }}</strong>
              </div>

              <div class="col-6 col-md-3">
                <small class="text-muted d-block">Avis</small>
                <strong>{{ formation.noteMoyenne || 0 }} / 5</strong>
              </div>
            </div>

            <h2 class="h5 fw-bold mb-3">Description</h2>
            <p class="text-muted">
              {{ formation.description || "Aucune description disponible." }}
            </p>
          </div>
        </div>
      </section>

      <aside class="col-lg-4">
        <div class="card shadow-sm border-0 sticky-card">
          <div class="card-body p-4">
            <h2 class="h5 fw-bold mb-4">Tarification</h2>

            <div v-if="formation.prixRemise" class="d-flex justify-content-between mb-2">
              <span class="text-muted">Prix public :</span>
              <span class="text-decoration-line-through">
                {{ formatPrix(formation.prix) }} Ar
              </span>
            </div>

            <div class="d-flex justify-content-between align-items-end mb-4">
              <span class="text-muted">Montant à payer :</span>
              <strong class="fs-3 text-gold">
                {{ formatPrix(montantFinal) }} Ar
              </strong>
            </div>

            <button
              class="btn btn-dark w-100 py-3 mb-3"
              :disabled="inscriptionEnCours || inscriptionCreee"
              @click="handleInscription"
            >
              <span v-if="inscriptionEnCours">Inscription...</span>
              <span v-else-if="inscriptionCreee">Inscription réussie</span>
              <span v-else>S'inscrire maintenant</span>
            </button>

            <div v-if="message" class="alert alert-success">
              {{ message }}
            </div>

            <div v-if="erreurAction" class="alert alert-danger">
              {{ erreurAction }}
            </div>

            <div v-if="inscriptionCreee" class="payment-box mt-4">
              <h3 class="h6 fw-bold mb-3">Paiement</h3>

              <label class="form-label">Opérateur</label>
              <select v-model="operateur" class="form-select mb-3">
                <option value="mvola">MVola</option>
                <option value="orange">Orange Money</option>
                <option value="airtel">Airtel Money</option>
              </select>

              <p class="mb-3">
                Montant :
                <strong>{{ formatPrix(montantFinal) }} Ar</strong>
              </p>

              <button
                class="btn btn-success w-100 mb-3"
                :disabled="paiementEnCours || paiementEffectue"
                @click="handlePaiement"
              >
                <span v-if="paiementEnCours">Paiement...</span>
                <span v-else-if="paiementEffectue">Paiement effectué</span>
                <span v-else>Payer maintenant</span>
              </button>

              <button
                v-if="paiementEffectue"
                class="btn btn-outline-dark w-100"
                @click="handleTelechargementPdf"
              >
                Télécharger le reçu PDF
              </button>
            </div>
          </div>
        </div>
      </aside>
    </div>
  </main>
</template>

<script setup>
import { computed, onMounted, ref } from "vue";
import { useRoute, useRouter } from "vue-router";

import { obtenirFormationParId } from "../../api/formations";
import { inscrireFormation } from "../../api/inscription";
import { payerInscription } from "../../api/paiements";
import { telechargerRecuPdf } from "../../api/recus";

const route = useRoute();
const router = useRouter();

const formation = ref(null);
const chargement = ref(false);
const erreur = ref("");

const inscriptionEnCours = ref(false);
const inscriptionCreee = ref(null);

const paiementEnCours = ref(false);
const paiementEffectue = ref(false);

const operateur = ref("mvola");
const message = ref("");
const erreurAction = ref("");

const montantFinal = computed(() => {
  if (!formation.value) {
    return 0;
  }

  return Number(formation.value.prixRemise || formation.value.prix || 0);
});

const getUtilisateurConnecte = () => {
  const rawAuth = localStorage.getItem("auth");
  const rawUtilisateur = localStorage.getItem("utilisateur");

  if (rawAuth) {
    try {
      const auth = JSON.parse(rawAuth);
      return auth.user;
    } catch (e) {
      console.error("auth invalide", e);
    }
  }

  if (rawUtilisateur) {
    try {
      return JSON.parse(rawUtilisateur);
    } catch (e) {
      console.error("utilisateur invalide", e);
    }
  }

  return null;
};

const chargerFormation = async () => {
  try {
    chargement.value = true;
    erreur.value = "";

    const id = route.params.id;
    formation.value = await obtenirFormationParId(id);
  } catch (e) {
    console.error("Erreur détail formation", e);
    erreur.value = "Impossible de charger cette formation.";
  } finally {
    chargement.value = false;
  }
};

const handleInscription = async () => {
  erreurAction.value = "";
  message.value = "";

  const utilisateur = getUtilisateurConnecte();

  if (!utilisateur?.idUser && !utilisateur?.id) {
    router.push("/connexion");
    return;
  }

  try {
    inscriptionEnCours.value = true;

    const response = await inscrireFormation({
      idUser: utilisateur.idUser || utilisateur.id,
      idFormation: formation.value.idFormation,
    });

    inscriptionCreee.value = response.data;
    message.value = response.data.message || "Inscription réussie";
  } catch (e) {
    console.error("Erreur inscription", e);
    erreurAction.value =
      e.response?.data?.message || "Erreur lors de l'inscription.";
  } finally {
    inscriptionEnCours.value = false;
  }
};

const handlePaiement = async () => {
  erreurAction.value = "";
  message.value = "";

  if (!inscriptionCreee.value?.idInscription) {
    erreurAction.value = "Aucune inscription trouvée pour le paiement.";
    return;
  }

  try {
    paiementEnCours.value = true;

    const response = await payerInscription({
      idInscription: inscriptionCreee.value.idInscription,
      montant: montantFinal.value,
      operateur: operateur.value,
    });

    paiementEffectue.value = true;
    message.value = response.data.message || "Paiement enregistré avec succès";
  } catch (e) {
    console.error("Erreur paiement", e);
    erreurAction.value =
      e.response?.data?.message || "Erreur lors du paiement.";
  } finally {
    paiementEnCours.value = false;
  }
};

const handleTelechargementPdf = async () => {
  if (!inscriptionCreee.value?.idInscription) {
    return;
  }

  await telechargerRecuPdf(inscriptionCreee.value.idInscription);
};

const formatPrix = (prix) => {
  return new Intl.NumberFormat("fr-FR").format(Number(prix || 0));
};

onMounted(() => {
  chargerFormation();
});
</script>

<style scoped>
.detail-hero {
  min-height: 330px;
  background: linear-gradient(135deg, #111111, #0f2a41);
  position: relative;
}

.detail-hero-overlay {
  position: absolute;
  inset: 0;
  padding: 40px;
  display: flex;
  flex-direction: column;
  justify-content: flex-end;
  background: linear-gradient(to top, rgba(0, 0, 0, 0.7), transparent);
}

.text-gold {
  color: #c59d5f;
}

.sticky-card {
  position: sticky;
  top: 20px;
}

.payment-box {
  border: 1px solid #e5e7eb;
  border-radius: 12px;
  padding: 18px;
  background: #fafafa;
}
</style>