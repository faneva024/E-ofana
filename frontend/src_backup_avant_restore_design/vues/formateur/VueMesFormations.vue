<!-- vues/formateur/VueFormationsListe.vue -->
<template>
  <div>
    <!-- En-tête de section -->
    <div class="d-flex flex-column flex-sm-row justify-content-between align-items-start align-items-sm-center gap-3 mb-4">
      <div>
        <h1 class="h3 fw-bold text-dark mb-0">Mes formations</h1>
      </div>
      <!-- Redirection vers le formulaire de création -->
      <router-link :to="{ name: 'FormateurCreation' }" class="btn-add-formation shadow-sm text-decoration-none w-100 w-sm-auto justify-content-center">
        <svg fill="none" height="16" stroke="currentColor" stroke-linecap="round" stroke-linejoin="round" stroke-width="2.5" width="16" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24">
          <line x1="12" y1="5" x2="12" y2="19"></line>
          <line x1="5" y1="12" x2="19" y2="12"></line>
        </svg>
        <span>Nouvelle formation</span>
      </router-link>
    </div>

    <!-- Liste Table Card -->
    <div class="custom-card">
      <div class="table-responsive">
        <table class="table table-ofana">
          <thead>
            <tr>
              <th>Titre</th>
              <th>Date de début</th>
              <th>Places</th>
              <th>Statut</th>
              <th class="text-sm-center text-end">Actions</th>
            </tr>
          </thead>
          <tbody>
            <!-- Rendu dynamique des formations -->
            <tr v-for="formation in formations" :key="formation.id">
              <td data-label="Titre" class="fw-semibold text-dark">{{ formation.titre }}</td>
              <td data-label="Date de début" class="text-secondary">{{ formation.dateDebut }}</td>
              <td data-label="Places" class="text-secondary">{{ formation.placesOccupees }}/{{ formation.placesTotales }}</td>
              <td data-label="Statut">
                <span class="status-text" :class="getStatusClass(formation.statut)">
                  {{ formation.statut }}
                </span>
              </td>
              <td data-label="Actions" class="text-sm-center text-end actions-cell">
                <!-- Bouton Modifier (Redirige vers la route de modification avec l'ID) -->
                <router-link 
                  :to="{ name: 'FormateurModification', params: { id: formation.id } }" 
                  class="btn-action-edit me-1" 
                  title="Modifier"
                >
                  <svg fill="none" height="14" stroke="currentColor" stroke-linecap="round" stroke-linejoin="round" stroke-width="2" width="14" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24">
                    <path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"></path>
                    <path d="M18.5 2.5a2.121 2.121 0 1 1 3 3L12 15l-4 1 1-4 9.5-9.5z"></path>
                  </svg>
                </router-link>
                <!-- Bouton Supprimer -->
                <button @click="supprimerFormation(formation.id)" class="btn-action-delete" title="Supprimer">
                  <svg fill="none" height="14" stroke="currentColor" stroke-linecap="round" stroke-linejoin="round" stroke-width="2" width="14" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24">
                    <polyline points="3 6 5 6 21 6"></polyline>
                    <path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"></path>
                  </svg>
                </button>
              </td>
            </tr>
            <!-- Message si aucune formation n'est disponible -->
            <tr v-if="formations.length === 0">
              <td colspan="5" class="text-center text-secondary py-4 empty-message">Aucune formation trouvée.</td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'

// Liste réactive contenant les données du fichier HTML initial
const formations = ref([
  { id: 1, titre: 'Développement Web Full Stack', dateDebut: '15 Juil 2026', placesOccupees: 18, placesTotales: 30, statut: 'Validée' },
  { id: 2, titre: 'React.js Avancé', dateDebut: '1 Aoû 2026', placesOccupees: 12, placesTotales: 20, statut: 'En attente' },
  { id: 3, titre: 'Node.js & API REST', dateDebut: '15 Aoû 2026', placesOccupees: 0, placesTotales: 25, statut: 'Rejetée' },
  { id: 4, titre: 'Base de données PostgreSQL', dateDebut: '1 Sep 2026', placesOccupees: 5, placesTotales: 20, statut: 'Validée' }
])

// Retourne la bonne classe de couleur CSS Bootstrap selon le statut
const getStatusClass = (statut) => {
  if (statut === 'Validée') return 'status-valid'
  if (statut === 'En attente') return 'status-pending'
  if (statut === 'Rejetée') return 'status-rejected'
  return ''
}

// Action de suppression d'une formation
const supprimerFormation = (id) => {
  if (confirm('Êtes-vous sûr de vouloir supprimer cette formation ?')) {
    formations.value = formations.value.filter(f => f.id !== id)
  }
}
</script>

<style scoped>
/* Extraction stricte des styles du tableau depuis formations.html */
.custom-card {
  background: #ffffff;
  border: 1px solid #eef0f2;
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.01);
  overflow: hidden;
  padding: 1rem;
}

.table-ofana {
  margin-bottom: 0;
  vertical-align: middle;
}

