<template>
  <nav class="sidebar-shell">
    <div>
      <router-link class="sidebar-link" :class="{ active: isActive('FormateurTableauDeBord') }" :to="{ name: 'FormateurTableauDeBord' }">
        <svg fill="none" height="18" stroke="currentColor" stroke-linecap="round" stroke-linejoin="round" stroke-width="2" width="18" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24"><rect x="3" y="3" width="7" height="9"></rect><rect x="14" y="3" width="7" height="5"></rect><rect x="14" y="12" width="7" height="9"></rect><rect x="3" y="16" width="7" height="5"></rect></svg>
        <span>Tableau de bord</span>
      </router-link>

      <router-link class="sidebar-link" :class="{ active: isActive('PublierFormation') }" :to="{ name: 'PublierFormation' }">
        <svg fill="none" height="18" stroke="currentColor" stroke-linecap="round" stroke-linejoin="round" stroke-width="2" width="18" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24"><line x1="12" y1="5" x2="12" y2="19"></line><line x1="5" y1="12" x2="19" y2="12"></line></svg>
        <span>Publier une formation</span>
      </router-link>

      <a class="sidebar-link" href="#">
        <svg fill="none" height="18" stroke="currentColor" stroke-linecap="round" stroke-linejoin="round" stroke-width="2" width="18" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24"><path d="M4 19.5A2.5 2.5 0 0 1 6.5 17H20"></path><path d="M6.5 2H20v20H6.5A2.5 2.5 0 0 1 4 19.5v-15A2.5 2.5 0 0 1 6.5 2z"></path></svg>
        <span>Mes formations</span>
      </a>

      <a class="sidebar-link" href="#">
        <svg fill="none" height="18" stroke="currentColor" stroke-linecap="round" stroke-linejoin="round" stroke-width="2" width="18" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24"><path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"></path><circle cx="9" cy="7" r="4"></circle><path d="M23 21v-2a4 4 0 0 0-3-3.87"></path><path d="M16 3.13a4 4 0 0 1 0 7.75"></path></svg>
        <span>Liste des inscrits</span>
      </a>

      <a class="sidebar-link" href="#">
        <svg fill="none" height="18" stroke="currentColor" stroke-linecap="round" stroke-linejoin="round" stroke-width="2" width="18" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24"><line x1="18" y1="20" x2="18" y2="10"></line><line x1="12" y1="20" x2="12" y2="4"></line><line x1="6" y1="20" x2="6" y2="14"></line></svg>
        <span>Tableau financier</span>
      </a>

      <a class="sidebar-link" href="#">
        <svg fill="none" height="18" stroke="currentColor" stroke-linecap="round" stroke-linejoin="round" stroke-width="2" width="18" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"></path><circle cx="12" cy="7" r="4"></circle></svg>
        <span>Profil du centre</span>
      </a>
    </div>

    <div>
      <div class="sidebar-divider"></div>
      <button class="sidebar-link logout-button" type="button" @click="$emit('logout')">
        <svg fill="none" height="18" stroke="currentColor" stroke-linecap="round" stroke-linejoin="round" stroke-width="2" width="18" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24"><path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"></path><polyline points="16 17 21 12 16 7"></polyline><line x1="21" y1="12" x2="9" y2="12"></line></svg>
        <span>Déconnexion</span>
      </button>
    </div>
  </nav>
</template>

<script setup>
import { useRoute } from 'vue-router'

defineEmits(['logout'])

const route = useRoute()

function isActive(name) {
  return route.name === name
}
</script>

<style scoped>
.sidebar-shell {
  width: 100%;
}

.sidebar-link {
  width: 100%;
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 0.85rem 1.5rem;
  color: #a0a0a0;
  text-decoration: none;
  font-weight: 500;
  font-size: 0.95rem;
  transition: all 0.2s ease;
  background: transparent;
  border: 0;
  text-align: left;
}

.sidebar-link:hover,
.sidebar-link.active {
  color: #ffffff;
  background-color: rgba(255, 255, 255, 0.03);
}

.sidebar-link.active {
  background-color: rgba(198, 156, 80, 0.1);
  color: #c69c50;
  font-weight: 600;
}

.sidebar-divider {
  border-top: 1px solid #2d2d2d;
  margin: 1rem 0;
}

.logout-button {
  cursor: pointer;
}
</style>