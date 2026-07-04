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
              style="font-size: 60px; opacity: 0.5"
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
                {{ formatPrice(prixFinal) }} Ar
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

          <!-- BLOC PAIEMENT -->
          <div v-if="inscriptionCreee" class="card paiement-card mt-4 p-3">
            <h5 class="fw-bold mb-3">Paiement</h5>

            <div class="mb-3">
              <label class="form-label">Opérateur</label>
              <select v-model="operateurPaiement" class="form-select">
                <option value="mvola">Mvola</option>
                <option value="orange">Orange Money</option>
                <option value="airtel">Airtel Money</option>
              </select>
            </div>

            <p class="mb-2">
              Montant à payer :
              <strong>{{ formatPrice(prixFinal) }} Ar</strong>
            </p>

            <button
              class="btn btn-success px-4 py-2"
              @click="handlePaiement"
              :disabled="paiementLoading || paiementEffectue"
            >
              {{
                paiementLoading
                  ? "Paiement en cours..."
                  : paiementEffectue
                    ? "Paiement effectué"
                    : "Payer maintenant"
              }}
            </button>

            <div v-if="messagePaiement" class="alert alert-success mt-3">
              {{ messagePaiement }}
            </div>

            <div v-if="erreurPaiement" class="alert alert-danger mt-3">
              {{ erreurPaiement }}
            </div>

            <!-- BOUTON PDF -->
            <div v-if="paiementEffectue" class="mt-3">
              <button
                class="btn btn-outline-primary px-4 py-2"
                @click="handleTelechargerRecu"
                :disabled="recuLoading"
              >
                {{ recuLoading ? "Téléchargement..." : "Télécharger le reçu PDF" }}
              </button>
            </div>

            <div v-if="erreurRecu" class="alert alert-danger mt-3">
              {{ erreurRecu }}
            </div>
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
import { computed, onMounted, ref } from "vue";
import { useRoute, useRouter } from "vue-router";
import { getFormationById } from "../api/formations";
import { inscrireFormation, getSessionsOuvertes } from "../api/inscription";
import { payerInscription } from "../api/paiements";
import { telechargerRecuPdf } from "../api/recu";

const route = useRoute();
const router = useRouter();

const formation = ref(null);
const loading = ref(false);
const error = ref("");

const inscriptionLoading = ref(false);
const messageInscription = ref("");
const erreurInscription = ref("");
const inscriptionCreee = ref(null);

const paiementLoading = ref(false);
const messagePaiement = ref("");
const erreurPaiement = ref("");
const operateurPaiement = ref("mvola");
const paiementEffectue = ref(false);

const recuLoading = ref(false);
const erreurRecu = ref("");

const prixFinal = computed(() => {
  if (!formation.value) return 0;
  return formation.value.prixRemise || formation.value.prix || 0;
});

const chargerDetail = async () => {
  try {
    loading.value = true;
    error.value = "";

    const response = await getFormationById(route.params.id);
    formation.value = response.data;
  } catch (e) {
    console.error(e);
    error.value = "Impossible de charger le détail de la formation.";
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
  messagePaiement.value = "";
  erreurPaiement.value = "";
  erreurRecu.value = "";
  inscriptionCreee.value = null;
  paiementEffectue.value = false;

  const utilisateur = getUtilisateurConnecte();

  if (!utilisateur || !utilisateur.idUser) {
    erreurInscription.value =
      "Vous devez être connecté avant de vous inscrire.";
    router.push("/connexion");
    return;
  }

  try {
    inscriptionLoading.value = true;

    // Une formation peut avoir plusieurs sessions : on récupère les
    // sessions encore ouvertes et on prend la plus proche. L'API
    // n'accepte plus idFormation directement (ni idUser, lu depuis
    // le token JWT désormais).
    const sessionsResponse = await getSessionsOuvertes(route.params.id);
    const sessions = sessionsResponse.data;

    if (!sessions || sessions.length === 0) {
      erreurInscription.value =
        "Aucune session ouverte pour cette formation actuellement.";
      return;
    }

    const idSession = sessions[0].idSession;

    const response = await inscrireFormation(idSession);

    inscriptionCreee.value = response.data;

    messageInscription.value =
      "Inscription réussie. Vous pouvez maintenant payer.";
  } catch (e) {
    console.error("Erreur inscription :", e);

    if (e.response && e.response.data && e.response.data.message) {
      erreurInscription.value = e.response.data.message;
    } else {
      erreurInscription.value = "Impossible de faire l'inscription.";
    }
  } finally {
    inscriptionLoading.value = false;
  }
};

const handlePaiement = async () => {
  messagePaiement.value = "";
  erreurPaiement.value = "";
  erreurRecu.value = "";

  if (!inscriptionCreee.value || !inscriptionCreee.value.idInscription) {
    erreurPaiement.value = "Aucune inscription à payer.";
    return;
  }

  try {
    paiementLoading.value = true;

    const response = await payerInscription(inscriptionCreee.value.idInscription, {
      montant: Number(prixFinal.value),
      operateur: operateurPaiement.value,
    });

    messagePaiement.value =
      response.data.message || "Paiement enregistré avec succès.";

    paiementEffectue.value = true;
  } catch (e) {
    console.error("Erreur paiement :", e);

    if (e.response && e.response.data && e.response.data.message) {
      erreurPaiement.value = e.response.data.message;
    } else {
      erreurPaiement.value = "Impossible d'enregistrer le paiement.";
    }
  } finally {
    paiementLoading.value = false;
  }
};

const handleTelechargerRecu = async () => {
  erreurRecu.value = "";

  if (!inscriptionCreee.value || !inscriptionCreee.value.idInscription) {
    erreurRecu.value = "Aucune inscription disponible pour générer le reçu.";
    return;
  }

  try {
    recuLoading.value = true;

    const response = await telechargerRecuPdf(
      inscriptionCreee.value.idInscription
    );

    const blob = new Blob([response.data], {
      type: "application/pdf",
    });

    const url = window.URL.createObjectURL(blob);

    const link = document.createElement("a");
    link.href = url;
    link.download = `recu-inscription-${inscriptionCreee.value.idInscription}.pdf`;

    document.body.appendChild(link);
    link.click();

    document.body.removeChild(link);
    window.URL.revokeObjectURL(url);
  } catch (e) {
    console.error("Erreur téléchargement reçu :", e);
    erreurRecu.value = "Impossible de télécharger le reçu PDF.";
  } finally {
    recuLoading.value = false;
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

.paiement-card {
  border: 1px solid #e5e7eb;
  border-radius: 12px;
  background: #fafafa;
}
</style>