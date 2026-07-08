<template>
  <div class="eo-form-card eo-card-wide mx-auto">

            <div class="text-center mb-4 eo-card-header">
              <div class="bg-eofana-dark d-inline-flex p-3 rounded-3 mb-3 shadow-sm eo-icon-bg">
                <svg style="color: #c69c50;" fill="currentColor" height="28" viewBox="0 0 16 16" width="28" xmlns="http://www.w3.org/2000/svg">
                  <path d="M14 1a1 1 0 0 1 1 1v12a1 1 0 0 1-1 1H2a1 1 0 0 1-1-1V2a1 1 0 0 1 1-1h12zM2 0a2 2 0 0 0-2 2v12a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V2a2 2 0 0 0-2-2H2z"/>
                  <path d="M8 4a.5.5 0 0 1 .5.5v3h3a.5.5 0 0 1 0 1h-3v3a.5.5 0 0 1-1 0v-3h-3a.5.5 0 0 1 0-1h3v-3A.5.5 0 0 1 8 4z"/>
                </svg>
              </div>
              <h1 class="h3 fw-bold text-dark mb-2 eo-card-title">
                {{ modeEdition ? 'Modifier la formation' : 'Nouvelle formation' }}
              </h1>
              <p class="text-muted small mb-0 eo-card-subtitle">
                {{ modeEdition ? 'Modifiez les informations de votre formation' : 'Créez une nouvelle formation à publier' }}
              </p>
            </div>

            <!-- Avertissement modification formation déjà approuvée -->
            <div v-if="modeEdition && formationOriginale.approuvee" class="alert alert-warning eo-alert d-flex align-items-start gap-2 mb-4" role="alert">
              <svg fill="currentColor" height="18" width="18" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 16 16" class="mt-0 flex-shrink-0">
                <path d="M8.982 1.566a1.13 1.13 0 0 0-1.96 0L.165 13.233c-.457.778.091 1.767.98 1.767h13.713c.889 0 1.438-.99.98-1.767L8.982 1.566zM8 5c.535 0 .954.462.9.995l-.35 3.507a.552.552 0 0 1-1.1 0L7.1 5.995A.905.905 0 0 1 8 5zm.002 6a1 1 0 1 1 0 2 1 1 0 0 1 0-2z"/>
              </svg>
              <div>
                <strong>Attention :</strong> Cette formation était déjà approuvée. Après modification, elle devra repasser en attente de validation par un modérateur.
              </div>
            </div>

            <!-- Erreur API -->
            <div v-if="apiError" class="alert alert-danger eo-alert d-flex align-items-center gap-2 mb-4" role="alert">
              <svg fill="currentColor" height="18" width="18" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 16 16">
                <path d="M8.982 1.566a1.13 1.13 0 0 0-1.96 0L.165 13.233c-.457.778.091 1.767.98 1.767h13.713c.889 0 1.438-.99.98-1.767L8.982 1.566zM8 5c.535 0 .954.462.9.995l-.35 3.507a.552.552 0 0 1-1.1 0L7.1 5.995A.905.905 0 0 1 8 5zm.002 6a1 1 0 1 1 0 2 1 1 0 0 1 0-2z"/>
              </svg>
              <span>{{ apiError }}</span>
            </div>

            <!-- Succès -->
            <div v-if="successMessage" class="alert alert-success eo-alert d-flex align-items-center gap-2 mb-4" role="alert">
              <svg fill="currentColor" height="18" width="18" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 16 16">
                <path d="M16 8A8 8 0 1 1 0 8a8 8 0 0 1 16 0zm-3.97-3.03a.75.75 0 0 0-1.08.022L7.477 9.417 5.384 7.323a.75.75 0 0 0-1.06 1.06L6.97 11.03a.75.75 0 0 0 1.079-.02l3.992-4.99a.75.75 0 0 0-.01-1.05z"/>
              </svg>
              <span>{{ successMessage }}</span>
            </div>

            <form @submit.prevent="handleSubmit" novalidate>

              <!-- Image de présentation -->
              <div class="mb-4 eo-form-group">
                <label class="form-label eo-form-label">Image de présentation</label>
                <div
                  class="eo-image-upload"
                  :class="{ 'eo-image-has-preview': imagePreview || form.image }"
                  @click="triggerImageUpload"
                >
                  <input
                    ref="fileInput"
                    type="file"
                    accept="image/*"
                    class="d-none"
                    @change="onImageSelected"
                  />
                  <div v-if="imagePreview || form.image" class="eo-image-preview">
                    <img :src="imagePreview || form.image" alt="Aperçu" />
                    <button type="button" class="eo-image-remove" @click.stop="removeImage" aria-label="Supprimer l'image">
                      <svg fill="currentColor" height="16" width="16" viewBox="0 0 16 16" xmlns="http://www.w3.org/2000/svg"><path d="M4.646 4.646a.5.5 0 0 1 .708 0L8 7.293l2.646-2.647a.5.5 0 0 1 .708.708L8.707 8l2.647 2.646a.5.5 0 0 1-.708.708L8 8.707l-2.646 2.647a.5.5 0 0 1-.708-.708L7.293 8 4.646 5.354a.5.5 0 0 1 0-.708z"/></svg>
                    </button>
                  </div>
                  <div v-else class="eo-image-placeholder">
                    <svg fill="none" height="32" stroke="#9ca3af" stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" width="32" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
                      <rect x="3" y="3" width="18" height="18" rx="2" ry="2"/><circle cx="8.5" cy="8.5" r="1.5"/><polyline points="21 15 16 10 5 21"/>
                    </svg>
                    <span class="text-muted small">Cliquez pour ajouter une image</span>
                  </div>
                </div>
                <div v-if="errors.image" class="eo-invalid-feedback d-block">{{ errors.image }}</div>
              </div>

              <div class="row g-3">
                <!-- Titre -->
                <div class="col-12">
                  <div class="mb-4 eo-form-group">
                    <label class="form-label eo-form-label" for="titre">Titre de la formation *</label>
                    <input
                      class="form-control eo-form-control"
                      :class="{ 'is-invalid': errors.titre }"
                      id="titre"
                      v-model="form.titre"
                      @blur="validateField('titre')"
                      placeholder="Ex: Vue.js 3 Masterclass"
                      type="text"
                      required
                    />
                    <div class="invalid-feedback eo-invalid-feedback" v-if="errors.titre">{{ errors.titre }}</div>
                  </div>
                </div>

                <!-- Description -->
                <div class="col-12">
                  <div class="mb-4 eo-form-group">
                    <label class="form-label eo-form-label" for="description">Description *</label>
                    <textarea
                      class="form-control eo-form-control eo-textarea"
                      :class="{ 'is-invalid': errors.description }"
                      id="description"
                      v-model="form.description"
                      @blur="validateField('description')"
                      placeholder="Décrivez le contenu et les objectifs de la formation..."
                      rows="5"
                      required
                    ></textarea>
                    <div class="invalid-feedback eo-invalid-feedback" v-if="errors.description">{{ errors.description }}</div>
                  </div>
                </div>

                <!-- Catégorie -->
                <div class="col-12 col-md-6">
                  <div class="mb-4 eo-form-group">
                    <label class="form-label eo-form-label" for="categorie">Catégorie *</label>
                    <select
                      class="form-select eo-form-control"
                      :class="{ 'is-invalid': errors.categorie }"
                      id="categorie"
                      v-model="form.categorie"
                      @blur="validateField('categorie')"
                      required
                    >
                      <option value="" disabled>Sélectionnez une catégorie</option>
                      <option value="Développement">Développement</option>
                      <option value="Design">Design</option>
                      <option value="Marketing">Marketing</option>
                      <option value="Gestion">Gestion</option>
                      <option value="Comptabilité">Comptabilité</option>
                      <option value="Agriculture">Agriculture</option>
                      <option value="Langues">Langues</option>
                      <option value="Autre">Autre</option>
                    </select>
                    <div class="invalid-feedback eo-invalid-feedback" v-if="errors.categorie">{{ errors.categorie }}</div>
                  </div>
                </div>

                <!-- Durée -->
                <div class="col-12 col-md-6">
                  <div class="mb-4 eo-form-group">
                    <label class="form-label eo-form-label" for="duree">Durée *</label>
                    <input
                      class="form-control eo-form-control"
                      :class="{ 'is-invalid': errors.duree }"
                      id="duree"
                      v-model="form.duree"
                      @blur="validateField('duree')"
                      placeholder="Ex: 20h"
                      type="text"
                      required
                    />
                    <div class="invalid-feedback eo-invalid-feedback" v-if="errors.duree">{{ errors.duree }}</div>
                  </div>
                </div>

                <!-- Lieu -->
                <div class="col-12 col-md-6">
                  <div class="mb-4 eo-form-group">
                    <label class="form-label eo-form-label" for="lieu">Lieu *</label>
                    <input
                      class="form-control eo-form-control"
                      :class="{ 'is-invalid': errors.lieu }"
                      id="lieu"
                      v-model="form.lieu"
                      @blur="validateField('lieu')"
                      placeholder="Ex: Antananarivo"
                      type="text"
                      required
                    />
                    <div class="invalid-feedback eo-invalid-feedback" v-if="errors.lieu">{{ errors.lieu }}</div>
                  </div>
                </div>

                <!-- Nombre de places -->
                <div class="col-12 col-md-6">
                  <div class="mb-4 eo-form-group">
                    <label class="form-label eo-form-label" for="nombrePlaces">Nombre de places disponibles *</label>
                    <input
                      class="form-control eo-form-control"
                      :class="{ 'is-invalid': errors.nombrePlaces }"
                      id="nombrePlaces"
                      v-model.number="form.nombrePlaces"
                      @blur="validateField('nombrePlaces')"
                      placeholder="Ex: 30"
                      type="number"
                      min="1"
                      required
                    />
                    <div class="invalid-feedback eo-invalid-feedback" v-if="errors.nombrePlaces">{{ errors.nombrePlaces }}</div>
                  </div>
                </div>

                <!-- Date de début -->
                <div class="col-12 col-md-6">
                  <div class="mb-4 eo-form-group">
                    <label class="form-label eo-form-label" for="dateDebut">Date de début *</label>
                    <input
                      class="form-control eo-form-control"
                      :class="{ 'is-invalid': errors.dateDebut }"
                      id="dateDebut"
                      v-model="form.dateDebut"
                      @blur="validateField('dateDebut')"
                      type="date"
                      required
                    />
                    <div class="invalid-feedback eo-invalid-feedback" v-if="errors.dateDebut">{{ errors.dateDebut }}</div>
                  </div>
                </div>

                <!-- Date limite d'inscription -->
                <div class="col-12 col-md-6">
                  <div class="mb-4 eo-form-group">
                    <label class="form-label eo-form-label" for="dateLimite">Date limite d'inscription *</label>
                    <input
                      class="form-control eo-form-control"
                      :class="{ 'is-invalid': errors.dateLimite }"
                      id="dateLimite"
                      v-model="form.dateLimite"
                      @blur="validateField('dateLimite')"
                      type="date"
                      required
                    />
                    <div class="invalid-feedback eo-invalid-feedback" v-if="errors.dateLimite">{{ errors.dateLimite }}</div>
                  </div>
                </div>

                <!-- Prix normal -->
                <div class="col-12 col-md-6">
                  <div class="mb-4 eo-form-group">
                    <label class="form-label eo-form-label" for="prix">Prix normal (Ar) *</label>
                    <input
                      class="form-control eo-form-control"
                      :class="{ 'is-invalid': errors.prix }"
                      id="prix"
                      v-model.number="form.prix"
                      @blur="validateField('prix')"
                      placeholder="Ex: 150000"
                      type="number"
                      min="0"
                      required
                    />
                    <div class="invalid-feedback eo-invalid-feedback" v-if="errors.prix">{{ errors.prix }}</div>
                  </div>
                </div>

                <!-- Prix remisé (optionnel) -->
                <div class="col-12 col-md-6">
                  <div class="mb-4 eo-form-group">
                    <label class="form-label eo-form-label" for="prixRemise">Prix remisé (optionnel)</label>
                    <input
                      class="form-control eo-form-control"
                      :class="{ 'is-invalid': errors.prixRemise }"
                      id="prixRemise"
                      v-model.number="form.prixRemise"
                      @blur="validateField('prixRemise')"
                      placeholder="Ex: 120000"
                      type="number"
                      min="0"
                    />
                    <div class="invalid-feedback eo-invalid-feedback" v-if="errors.prixRemise">{{ errors.prixRemise }}</div>
                    <div class="form-text text-muted small">Laissez vide si aucun prix remisé.</div>
                  </div>
                </div>
              </div>

              <!-- Message d'information modération -->
              <div class="alert alert-info eo-alert d-flex align-items-start gap-2 mb-4 mt-2" role="alert">
                <svg fill="currentColor" height="18" width="18" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 16 16" class="mt-0 flex-shrink-0">
                  <path d="M8 16A8 8 0 1 0 8 0a8 8 0 0 0 0 16zm.93-9.412-1 4.705c-.07.34.029.533.304.533.194 0 .487-.07.686-.246l-.088.416c-.287.346-.92.598-1.465.598-.703 0-1.002-.422-.808-1.319l.738-3.468c.064-.293.006-.399-.287-.47l-.451-.081.082-.381 2.29-.287zM8 5.5a1 1 0 1 1 0-2 1 1 0 0 1 0 2z"/>
                </svg>
                <div>
                  <strong>Information :</strong> Votre formation sera visible après approbation du modérateur.
                </div>
              </div>

              <!-- Boutons -->
              <div class="d-flex gap-3">
                <button
                  type="button"
                  class="btn btn-outline-dark flex-fill d-flex align-items-center justify-content-center gap-2"
                  @click="annuler"
                >
                  <span>Annuler</span>
                </button>
                <button
                  type="submit"
                  class="btn btn-eofana-dark flex-fill d-flex align-items-center justify-content-center gap-2"
                  :disabled="submitting"
                >
                  <span v-if="submitting" class="spinner-border spinner-border-sm me-2" role="status" aria-hidden="true"></span>
                  <span v-else>Soumettre pour validation</span>
                  <svg v-if="!submitting" fill="none" height="16" stroke="currentColor" stroke-linecap="round" stroke-linejoin="round" stroke-width="2" width="16" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24"><polyline points="9 18 15 12 9 6"></polyline></svg>
                </button>
              </div>

            </form>
          </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import api from '../../api/axios'

