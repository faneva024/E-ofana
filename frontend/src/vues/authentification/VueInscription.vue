<template>
  <div class="inscription-container">
    <div class="inscription-header">
      <div class="header-content">
        <div class="logo-section">
          <i class="bi bi-mortarboard-fill logo-icon"></i>
          <h1 class="eo-title">E-OFANA</h1>
          <p class="eo-subtitle">Créez votre compte pour commencer</p>
        </div>
      </div>
    </div>

    <div class="inscription-form-wrapper">
      <div class="form-card">
        <h2 class="form-title">Inscription</h2>

        <div v-if="erreur" class="alert alert-danger" role="alert">
          <i class="bi bi-exclamation-triangle-fill me-2"></i>
          {{ erreur }}
        </div>

        <div v-if="success" class="alert alert-success" role="alert">
          <i class="bi bi-check-circle-fill me-2"></i>
          {{ success }}
        </div>

        <form @submit.prevent="handleSubmit" novalidate>
          <div class="form-section">
            <h3 class="section-title">
              <i class="bi bi-person-fill me-2"></i>
              Informations personnelles
            </h3>

            <div class="row">
              <div class="col-md-6 mb-3">
                <label for="nom" class="form-label">Nom *</label>
                <div class="input-group">
                  <span class="input-group-text">
                    <i class="bi bi-person"></i>
                  </span>
                  <input
                    type="text"
                    class="form-control"
                    :class="{ 'is-invalid': errors.nom }"
                    id="nom"
                    v-model="form.nom"
                    @blur="validateField('nom')"
                    placeholder="Votre nom"
                  />
                  <div class="invalid-feedback" v-if="errors.nom">
                    {{ errors.nom }}
                  </div>
                </div>
              </div>

              <div class="col-md-6 mb-3">
                <label for="prenom" class="form-label">Prénom *</label>
                <div class="input-group">
                  <span class="input-group-text">
                    <i class="bi bi-person"></i>
                  </span>
                  <input
                    type="text"
                    class="form-control"
                    :class="{ 'is-invalid': errors.prenom }"
                    id="prenom"
                    v-model="form.prenom"
                    @blur="validateField('prenom')"
                    placeholder="Votre prénom"
                  />
                  <div class="invalid-feedback" v-if="errors.prenom">
                    {{ errors.prenom }}
                  </div>
                </div>
              </div>
            </div>

            <div class="mb-3">
              <label for="email" class="form-label">Email *</label>
              <div class="input-group">
                <span class="input-group-text">
                  <i class="bi bi-envelope-fill"></i>
                </span>
                <input
                  type="email"
                  class="form-control"
                  :class="{ 'is-invalid': errors.email }"
                  id="email"
                  v-model="form.email"
                  @blur="validateField('email')"
                  placeholder="votre@email.com"
                />
                <div class="invalid-feedback" v-if="errors.email">
                  {{ errors.email }}
                </div>
              </div>
            </div>

            <div class="mb-3">
              <label for="telephone" class="form-label">Téléphone</label>
              <div class="input-group">
                <span class="input-group-text">
                  <i class="bi bi-telephone-fill"></i>
                </span>
                <input
                  type="tel"
                  class="form-control"
                  :class="{ 'is-invalid': errors.telephone }"
                  id="telephone"
                  v-model="form.telephone"
                  @blur="validateField('telephone')"
                  placeholder="+261 34 00 000 00"
                />
                <div class="invalid-feedback" v-if="errors.telephone">
                  {{ errors.telephone }}
                </div>
              </div>
            </div>
          </div>

          <div class="form-section">
            <h3 class="section-title">
              <i class="bi bi-shield-lock-fill me-2"></i>
              Mot de passe
            </h3>

            <div class="mb-3">
              <label for="password" class="form-label">Mot de passe *</label>
              <div class="input-group">
                <span class="input-group-text">
                  <i class="bi bi-key-fill"></i>
                </span>
                <input
                  :type="showPassword ? 'text' : 'password'"
                  class="form-control"
                  :class="{ 'is-invalid': errors.password }"
                  id="password"
                  v-model="form.password"
                  @blur="validateField('password')"
                  placeholder="Minimum 8 caractères"
                />
                <button
                  type="button"
                  class="btn btn-outline-secondary"
                  @click="showPassword = !showPassword"
                >
                  <i :class="showPassword ? 'bi bi-eye-slash-fill' : 'bi bi-eye-fill'"></i>
                </button>
                <div class="invalid-feedback" v-if="errors.password">
                  {{ errors.password }}
                </div>
              </div>

              <div class="password-strength mt-2">
                <div class="progress" style="height: 5px;">
                  <div
                    class="progress-bar"
                    :class="passwordStrengthClass"
                    :style="{ width: passwordStrength + '%' }"
                  ></div>
                </div>
                <small class="text-muted">{{ passwordStrengthText }}</small>
              </div>
            </div>

            <div class="mb-3">
              <label for="passwordConfirm" class="form-label">
                Confirmer le mot de passe *
              </label>
              <div class="input-group">
                <span class="input-group-text">
                  <i class="bi bi-key-fill"></i>
                </span>
                <input
                  :type="showPasswordConfirm ? 'text' : 'password'"
                  class="form-control"
                  :class="{ 'is-invalid': errors.passwordConfirm }"
                  id="passwordConfirm"
                  v-model="form.passwordConfirm"
                  @blur="validateField('passwordConfirm')"
                  placeholder="Confirmez votre mot de passe"
                />
                <button
                  type="button"
                  class="btn btn-outline-secondary"
                  @click="showPasswordConfirm = !showPasswordConfirm"
                >
                  <i :class="showPasswordConfirm ? 'bi bi-eye-slash-fill' : 'bi bi-eye-fill'"></i>
                </button>
                <div class="invalid-feedback" v-if="errors.passwordConfirm">
                  {{ errors.passwordConfirm }}
                </div>
              </div>
            </div>
          </div>

          <div class="form-section">
            <div class="form-check mb-3">
              <input
                type="checkbox"
                class="form-check-input"
                :class="{ 'is-invalid': errors.conditions }"
                id="conditions"
                v-model="form.conditions"
                @change="validateField('conditions')"
              />
              <label class="form-check-label" for="conditions">
                J'accepte les
                <a href="#" class="link-primary">conditions d'utilisation</a>
                et la
                <a href="#" class="link-primary">politique de confidentialité</a>
                *
              </label>
              <div class="invalid-feedback d-block" v-if="errors.conditions">
                {{ errors.conditions }}
              </div>
            </div>
          </div>

          <div class="form-actions">
            <button
              type="submit"
              class="btn btn-primary btn-lg w-100"
              :disabled="loading"
            >
              <span v-if="loading" class="spinner-border spinner-border-sm me-2"></span>
              <i v-else class="bi bi-person-plus-fill me-2"></i>
              {{ loading ? "Inscription en cours..." : "S'inscrire" }}
            </button>
          </div>

          <div class="form-footer">
            <p class="text-center mb-0">
              Déjà inscrit ?
              <router-link
                :to="{ path: '/connexion', query: { formationId: route.query.formationId } }"
                class="link-primary fw-bold"
              >
                Connectez-vous
              </router-link>
            </p>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed } from "vue";
