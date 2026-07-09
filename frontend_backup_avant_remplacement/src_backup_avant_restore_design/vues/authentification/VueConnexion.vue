<template>
  <div class="connexion-container">
    <div class="connexion-header">
      <div class="header-content">
        <div class="logo-section">
          <i class="bi bi-mortarboard-fill logo-icon"></i>
          <h1 class="eo-title">E-OFANA</h1>
          <p class="eo-subtitle">Connectez-vous à votre espace</p>
        </div>
      </div>
    </div>

    <div class="connexion-form-wrapper">
      <div class="form-card">
        <h2 class="form-title">Connexion</h2>
        
        <div v-if="erreur" class="alert alert-danger" role="alert">
          <i class="bi bi-exclamation-triangle-fill me-2"></i>
          {{ erreur }}
        </div>

        <div v-if="success" class="alert alert-success" role="alert">
           <i class="bi bi-check-circle-fill me-2"></i>
          {{ success }}
        </div>

        <form @submit.prevent="handleSubmit" novalidate>
          <div class="mb-4">
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
                required
              />
              <div class="invalid-feedback" v-if="errors.email">
                {{ errors.email }}
              </div>
            </div>
          </div>

          <div class="mb-4">
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
                placeholder="Votre mot de passe"
                required
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
          </div>

          <div class="mb-4">
            <div class="form-check">
              <input
                type="checkbox"
                class="form-check-input"
                id="rememberMe"
                v-model="form.rememberMe"
              />
              <label class="form-check-label" for="rememberMe">
                Se souvenir de moi
              </label>
            </div>
          </div>

          <div class="mb-4">
            <router-link to="/mot-de-passe-oublie" class="link-primary">
              Mot de passe oublié ?
            </router-link>
          </div>

          <div class="form-actions">
            <button
              type="submit"
               class="btn btn-primary btn-lg w-100"
                :disabled="loading"
                >
              <span v-if="loading" class="spinner-border spinner-border-sm me-2"></span>
              <i v-else class="bi bi-box-arrow-in-right me-2"></i>
              {{ loading ? "Connexion en cours..." : "Se connecter" }}
            </button>
          </div>

          <div class="form-footer">
            <p class="text-center mb-0">
              Pas encore de compte ? 
              <router-link to="/inscription" class="link-primary fw-bold">
                Inscrivez-vous
              </router-link>
            </p>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from "vue";
import { useRouter, useRoute } from "vue-router";
import { connexion } from "../../api/auth";

const router = useRouter();
const route = useRoute();

const form = reactive({
  email: "",
  password: "",
  rememberMe: false,
});

const errors = reactive({});
const showPassword = ref(false);
const loading = ref(false);
const erreur = ref("");
const success = ref("");

const validateField = (field) => {
  switch (field) {
    case "email": {
      const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
      errors.email = emailRegex.test(form.email) ? "" : "Email invalide";
      break;
    }

    case "password":
      errors.password = form.password ? "" : "Le mot de passe est requis";
      break;

    default:
      break;
  }
};

const validateForm = () => {
  validateField("email");
  validateField("password");

  return !Object.values(errors).some((error) => error);
};

const handleSubmit = async () => {
  if (!validateForm()) return;

  loading.value = true;
  erreur.value = "";
  success.value = "";

  try {
    const response = await connexion({
      email: form.email,
      motDePasse: form.password,
    });

    const data = response.data;

    localStorage.setItem("token", data.token);
    localStorage.setItem(
      "utilisateur",
      JSON.stringify({
        idUser: data.idUser,
        nom: data.nom,
        prenom: data.prenom,
        email: data.email,
        telephone: data.telephone,
        role: data.role,
      })
    );

    success.value = "Connexion réussie.";

    setTimeout(() => {
      if (route.query.formationId) {
        router.push(`/formations/${route.query.formationId}`);
      } else {
        router.push("/mon-espace");
      }
    }, 600);
  } catch (e) {
    console.error("Erreur connexion :", e);

    if (e.response && e.response.data && e.response.data.message) {
      erreur.value = e.response.data.message;
    } else {
      erreur.value = "Erreur de connexion";
    }
  } finally {
    loading.value = false;
  }
};
</script>

<style scoped>
.connexion-container {
  min-height: 100vh;
  background: white;
}

.connexion-header {
  background: white;
  padding: var(--eo-spacing-xl) 0;
  text-align: center;
  color: var(--eo-gray-800);
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
  color: var(--eo-primary);
}

.eo-title {
  font-size: var(--eo-font-size-4xl);
  font-weight: 800;
  margin: 0;
  font-family: var(--eo-font-family);
  color: var(--eo-primary);
}

.eo-subtitle {
  font-size: var(--eo-font-size-lg);
  margin: 0;
  color: var(--eo-gray-600);
}

.connexion-form-wrapper {
  padding: var(--eo-spacing-2xl) var(--eo-spacing-md);
  display: flex;
  justify-content: center;
}

.form-card {
  background: white;
  border-radius: var(--eo-radius-2xl);
  box-shadow: var(--eo-shadow-xl);
  padding: var(--eo-spacing-2xl);
  max-width: 450px;
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

.form-check-input:checked {
  background-color: var(--eo-primary);
  border-color: var(--eo-primary);
}

.form-check-input:focus {
  box-shadow: 0 0 0 0.2rem rgba(0, 102, 204, 0.25);
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

@media (max-width: 768px) {
  .form-card {
    padding: var(--eo-spacing-lg);
  }
  
  .eo-title {
    font-size: var(--eo-font-size-3xl);
  }
  
  .connexion-form-wrapper {
    padding: var(--eo-spacing-lg) var(--eo-spacing-sm);
  }
}
</style>