const router = useRouter()
const route = useRoute()

const modeEdition = computed(() => !!route.params.id)
const formationId = computed(() => route.params.id || null)

const fileInput = ref(null)
const imagePreview = ref(null)
const imageFile = ref(null)
const submitting = ref(false)
const apiError = ref('')
const successMessage = ref('')
const formationOriginale = ref({})

const form = reactive({
  titre: '',
  description: '',
  categorie: '',
  duree: '',
  lieu: '',
  dateDebut: '',
  dateLimite: '',
  nombrePlaces: '',
  prix: '',
  prixRemise: '',
  image: ''
})

const errors = reactive({
  titre: '',
  description: '',
  categorie: '',
  duree: '',
  lieu: '',
  dateDebut: '',
  dateLimite: '',
  nombrePlaces: '',
  prix: '',
  prixRemise: '',
  image: ''
})

const validateField = (field) => {
  switch (field) {
    case 'titre':
      errors.titre = form.titre ? '' : 'Le titre est obligatoire.'
      break
    case 'description':
      errors.description = form.description ? '' : 'La description est obligatoire.'
      break
    case 'categorie':
      errors.categorie = form.categorie ? '' : 'La catégorie est obligatoire.'
      break
    case 'duree':
      errors.duree = form.duree ? '' : 'La durée est obligatoire.'
      break
    case 'lieu':
      errors.lieu = form.lieu ? '' : 'Le lieu est obligatoire.'
      break
    case 'dateDebut':
      errors.dateDebut = form.dateDebut ? '' : 'La date de début est obligatoire.'
      break
    case 'dateLimite':
      errors.dateLimite = form.dateLimite ? '' : 'La date limite est obligatoire.'
      break
    case 'nombrePlaces':
      errors.nombrePlaces = form.nombrePlaces > 0 ? '' : 'Le nombre de places doit être supérieur à 0.'
      break
    case 'prix':
      errors.prix = form.prix >= 0 ? '' : 'Le prix est obligatoire.'
      break
    case 'prixRemise':
      if (form.prixRemise !== '' && form.prixRemise !== null) {
        errors.prixRemise = form.prixRemise >= 0 ? '' : 'Le prix remisé ne peut pas être négatif.'
      } else {
        errors.prixRemise = ''
      }
      break
    case 'image':
      errors.image = ''
      break
  }
}

