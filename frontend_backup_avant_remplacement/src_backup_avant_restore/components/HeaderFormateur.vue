<template>
  <header class="custom-header sticky-top">
    <div class="container-fluid px-3 d-flex align-items-center">
      
      
      <div class="d-flex align-items-center gap-3 me-auto">
       <!--ito ilay emit -->
        <button class="btn text-white d-lg-none menu-toggle-btn p-1" type="button" @click="$emit('menu-toggle')">
          <span class="material-symbols-outlined fs-2">menu</span>
        </button>

        <!-- Logo -->
        <router-link class="d-flex align-items-center gap-2 text-reset text-decoration-none" to="/">
          <div class="logo-box">
            <span class="material-symbols-outlined" style="font-size: 22px; font-weight: bold;">school</span>
          </div>
          <span class="logo-text">E-OFANA</span>
        </router-link>
      </div>

      <!-- Bloc Droite : Actions -->
      <div class="nav-actions-wrapper">
        <span class="small fw-medium brand-gold-text d-none d-sm-inline">Espace Formateur</span>
        
        <router-link :to="{ name: 'Home' }" class="nav-action-link d-flex align-items-center gap-2">
          <span class="material-symbols-outlined" style="font-size: 20px;">home</span>
          <span class="d-none d-md-inline">Accueil</span>
        </router-link>
        
        <div class="d-flex align-items-center gap-2">
          <div class="user-avatar-nav">{{ initials }}</div>
          <span class="small d-none d-md-inline text-white">{{ displayName }}</span>
        </div>
      </div>

    </div>
  </header>
</template>

<script setup>
import { computed } from 'vue'
import { useAuthFormateurStore } from '../stores/authFormateurStore'

defineEmits(['menu-toggle'])

const authFormateurStore = useAuthFormateurStore()

// Calcule les initiales à partir du compte formateur connecté
const initials = computed(() => {
  const p = authFormateurStore.currentFormateur?.prenom || ''
  const n = authFormateurStore.currentFormateur?.nom || ''
  if (p && n) return (p[0] + n[0]).toUpperCase()
  if (authFormateurStore.currentFormateur?.email) return authFormateurStore.currentFormateur.email[0].toUpperCase()
  return 'F' // Par défaut 'F' pour Formateur
})

// Calcule le nom affiché à côté de l'avatar
const displayName = computed(() => {
  const p = authFormateurStore.currentFormateur?.prenom || ''
  const n = authFormateurStore.currentFormateur?.nom || ''
  if (p && n) return p + ' ' + n
  return authFormateurStore.currentFormateur?.email?.split('@')[0] || 'Formateur'
})
</script>

<style scoped>
.custom-header {
  background-color: #121212;
  padding: 0.85rem 0;
  border-bottom: 1px solid rgba(255, 255, 255, 0.03);
  width: 100%;
  min-height: var(--formateur-header-height, 72px);
  z-index: 1060;
}

.logo-box {
  background-color: #c59d5f;
  color: #121212;
  width: 38px;
  height: 38px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.logo-text {
  color: #ffffff;
  font-weight: 800;
  font-size: 1.35rem;
  letter-spacing: -0.03em;
}

.brand-gold-text {
  color: #c59d5f;
}

.nav-action-link {
  color: #e2e8f0;
  text-decoration: none;
  font-size: 0.95rem;
  font-weight: 500;
  display: inline-flex;
  align-items: center;
  gap: 0.4rem;
  transition: color 0.2s;
  opacity: 0.9;
}

.nav-action-link:hover {
  color: #ffffff;
}

.user-avatar-nav {
  width: 34px;
  height: 34px;
  background-color: #c59d5f;
  color: #121212;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 700;
  font-size: 0.85rem;
}

.menu-toggle-btn:focus {
  box-shadow: none;
}

.nav-actions-wrapper {
  display: flex;
  align-items: center;
  gap: 1.5rem;
}


</style>