import { useRouter, useRoute } from "vue-router";
import { creerUtilisateur } from "../../api/utilisateurs.js";

const router = useRouter();
const route = useRoute();

const form = reactive({
  nom: "",
  prenom: "",
  email: "",
  telephone: "",
  password: "",
  passwordConfirm: "",
  conditions: false,
});

const errors = reactive({});
const showPassword = ref(false);
const showPasswordConfirm = ref(false);
const loading = ref(false);
const erreur = ref("");
const success = ref("");

const passwordStrength = computed(() => {
  const password = form.password;
  let strength = 0;

  if (password.length >= 8) strength += 25;
  if (password.match(/[a-z]/)) strength += 25;
  if (password.match(/[A-Z]/)) strength += 25;
  if (password.match(/[0-9]/) || password.match(/[^a-zA-Z0-9]/)) strength += 25;

  return strength;
});

const passwordStrengthClass = computed(() => {
  const strength = passwordStrength.value;

  if (strength <= 25) return "bg-danger";
  if (strength <= 50) return "bg-warning";
  if (strength <= 75) return "bg-info";

  return "bg-success";
});

const passwordStrengthText = computed(() => {
  const strength = passwordStrength.value;

  if (strength <= 25) return "Très faible";
  if (strength <= 50) return "Faible";
  if (strength <= 75) return "Moyen";

  return "Fort";
});

const validateField = (field) => {
  switch (field) {
    case "nom":
      errors.nom = form.nom.trim() ? "" : "Le nom est requis";
      break;

    case "prenom":
      errors.prenom = form.prenom.trim() ? "" : "Le prénom est requis";
      break;

    case "email": {
      const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
      errors.email = emailRegex.test(form.email) ? "" : "Email invalide";
      break;
    }

    case "telephone":
      if (form.telephone) {
        const phoneRegex = /^\+?[\d\s-]{10,}$/;
        errors.telephone = phoneRegex.test(form.telephone)
          ? ""
          : "Numéro de téléphone invalide";
      } else {
        errors.telephone = "";
      }
      break;

    case "password":
      if (form.password.length < 8) {
        errors.password = "Le mot de passe doit contenir au moins 8 caractères";
      } else {
        errors.password = "";
      }

      if (form.passwordConfirm) {
        validateField("passwordConfirm");
      }
      break;

    case "passwordConfirm":
      errors.passwordConfirm =
        form.password === form.passwordConfirm
          ? ""
          : "Les mots de passe ne correspondent pas";
      break;

    case "conditions":
      errors.conditions = form.conditions
        ? ""
        : "Vous devez accepter les conditions";
      break;

    default:
      break;
  }
};