const validateForm = () => {
  validateField('titre')
  validateField('description')
  validateField('categorie')
  validateField('duree')
  validateField('lieu')
  validateField('dateDebut')
  validateField('dateLimite')
  validateField('nombrePlaces')
  validateField('prix')
  return !Object.values(errors).some(error => error)
}

const triggerImageUpload = () => {
  fileInput.value?.click()
}

const onImageSelected = (event) => {
  const file = event.target.files[0]
  if (!file) return

  const validTypes = ['image/jpeg', 'image/png', 'image/gif', 'image/webp']
  if (!validTypes.includes(file.type)) {
    errors.image = 'Format non supporté. Utilisez JPG, PNG, GIF ou WebP.'
    return
  }
  if (file.size > 5 * 1024 * 1024) {
    errors.image = 'L\'image ne doit pas dépasser 5 Mo.'
    return
  }

  errors.image = ''
  imageFile.value = file
  const reader = new FileReader()
  reader.onload = (e) => {
    imagePreview.value = e.target.result
  }
  reader.readAsDataURL(file)
}

const removeImage = () => {
  imageFile.value = null
  imagePreview.value = null
  form.image = ''
  if (fileInput.value) fileInput.value.value = ''
}

const annuler = () => {
  router.push({ name: 'FormateurTableauDeBord' })
}

