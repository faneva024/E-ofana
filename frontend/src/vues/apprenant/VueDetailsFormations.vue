<template>
  <main class="container-xl pb-5 vue-main-content">
    <!-- BREADCRUMBS -->
    <nav aria-label="breadcrumb" class="py-4">
      <ol class="breadcrumb mb-0" style="font-size: 0.75rem;">
        <li v-for="(crumb, index) in breadcrumbs" :key="index" class="breadcrumb-item" :class="{ active: crumb.active }">
          <a v-if="!crumb.active" href="#" @click.prevent>{{ crumb.text }}</a>
          <span v-else class="text-secondary">{{ crumb.text }}</span>
        </li>
      </ol>
    </nav>

    <div class="row g-4">
      <!-- LEFT COLUMN: Content -->
      <div class="col-lg-8">
        <!-- SECTION PRINCIPALE -->
        <section class="mb-4">
          <div class="hero-section p-0 overflow-hidden card-custom">
            <!-- Image grande (responsive) -->
            <div class="ratio ratio-21x9 bg-dark position-relative">
              <img 
                src="https://images.unsplash.com/photo-1517694712202-14dd9538aa97?q=80&w=1200&auto=format&fit=crop" 
                alt="Développement Web Full Stack" 
                class="img-fluid object-fit-cover opacity-50"
              />
              <div class="position-absolute bottom-0 start-0 w-100 p-4 text-white gradient-overlay">
                <span class="tag mb-2">{{ course.category }}</span>
                <h1 class="display-6 fw-bold mb-2">{{ course.title }}</h1>
                
                <!-- Étoiles notation (placeholder Phase 2) -->
                <div class="d-flex align-items-center gap-2 small">
                  <span class="text-warning">★ {{ course.rating }}</span>
                  <span class="opacity-75">({{ course.reviewsCount }} avis — Placeholder Phase 2)</span>
                </div>
              </div>
            </div>

            <!-- Détails de la formation -->
            <div class="p-4 bg-white border-top">
              <div class="row g-3 style-details">
                <div class="col-6 col-md-3 d-flex align-items-center gap-2">
                  <span class="material-symbols-outlined text-primary-custom">schedule</span>
                  <div>
                    <small class="text-muted d-block">Durée</small>
                    <strong>{{ course.duration }}</strong>
                  </div>
                </div>
                <div class="col-6 col-md-3 d-flex align-items-center gap-2">
                  <span class="material-symbols-outlined text-primary-custom">location_on</span>
                  <div>
                    <small class="text-muted d-block">Lieu exact</small>
                    <strong>{{ course.location }}</strong>
                  </div>
                </div>
                <div class="col-6 col-md-3 d-flex align-items-center gap-2">
                  <span class="material-symbols-outlined text-primary-custom">calendar_today</span>
                  <div>
                    <small class="text-muted d-block">Date début</small>
                    <strong>{{ course.startDate }}</strong>
                  </div>
                </div>
                <div class="col-6 col-md-3 d-flex align-items-center gap-2">
                  <span class="material-symbols-outlined text-primary-custom">event_busy</span>
                  <div>
                    <small class="text-muted d-block">Limite inscription</small>
                    <strong>{{ course.deadlineDate }}</strong>
                  </div>
                </div>
              </div>

              <!-- Nombre de places restantes (Avertissement si < 5) -->
              <div class="mt-4">
                <div v-if="course.spotsLeft < 5" class="alert alert-danger d-flex align-items-center gap-2 mb-0" role="alert">
                  <span class="material-symbols-outlined">warning</span>
                  <div><strong>Attention !</strong> Il ne reste plus que {{ course.spotsLeft }} places disponibles pour cette session.</div>
                </div>
                <div v-else class="alert alert-success d-flex align-items-center gap-2 mb-0" role="alert">
                  <span class="material-symbols-outlined">group</span>
                  <div>{{ course.spotsLeft }} places restantes disponibles.</div>
                </div>
              </div>

            </div>
          </div>
        </section>

        <!-- DESCRIPTION LONGUE -->
        <section class="mb-4">
          <div class="card card-custom p-4">
            <h2 class="h5 fw-bold mb-4">Description de la formation</h2>
            <div class="text-secondary" style="line-height: 1.6;">
              <p v-for="(paragraph, idx) in course.description" :key="idx" :class="{ 'mb-0': idx === course.description.length - 1 }">
                {{ paragraph }}
              </p>
            </div>
          </div>
        </section>

        <!-- SECTION FORMATEUR (Info du centre) -->
        <section class="mb-4">
          <div class="card card-custom p-4">
            <h2 class="h5 fw-bold mb-4">Centre de formation</h2>
            <div class="d-flex align-items-center justify-content-between flex-wrap gap-3">
              <div class="d-flex align-items-center gap-3">
                <!-- Logo du centre -->
                <div class="bg-light d-flex align-items-center justify-content-center rounded" style="width: 60px; height: 60px;">
                  <span class="material-symbols-outlined text-secondary" style="font-size: 32px;">corporate_fare</span>
                </div>
                <div>
                  <h3 class="h6 fw-bold mb-1">{{ center.name }}</h3>
                  <p class="small text-muted mb-0 d-flex align-items-center gap-1">
                    <span class="material-symbols-outlined" style="font-size: 14px;">distance</span>
                    {{ center.location }}
                  </p>
                </div>
              </div>
              <a href="#" class="btn btn-outline-secondary btn-sm d-flex align-items-center gap-1" @click.prevent="contactCenter">
                <span class="material-symbols-outlined" style="font-size: 16px;">mail</span>
                Contacter le centre
              </a>
            </div>
          </div>
        </section>
        
      </div>

      <!-- RIGHT COLUMN: Section Prix (Encadré Bootstrap Card) -->
      <aside class="col-lg-4">
        <div class="sticky-price-bar card card-custom p-4">
          <h2 class="h6 text-uppercase fw-bold text-muted tracking-wider mb-4">Tarification</h2>
          
          <div class="price-container mb-4">
            <!-- Prix de base si remise existante -->
            <div v-if="course.basePrice" class="d-flex justify-content-between align-items-center mb-1">
              <span class="text-muted small">Prix public conseillé :</span>
              <span class="text-decoration-line-through text-muted">{{ formatPrice(course.basePrice) }} Ar</span>
            </div>

            <!-- Prix remisé par le centre -->
            <div v-if="course.basePrice" class="d-flex justify-content-between align-items-center mb-2">
              <span class="text-muted small">Prix remisé centre :</span>
              <span class="fw-semibold text-dark">{{ formatPrice(course.centerPrice) }} Ar</span>
            </div>

            <!-- Prix final avec remise exclusive 5% E-ofana -->
            <div class="p-3 bg-light rounded border border-warning-subtle mt-3">
              <div class="d-flex justify-content-between align-items-end">
                <div>
                  <span class="badge bg-danger mb-1">-5% E-HOFANA</span>
                  <div class="small text-muted">Tarif préférentiel :</div>
                </div>
                <div class="text-end">
                  <h4 class="h2 fw-bold text-primary-custom mb-0">{{ formatPrice(finalPrice) }} Ar</h4>
                  <small class="text-muted d-block" style="font-size: 11px;">Paiement unique</small>
                </div>
              </div>
            </div>
          </div>

          <!-- Boutons d'action -->
          <div class="d-flex flex-column gap-3">
            <button class="btn btn-primary-custom py-3 w-100" @click="enroll">
              S'inscrire maintenant
            </button>
            <button class="btn btn-surface-dim py-3 w-100" @click="reserve">
              Réserver une place
            </button>
          </div>

          <div class="d-none d-lg-block mt-4 pt-4 border-top">
            <h5 class="text-uppercase small fw-bold text-muted tracking-wider mb-3">Inclus d'office :</h5>
            <ul class="list-unstyled d-flex flex-column gap-2 mb-0">
              <li class="d-flex align-items-center gap-2 small text-secondary">
                <span class="material-symbols-outlined text-primary-custom" style="font-size: 18px;">workspace_premium</span> Certificat de réussite émis par {{ center.name }}
              </li>
              <li class="d-flex align-items-center gap-2 small text-secondary">
                <span class="material-symbols-outlined text-primary-custom" style="font-size: 18px;">forum</span> Accès à l'espace d'entraide
              </li>
            </ul>
          </div>
        </div>
      </aside>
    </div>
  </main>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'

