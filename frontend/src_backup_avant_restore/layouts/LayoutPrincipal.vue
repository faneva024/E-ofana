<template>
  <div class="layout-container">
    <!-- Barre de navigation -->
    <Header v-if="shouldShowChrome" />

    <!-- Contenu dynamique de la page -->
    <main class="main-content">
      <router-view />
    </main>

    <!-- Pied de page -->
    <Footer v-if="shouldShowChrome" />
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import Header from '../components/Header.vue'
import Footer from '../components/Footer.vue'

const route = useRoute()

const shouldShowChrome = computed(() => {
  const hiddenRoutes = ['MonEspaceApprenant', 'MonEspace']
  return !hiddenRoutes.includes(route.name) && !route.path.startsWith('/mon-espace') && !route.path.startsWith('/formateur')
})
</script>

<style>
/* Variables globales et styles appliqués au corps du layout */
:root {
  --primary-color: #c59d5f;
  --brand-dark: #1f1f1f;
  --surface-color: #fbf9f8;
}

body {
  font-family: 'Hanken Grotesk', sans-serif;
  background-color: var(--surface-color);
  color: #212529;
  overflow-x: hidden;
  margin: 0;
}

.layout-container {
  display: flex;
  flex-direction: column;
  min-height: 100vh;
}

.main-content {
  flex-grow: 1;
}
</style>