const handleSubmit = async () => {
  if (!validateForm()) return

  apiError.value = ''
  successMessage.value = ''
  submitting.value = true

  try {
    const formData = new FormData()
    formData.append('titre', form.titre)
    formData.append('description', form.description)
    formData.append('categorie', form.categorie)
    formData.append('duree', form.duree)
    formData.append('lieu', form.lieu)
    formData.append('dateDebut', form.dateDebut)
    formData.append('dateLimiteInscription', form.dateLimite)
    formData.append('nombrePlaces', form.nombrePlaces)
    formData.append('prix', form.prix)
    if (form.prixRemise !== '' && form.prixRemise !== null) {
      formData.append('prixRemise', form.prixRemise)
    }
    if (imageFile.value) {
      formData.append('image', imageFile.value)
    }

    if (modeEdition.value) {
      await api.put(`/v1/formations/${formationId.value}`, formData, {
        headers: { 'Content-Type': 'multipart/form-data' }
      })
      successMessage.value = 'Formation modifiée avec succès. Elle sera visible après approbation du modérateur.'
    } else {
      await api.post('/v1/formations', formData, {
        headers: { 'Content-Type': 'multipart/form-data' }
      })
      successMessage.value = 'Formation créée avec succès. Elle sera visible après approbation du modérateur.'
    }

    setTimeout(() => {
      router.push({ name: 'FormateurTableauDeBord' })
    }, 2000)
  } catch (err) {
    const message = err.response?.data?.message
    if (message) {
      apiError.value = message
    } else if (err.message) {
      apiError.value = err.message
    } else {
      apiError.value = 'Une erreur est survenue. Veuillez réessayer.'
    }
  } finally {
    submitting.value = false
  }
}