const currentView = ref('details')
const router = useRouter()
const breadcrumbs = ref([
  { text: 'Développement Web Full Stack', active: true }
])

// --- Données du Formateur / Centre ---
const center = ref({
  name: 'TechAcademy Antananarivo',
  location: 'En face de la gare, Soarano, Antananarivo',
  email: 'contact@techacademy.mg'
})

// --- Données Principales de la formation ---
const course = ref({
  title: 'Développement Web Full Stack',
  category: 'Informatique',
  rating: 4.8,
  reviewsCount: 47,
  spotsLeft: 3,             // Testez en mettant 12 pour voir l'alerte verte passer au rouge
  location: 'Antananarivo (Présentiel)',
  duration: '3 mois',
  
  // Gestion de la tarification
  basePrice: 180000,        // Prix d'origine (optionnel)
  centerPrice: 150000,      // Prix pratiqué par le centre avant réduction E-ofana
  
  startDate: '15 Juillet 2026',
  deadlineDate: '10 Juillet 2026',
  description: [
    'Cette formation intensive vous prépare au développement web moderne. Vous apprendrez à créer des applications complètes du frontend au backend, en utilisant les technologies les plus demandées sur le marché malgache et international.',
    "À l'issue de cette formation, vous serez capable de concevoir, développer et déployer des applications web professionnelles, et vous serez prêt à intégrer le marché de l'emploi numérique."
  ]
})