.table-ofana th {
  background-color: #ffffff;
  font-weight: 700;
  color: #8a92a6;
  text-transform: uppercase;
  font-size: 0.75rem;
  letter-spacing: 0.05em;
  padding: 1rem 1.5rem;
  border-bottom: 1px solid #f1f3f5;
}

.table-ofana td {
  padding: 1.2rem 1.5rem;
  border-bottom: 1px solid #f1f3f5;
  font-size: 0.95rem;
}

/* Badges de Statuts */
.status-text {
  font-weight: 600;
  font-size: 0.9rem;
}
.status-valid { color: #2e7d32; }
.status-pending { color: #ed6c02; }
.status-rejected { color: #d32f2f; }

/* Boutons d'action */
.btn-action-edit {
  width: 34px;
  height: 34px;
  border-radius: 6px;
  border: 1px solid #e9ecef;
  background: #f8f9fa;
  color: #495057;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s;
}

.btn-action-edit:hover {
  background-color: #e9ecef;
  color: #1a1a1a;
}

.btn-action-delete {
  width: 34px;
  height: 34px;
  border-radius: 6px;
  border: none;
  background: #fdf2f2;
  color: #d32f2f;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s;
  cursor: pointer;
}

.btn-action-delete:hover {
  background-color: #fde8e8;
}

.btn-add-formation {
  background-color: #1a1a1a;
  color: white;
  font-weight: 600;
  font-size: 0.9rem;
  
  /* Modifie ici : 6px ou 8px en haut/bas pour qu'il soit moins épais */
  padding: 6px 16px; 
  
  border-radius: 8px;
  border: none;
  transition: background-color 0.2s;
  display: inline-flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
}

.btn-add-formation:hover {
  background-color: #2d2d2d;
  color: white;
}

/* --- Ajustements Responsifs (Mobile-First) --- */
@media (max-width: 575.98px) {
  /* Transformation du tableau en blocs empilés */
  .table-ofana thead {
    display: none; /* Masque les en-têtes de colonnes devenus inutiles */
  }

  .table-ofana tr {
    display: block;
    border: 1px solid #eef0f2;
    border-radius: 8px;
    margin-bottom: 1rem;
    padding: 0.5rem;
    background-color: #fbfbfb;
  }

  .table-ofana tr:last-child:not(:has(.empty-message)) {
    margin-bottom: 0;
  }

  .table-ofana td {
    display: flex;
    justify-content: space-between;
    align-items: center;
    text-align: right;
    padding: 0.75rem 0.5rem;
    border-bottom: 1px dashed #f1f3f5;
  }

  .table-ofana td:last-child {
    border-bottom: none;
  }

  /* Génération des labels dynamiques à gauche via l'attribut data-label */
  .table-ofana td::before {
    content: attr(data-label);
    font-weight: 700;
    color: #8a92a6;
    text-transform: uppercase;
    font-size: 0.75rem;
    letter-spacing: 0.05em;
    text-align: left;
  }

  /* Ajustement spécifique pour la ligne "Aucune formation trouvée" */
  .table-ofana td.empty-message {
    display: block;
    text-align: center;
  }
  .table-ofana td.empty-message::before {
    display: none;
  }

  .actions-cell {
    justify-content: flex-end !important;
  }
}

/* --- Ajustements Responsifs (Mobile-First) --- */
@media (max-width: 575.98px) {
  /* Annule le scroll horizontal forcé par Bootstrap pour laisser le tableau s'adapter */
  .table-responsive {
    overflow-x: visible !important;
  }

  /* Transformation du tableau en blocs empilés */
  .table-ofana thead {
    display: none; /* Masque les en-têtes de colonnes devenus inutiles */
  }

  .table-ofana tr {
    display: block;
    border: 1px solid #eef0f2;
    border-radius: 8px;
    margin-bottom: 1rem;
    padding: 0.5rem;
    background-color: #fbfbfb;
    width: 100%;
  }

  .table-ofana tr:last-child:not(:has(.empty-message)) {
    margin-bottom: 0;
  }

  .table-ofana td {
    display: flex;
    justify-content: space-between;
    align-items: center;
    text-align: right;
    padding: 0.75rem 0.5rem;
    border-bottom: 1px dashed #f1f3f5;
    width: 100%;
  }

  .table-ofana td:last-child {
    border-bottom: none;
  }

  /* Génération des labels dynamiques à gauche via l'attribut data-label */
  .table-ofana td::before {
    content: attr(data-label);
    font-weight: 700;
    color: #8a92a6;
    text-transform: uppercase;
    font-size: 0.75rem;
    letter-spacing: 0.05em;
    text-align: left;
    padding-right: 10px;
  }

  /* Ajustement spécifique pour la ligne "Aucune formation trouvée" */
  .table-ofana td.empty-message {
    display: block;
    text-align: center;
  }
  .table-ofana td.empty-message::before {
    display: none;
  }

  /* Aligne bien les boutons Modifier et Supprimer à droite */
  .actions-cell {
    display: flex !important;
    justify-content: space-between; 
    align-items: center;
  }
}
</style>