onMounted(async () => {
  if (modeEdition.value) {
    try {
      const response = await api.get(`/v1/formations/${formationId.value}`)
      const data = response.data

      formationOriginale.value = data
      form.titre = data.titre || ''
      form.description = data.description || ''
      form.categorie = data.categorie || ''
      form.duree = data.duree || ''
      form.lieu = data.lieu || ''
      form.dateDebut = data.dateDebut || ''
      form.dateLimite = data.dateLimiteInscription || data.dateLimite || ''
      form.nombrePlaces = data.nombrePlaces || ''
      form.prix = data.prix || ''
      form.prixRemise = data.prixRemise || ''
      form.image = data.image || ''
    } catch (err) {
      apiError.value = 'Impossible de charger les données de la formation.'
    }
  }
})
</script>

<style scoped>
.eo-form-card {
  background: #ffffff;
  border-radius: 1rem;
  border: 1px solid #e0e0e0;
  padding: 3rem;
  max-width: 512px;
  width: 100%;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.05);
}

.eo-card-wide {
  max-width: 720px;
}

.eo-card-title {
  color: #1a1a1a;
}

.eo-icon-bg {
  background-color: #1a1a1a !important;
}

.eo-form-group {
  margin-bottom: 0 !important;
}

.eo-form-control {
  height: 48px;
  border-radius: 8px;
  border-color: #dee2e6;
}