const validateForm = () => {
  validateField("nom");
  validateField("prenom");
  validateField("email");
  validateField("telephone");
  validateField("password");
  validateField("passwordConfirm");
  validateField("conditions");

  return !Object.values(errors).some((error) => error);
};

const resetForm = () => {
  form.nom = "";
  form.prenom = "";
  form.email = "";
  form.telephone = "";
  form.password = "";
  form.passwordConfirm = "";
  form.conditions = false;
};

const handleSubmit = async () => {
  if (!validateForm()) return;

  loading.value = true;
  erreur.value = "";
  success.value = "";

  try {
    const userData = {
      nom: form.nom,
      prenom: form.prenom,
      email: form.email,
      telephone: form.telephone,
      motDePasse: form.password,
    };

    await creerUtilisateur(userData);

    success.value = "Compte créé avec succès.";
    resetForm();

    setTimeout(() => {
      router.push({
        path: "/connexion",
        query: {
          formationId: route.query.formationId,
        },
      });
    }, 800);
  } catch (e) {
    console.error("Erreur création utilisateur :", e);

    if (e.response && e.response.data && e.response.data.message) {
      erreur.value = e.response.data.message;
    } else {
      erreur.value = "Impossible de créer le compte. Vérifiez les informations.";
    }
  } finally {
    loading.value = false;
  }
};
</script>

<style scoped>
.inscription-container {
  min-height: 100vh;
  background: linear-gradient(135deg, var(--eo-gray-50) 0%, var(--eo-primary-light) 100%);
}

.inscription-header {
  background: var(--eo-primary);
  padding: var(--eo-spacing-xl) 0;
  text-align: center;
  color: white;
}

.header-content {
  max-width: 600px;
  margin: 0 auto;
  padding: 0 var(--eo-spacing-md);
}

.logo-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: var(--eo-spacing-sm);
}

.logo-icon {
  font-size: 3rem;
  color: white;
}

.eo-title {
  font-size: var(--eo-font-size-4xl);
  font-weight: 800;
  margin: 0;
  font-family: var(--eo-font-family);
}

.eo-subtitle {
  font-size: var(--eo-font-size-lg);
  margin: 0;
  opacity: 0.9;
}

.inscription-form-wrapper {
  padding: var(--eo-spacing-2xl) var(--eo-spacing-md);
  display: flex;
  justify-content: center;
}

.form-card {
  background: white;
  border-radius: var(--eo-radius-2xl);
  box-shadow: var(--eo-shadow-xl);
  padding: var(--eo-spacing-2xl);
  max-width: 600px;
  width: 100%;
}

.form-title {
  color: var(--eo-primary);
  font-size: var(--eo-font-size-3xl);
  font-weight: 700;
  margin-bottom: var(--eo-spacing-xl);
  text-align: center;
  font-family: var(--eo-font-family);
}

.form-section {
  margin-bottom: var(--eo-spacing-xl);
  padding-bottom: var(--eo-spacing-lg);
  border-bottom: 1px solid var(--eo-gray-200);
}

.form-section:last-of-type {
  border-bottom: none;
}

.section-title {
  color: var(--eo-gray-700);
  font-size: var(--eo-font-size-lg);
  font-weight: 600;
  margin-bottom: var(--eo-spacing-lg);
  font-family: var(--eo-font-family);
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

.form-label {
  font-weight: 500;
  color: var(--eo-gray-700);
  margin-bottom: var(--eo-spacing-sm);
}

.password-strength {
  margin-top: var(--eo-spacing-sm);
}

.form-actions {
  margin-top: var(--eo-spacing-xl);
}

.btn-primary {
  background-color: var(--eo-primary);
  border-color: var(--eo-primary);
  padding: var(--eo-spacing-md) var(--eo-spacing-xl);
  font-weight: 600;
  border-radius: var(--eo-radius-lg);
  transition: all var(--eo-transition-base);
}

.btn-primary:hover:not(:disabled) {
  background-color: var(--eo-primary-dark);
  border-color: var(--eo-primary-dark);
  transform: translateY(-2px);
  box-shadow: var(--eo-shadow-lg);
}

.btn-primary:disabled {
  opacity: 0.7;
}

.form-footer {
  margin-top: var(--eo-spacing-lg);
  text-align: center;
  padding-top: var(--eo-spacing-lg);
  border-top: 1px solid var(--eo-gray-200);
}

.alert {
  border-radius: var(--eo-radius-lg);
  margin-bottom: var(--eo-spacing-lg);
}

.alert-danger {
  background-color: var(--eo-danger-light);
  border-color: var(--eo-danger);
  color: var(--eo-danger);
}

.alert-success {
  background-color: #d1e7dd;
  border-color: #badbcc;
  color: #0f5132;
}

@media (max-width: 768px) {
  .form-card {
    padding: var(--eo-spacing-lg);
  }

  .eo-title {
    font-size: var(--eo-font-size-3xl);
  }

  .inscription-form-wrapper {
    padding: var(--eo-spacing-lg) var(--eo-spacing-sm);
  }
}
</style>