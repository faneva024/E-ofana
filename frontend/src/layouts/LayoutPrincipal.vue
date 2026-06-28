<template>
  <div class="layout-container">
    <!-- Barre de navigation -->
    <Header />

    <!-- Contenu dynamique de la page -->
    <main class="main-content">
      <router-view />
    </main>

    <!-- Pied de page -->
    <Footer />
  </div>
</template>

<script setup>
import Header from '../components/Header.vue';
import Footer from '../components/Footer.vue';
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