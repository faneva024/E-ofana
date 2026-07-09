<template>
  <div class="formateur-layout">
    <HeaderFormateur @menu-toggle="isMobileSidebarOpen = true" />

    <div class="formateur-shell">
      <aside class="sidebar-desktop d-none d-lg-flex">
        <SidebarFormateur @logout="handleLogout" />
      </aside>

      <transition name="sidebar-fade">
        <button
          v-if="isMobileSidebarOpen"
          class="mobile-backdrop d-lg-none"
          type="button"
          aria-label="Fermer le menu"
          @click="isMobileSidebarOpen = false"
        ></button>
      </transition>

      <aside class="mobile-sidebar d-lg-none" :class="{ open: isMobileSidebarOpen }">
        <div class="mobile-sidebar-header">
          <h5 class="offcanvas-title text-white fw-bold mb-0">Menu</h5>
          <button type="button" class="btn-close btn-close-white" aria-label="Fermer le menu" @click="isMobileSidebarOpen = false"></button>
        </div>
        <div class="mobile-sidebar-body">
          <SidebarFormateur @logout="handleLogout" @link-clicked="closeMobileSidebar" />
        </div>
      </aside>

      <main class="main-content">
        <div class="content-body">
          <router-view />
        </div>
      </main>
    </div>
  </div>
  <NavPage></NavPage>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthFormateurStore } from '../stores/authFormateurStore'
import HeaderFormateur from '../components/HeaderFormateur.vue'
import SidebarFormateur from '../components/SidebarFormateur.vue'
import NavPage from '../composants/navPage.vue'

const router = useRouter()
const authFormateurStore = useAuthFormateurStore()

const isMobileSidebarOpen = ref(false)

const closeMobileSidebar = () => {
  isMobileSidebarOpen.value = false
}

const handleLogout = () => {
  authFormateurStore.logout()
  closeMobileSidebar()
  router.push({ name: 'Connexion' })
}
</script>

<style scoped>
.formateur-layout {
  min-height: 100vh;
  background: #f8f9fa;
  color: #1a1a1a;
  --formateur-header-height: 72px;
}

.formateur-shell {
  display: flex;
  min-height: calc(100vh - var(--formateur-header-height));
}

.sidebar-desktop {
  width: 260px;
  background: #1a1a1a;
  border-right: 1px solid #2d2d2d;
  padding: 1.5rem 0;
  position: sticky;
  top: var(--header-height);
  height: calc(100vh - var(--header-height));
  flex-direction: column;
  justify-content: space-between;
  flex-shrink: 0;
}

.main-content {
  flex: 1;
  min-width: 0;
}

.content-body {
  padding: 2.5rem;
}

.mobile-backdrop {
  position: fixed;
  inset: var(--formateur-header-height) 0 0 0;
  border: 0;
  background: rgba(0, 0, 0, 0.45);
  z-index: 1070;
}

.mobile-sidebar {
  position: fixed;
  top: var(--formateur-header-height);
  left: 0;
  width: 280px;
  height: calc(100vh - var(--formateur-header-height));
  background: #1a1a1a;
  border-right: 1px solid #2d2d2d;
  transform: translateX(-100%);
  transition: transform 0.25s ease;
  z-index: 1080;
  display: flex;
  flex-direction: column;
}

.mobile-sidebar.open {
  transform: translateX(0);
}

.mobile-sidebar-header {
  min-height: 64px;
  padding: 0 1.25rem;
  border-bottom: 1px solid rgba(255, 255, 255, 0.08);
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.mobile-sidebar-body {
  padding: 0.75rem 0 1.5rem;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  height: 100%;
}

.sidebar-fade-enter-active,
.sidebar-fade-leave-active {
  transition: opacity 0.2s ease;
}

.sidebar-fade-enter-from,
.sidebar-fade-leave-to {
  opacity: 0;
}

@media (max-width: 991.98px) {
  .content-body {
    padding: 1.5rem;
  }
}

@media (max-width: 575.98px) {
  .content-body {
    padding: 1rem;
  }
}
</style>