.eo-form-control:focus {
  border-color: #c69c50;
  box-shadow: 0 0 0 0.2rem rgba(198, 156, 80, 0.25);
}

.eo-textarea {
  height: auto;
  min-height: 120px;
  padding-top: 12px;
}

.eo-form-label {
  font-weight: 600;
  font-size: 0.875rem;
  color: #333333;
  margin-bottom: 0.5rem;
}

.btn-eofana-dark {
  background-color: #1a1a1a;
  color: #ffffff;
  font-weight: 600;
  border-radius: 8px;
  border: none;
  padding: 0.75rem 1.5rem;
  transition: background-color 0.2s;
}

.btn-eofana-dark:hover {
  background-color: #333333;
  color: #ffffff;
}

.btn-eofana-dark:disabled {
  opacity: 0.65;
  cursor: not-allowed;
}

.btn-outline-dark {
  border-radius: 8px;
  font-weight: 600;
  padding: 0.75rem 1.5rem;
  border-color: #dee2e6;
  color: #333333;
}

.btn-outline-dark:hover {
  background-color: #f5f5f5;
  border-color: #c69c50;
  color: #1a1a1a;
}

.eo-alert {
  border-radius: 8px;
  font-size: 0.9rem;
}

.eo-invalid-feedback {
  font-size: 0.8rem;
  margin-top: 0.25rem;
}

/* Image upload */
.eo-image-upload {
  border: 2px dashed #dee2e6;
  border-radius: 8px;
  padding: 2rem;
  text-align: center;
  cursor: pointer;
  transition: border-color 0.2s, background 0.2s;
  position: relative;
}

.eo-image-upload:hover {
  border-color: #c69c50;
  background: rgba(198, 156, 80, 0.04);
}

.eo-image-has-preview {
  border-style: solid;
  padding: 0.5rem;
  cursor: default;
}

.eo-image-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 0.5rem;
}

.eo-image-preview {
  position: relative;
  display: inline-block;
  width: 100%;
  max-height: 250px;
  overflow: hidden;
  border-radius: 6px;
}

.eo-image-preview img {
  width: 100%;
  height: auto;
  max-height: 250px;
  object-fit: cover;
  display: block;
}

.eo-image-remove {
  position: absolute;
  top: 0.5rem;
  right: 0.5rem;
  background: rgba(0, 0, 0, 0.6);
  border: none;
  border-radius: 50%;
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #ffffff;
  cursor: pointer;
  transition: background 0.2s;
}

.eo-image-remove:hover {
  background: rgba(220, 53, 69, 0.8);
}

@media (max-width: 768px) {
  .eo-form-card {
    padding: 1.5rem;
  }
}
</style>
