<template>
  <div class="admin-dashboard">
    <div class="container py-5">
      <h1 class="fw-bold mb-4">Tableau de bord Admin</h1>
      <p class="text-muted">Bienvenue dans votre espace d'administration E-OFANA.</p>
    </div>
  </div>
</template>

<script setup>
import { useAuthAdminStore } from '../../stores/authAdminStore'

const authAdminStore = useAuthAdminStore()
</script>

<style scoped>
.admin-dashboard {
  min-height: 80vh;
}
</style>