// --- Prix Final avec calcul de la remise exclusive E-ofana de 5% ---
const finalPrice = computed(() => {
  const priceToDiscount = course.value.centerPrice
  return Math.round(priceToDiscount * 0.95)
})

// --- Formateur de prix pour l'affichage (Espaces pour les milliers) ---
const formatPrice = (price) => {
  return new Intl.NumberFormat('fr-FR').format(price)
}

// --- Méthodes d'action ---
const enroll = () => router.push({ name: 'FormulaireInscription' })
const reserve = () => router.push({ name: 'FormulaireReservation' })
const contactCenter = () => alert(`Ouverture du formulaire de contact pour : ${center.value.email}`)
</script>

<style scoped>
.vue-main-content {
  --primary-color: #c59d5f;
  --surface-color: #fbf9f8;
  --surface-dim: #dbdad9;
  --dark-color: #1a1a1a;
  --hero-bg: #0f2a41;
  --border-radius-custom: 8px;
}

.text-primary-custom { color: var(--primary-color) !important; }

.gradient-overlay {
  background: linear-gradient(to top, rgba(15, 42, 65, 0.95) 20%, rgba(15, 42, 65, 0.2));
}

.style-details strong {
  display: block;
  font-size: 0.9rem;
}

.btn-primary-custom {
  background-color: var(--primary-color);
  color: #121212 !important; 
  border: none;
  font-weight: 700;
  border-radius: var(--border-radius-custom);
  transition: transform 0.2s ease, box-shadow 0.2s ease, opacity 0.2s ease; 
}
.btn-primary-custom:hover {
  color: #121212 !important; 
  background-color: var(--primary-color); 
  opacity: 0.95;
  transform: translateY(-2px); 
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.15); 
}

.btn-surface-dim {
  background-color: rgba(219, 218, 217, 0.4);
  color: var(--dark-color);
  border: none;
  font-weight: 600;
  border-radius: var(--border-radius-custom);
}
.btn-surface-dim:hover { background-color: rgba(219, 218, 217, 0.6); }

.card-custom {
  background: white;
  border-radius: var(--border-radius-custom);
  border: none;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1);
}

.sticky-price-bar {
  position: sticky;
  top: 20px;
  box-shadow: 0 4px 12px rgba(0,0,0,0.08);
}

.tag {
  background: var(--primary-color);
  color: #121212;
  font-size: 0.75rem;
  font-weight: 700;
  padding: 0.25rem 0.75rem;
  border-radius: 50rem;
  display: inline-block;
}

.breadcrumb a { color: var(--primary-color); text-decoration: none; }
.breadcrumb-item + .breadcrumb-item::before { content: "/"; color: #6c757d; }
</style>