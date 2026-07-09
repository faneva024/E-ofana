<template>
  <header class="custom-header sticky-top">
    <div class="container-xl d-flex align-items-center justify-content-between">
      <router-link class="d-flex align-items-center gap-2 text-reset text-decoration-none" to="/">
        <div class="logo-box">
          <span class="material-symbols-outlined" style="font-size: 22px; font-weight: bold;">school</span>
        </div>
        <span class="logo-text">E-OFANA</span>
      </router-link>

      <button 
        class="btn text-white d-lg-none menu-toggle-btn p-1" 
        type="button" 
        @click="toggleMenu"
        :aria-expanded="isMenuOpen"
        aria-label="Toggle navigation"
      >
        <span class="material-symbols-outlined fs-2">
          {{ isMenuOpen ? 'close' : 'menu' }}
        </span>
      </button>

      <div class="nav-actions-wrapper" :class="{ 'is-open': isMenuOpen }">
        <a class="nav-action-link d-flex align-items-center gap-2" href="/recherche-formations" @click="closeMenu">
          <span class="material-symbols-outlined" style="font-size: 20px;">search</span>
          <span>Rechercher</span>
        </a>
        <template v-if="authStore.isAuthenticated">
          <router-link class="nav-action-link d-flex align-items-center gap-2" to="/mon-espace-apprenant" @click="closeMenu">
            <span class="material-symbols-outlined" style="font-size: 20px;">dashboard</span>
            <span>Mon espace</span>
          </router-link>
          <div class="d-flex align-items-center gap-2 text-white-50" @click="closeMenu">
            <div class="user-avatar-nav">{{ initials }}</div>
            <span class="small d-none d-md-inline text-white">{{ displayName }}</span>
          </div>
        </template>
        <template v-else>
          <a class="nav-action-link d-flex align-items-center gap-2" href="/connexion" @click="closeMenu">
            <span class="material-symbols-outlined" style="font-size: 20px;">login</span>
            <span>Connexion</span>
          </a>
          <button class="btn-inscription d-flex align-items-center justify-content-center gap-2 w-100-mobile" @click="handleRegister">
            <span class="material-symbols-outlined" style="font-size: 18px;">person_add</span>
            <span>Inscription</span>
          </button>
        </template>
      </div>
    </div>
  </header>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useAuthStore } from '../stores/authStore'

const authStore = useAuthStore()
const isMenuOpen = ref(false)

const initials = computed(() => {
  const p = authStore.user?.prenom || ''
  const n = authStore.user?.nom || ''
  if (p && n) return (p[0] + n[0]).toUpperCase()
  if (authStore.user?.email) return authStore.user.email[0].toUpperCase()
  return 'U'
})

const displayName = computed(() => {
  const p = authStore.user?.prenom || ''
  const n = authStore.user?.nom || ''
  if (p && n) return p + ' ' + n
  return authStore.user?.email?.split('@')[0] || 'Utilisateur'
})

const toggleMenu = () => {
  isMenuOpen.value = !isMenuOpen.value
}

const closeMenu = () => {
  isMenuOpen.value = false
}

const handleRegister = () => {
  closeMenu()
  window.location.href = '/inscription'
}
</script>

<style scoped>
.custom-header {
  background-color: #121212;
  padding: 0.85rem 0;
  border-bottom: 1px solid rgba(255, 255, 255, 0.03);
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

.btn-inscription {
  background-color: #c59d5f;
  color: #121212;
  font-weight: 600;
  border: none;
  padding: 0.5rem 1.25rem;
  border-radius: 8px;
  transition: background-color 0.2s;
}

.btn-inscription:hover {
  background-color: #b38c50;
  color: #121212;
}

.menu-toggle-btn:focus {
  box-shadow: none;
}

.nav-actions-wrapper {
  display: flex;
  align-items: center;
  gap: 1.5rem;
}

@media (max-width: 991.98px) {
  .nav-actions-wrapper {
    position: absolute;
    top: calc(100% + 1rem);
    left: 0;
    right: 0;
    background-color: #121212;
    flex-direction: column;
    align-items: flex-start;
    gap: 1rem;
    padding: 1.5rem;
    border-radius: 8px;
    box-shadow: 0 10px 25px rgba(0, 0, 0, 0.3);
    border: 1px solid rgba(255, 255, 255, 0.05);
    opacity: 0;
    visibility: hidden;
    transform: translateY(-10px);
    transition: opacity 0.25s, transform 0.25s, visibility 0.25s;
    z-index: 1050;
  }

  .nav-actions-wrapper.is-open {
    opacity: 1;
    visibility: visible;
    transform: translateY(0);
  }

  .nav-action-link {
    width: 100%;
    border-bottom: 1px solid rgba(255, 255, 255, 0.05);
    padding-bottom: 0.75rem;
  }

  .w-100-mobile {
    width: 100%;
  }
}
</style>
