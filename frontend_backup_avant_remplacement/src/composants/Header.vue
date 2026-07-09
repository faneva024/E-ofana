<template>
  <header class="eo-header">
    <nav class="navbar navbar-expand-lg">
      <div class="container">
        <router-link to="/" class="navbar-brand">
          <div class="brand-content">
            <i class="bi bi-mortarboard-fill brand-icon"></i>
            <span class="brand-text">E-OFANA</span>
          </div>
        </router-link>

        <button
          class="navbar-toggler"
          type="button"
          @click="mobileMenuOpen = !mobileMenuOpen"
          :aria-expanded="mobileMenuOpen"
        >
          <span class="navbar-toggler-icon"></span>
        </button>

        <div class="collapse navbar-collapse" :class="{ show: mobileMenuOpen }">
          <ul class="navbar-nav ms-auto">
            <li class="nav-item">
              <router-link to="/" class="nav-link" @click="mobileMenuOpen = false">
                <i class="bi bi-house-fill me-1"></i>
                Accueil
              </router-link>
            </li>
            <li class="nav-item">
              <router-link to="/recherche-formations" class="nav-link" @click="mobileMenuOpen = false">
                <i class="bi bi-search me-1"></i>
                Formations
              </router-link>
            </li>
            <li class="nav-item">
              <router-link to="/a-propos" class="nav-link" @click="mobileMenuOpen = false">
                <i class="bi bi-info-circle-fill me-1"></i>
                À propos
              </router-link>
            </li>
            <li class="nav-item">
              <router-link to="/contact" class="nav-link" @click="mobileMenuOpen = false">
                <i class="bi bi-envelope-fill me-1"></i>
                Contact
              </router-link>
            </li>
          </ul>

          <div class="auth-buttons ms-lg-3">
            <template v-if="!authStore.isAuthenticated">
              <router-link to="/connexion" class="btn btn-outline-primary me-2" @click="mobileMenuOpen = false">
                <i class="bi bi-box-arrow-in-right me-1"></i>
                Connexion
              </router-link>
              <router-link to="/inscription" class="btn btn-primary" @click="mobileMenuOpen = false">
                <i class="bi bi-person-plus-fill me-1"></i>
                Inscription
              </router-link>
            </template>
            <template v-else>
              <div class="user-menu">
                <div class="dropdown">
                  <button
                    class="btn btn-primary dropdown-toggle"
                    type="button"
                    @click="userDropdownOpen = !userDropdownOpen"
                  >
                    <i class="bi bi-person-circle me-1"></i>
                    {{ displayName }}
                  </button>
                  <ul class="dropdown-menu" :class="{ show: userDropdownOpen }">
                    <li>
                      <router-link to="/mon-espace" class="dropdown-item" @click="closeDropdowns">
                        <i class="bi bi-grid-fill me-2"></i>
                        Mon espace
                      </router-link>
                    </li>
                    <li>
                      <router-link to="/mon-profil" class="dropdown-item" @click="closeDropdowns">
                        <i class="bi bi-person-gear me-2"></i>
                        Mon profil
                      </router-link>
                    </li>
                    <li><hr class="dropdown-divider"></li>
                    <li>
                      <a class="dropdown-item" href="#" @click.prevent="handleLogout">
                        <i class="bi bi-box-arrow-right me-2"></i>
                        Déconnexion
                      </a>
                    </li>
                  </ul>
                </div>
              </div>
            </template>
          </div>
        </div>
      </div>
    </nav>
  </header>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useAuthStore } from '../stores/authStore'

const authStore = useAuthStore()

const mobileMenuOpen = ref(false)
const userDropdownOpen = ref(false)

const displayName = computed(() => {
  if (authStore.user) {
    return authStore.user.prenom || authStore.user.email?.split('@')[0] || 'Utilisateur'
  }
  return 'Utilisateur'
})

const handleLogout = () => {
  authStore.logout()
  closeDropdowns()
}

const closeDropdowns = () => {
  mobileMenuOpen.value = false
  userDropdownOpen.value = false
}

const handleClickOutside = (event) => {
  if (!event.target.closest('.dropdown')) {
    userDropdownOpen.value = false
  }
}

onMounted(() => {
  document.addEventListener('click', handleClickOutside)
})

onUnmounted(() => {
  document.removeEventListener('click', handleClickOutside)
})
</script>

<style scoped>
.eo-header {
  background: white;
  box-shadow: var(--eo-shadow-md);
  position: sticky;
  top: 0;
  z-index: var(--eo-z-sticky);
}

.navbar {
  padding: var(--eo-spacing-md) 0;
}

.navbar-brand {
  display: flex;
  align-items: center;
  text-decoration: none;
  padding: 0;
}

.brand-content {
  display: flex;
  align-items: center;
  gap: var(--eo-spacing-sm);
}

.brand-icon {
  font-size: 2rem;
  color: var(--eo-primary);
}

.brand-text {
  font-size: var(--eo-font-size-2xl);
  font-weight: 800;
  color: var(--eo-primary);
  font-family: var(--eo-font-family);
  letter-spacing: -0.5px;
}

.navbar-toggler {
  border: none;
  padding: var(--eo-spacing-sm);
}

.navbar-toggler:focus {
  box-shadow: none;
}

.navbar-nav {
  gap: var(--eo-spacing-sm);
  align-items: center;
}

.nav-link {
  color: var(--eo-gray-700);
  font-weight: 500;
  padding: var(--eo-spacing-sm) var(--eo-spacing-md) !important;
  border-radius: var(--eo-radius-md);
  transition: all var(--eo-transition-base);
  display: flex;
  align-items: center;
}

.nav-link:hover,
.nav-link.router-link-active {
  color: var(--eo-primary);
  background: var(--eo-gray-100);
}

.nav-link i {
  font-size: 1.1rem;
}

.auth-buttons {
  display: flex;
  align-items: center;
  gap: var(--eo-spacing-sm);
}

.btn-outline-primary {
  color: var(--eo-primary);
  border-color: var(--eo-primary);
  padding: var(--eo-spacing-sm) var(--eo-spacing-lg);
  font-weight: 500;
  border-radius: var(--eo-radius-lg);
  transition: all var(--eo-transition-base);
}

.btn-outline-primary:hover {
  background-color: var(--eo-primary);
  border-color: var(--eo-primary);
  color: white;
}

.btn-primary {
  background-color: var(--eo-primary);
  border-color: var(--eo-primary);
  padding: var(--eo-spacing-sm) var(--eo-spacing-lg);
  font-weight: 500;
  border-radius: var(--eo-radius-lg);
  transition: all var(--eo-transition-base);
}

.btn-primary:hover {
  background-color: var(--eo-primary-dark);
  border-color: var(--eo-primary-dark);
  color: white;
}

.user-menu {
  position: relative;
}

.dropdown-toggle {
  background-color: var(--eo-primary);
  border-color: var(--eo-primary);
  padding: var(--eo-spacing-sm) var(--eo-spacing-lg);
  font-weight: 500;
  border-radius: var(--eo-radius-lg);
  display: flex;
  align-items: center;
  gap: var(--eo-spacing-xs);
}

.dropdown-toggle:hover {
  background-color: var(--eo-primary-dark);
  border-color: var(--eo-primary-dark);
  color: white;
}

.dropdown-toggle::after {
  margin-left: var(--eo-spacing-xs);
}

.dropdown-menu {
  position: absolute;
  right: 0;
  top: calc(100% + var(--eo-spacing-xs));
  min-width: 200px;
  border: none;
  border-radius: var(--eo-radius-lg);
  box-shadow: var(--eo-shadow-lg);
  padding: var(--eo-spacing-sm);
  background: white;
  z-index: var(--eo-z-dropdown);
}

.dropdown-item {
  color: var(--eo-gray-700);
  padding: var(--eo-spacing-sm) var(--eo-spacing-md);
  border-radius: var(--eo-radius-md);
  display: flex;
  align-items: center;
  transition: all var(--eo-transition-base);
}

.dropdown-item:hover {
  background-color: var(--eo-gray-100);
  color: var(--eo-primary);
}

.dropdown-item i {
  font-size: 1.1rem;
}

.dropdown-divider {
  margin: var(--eo-spacing-sm) 0;
  border-color: var(--eo-gray-200);
}

@media (max-width: 991px) {
  .navbar-collapse {
    background: white;
    padding: var(--eo-spacing-lg);
    margin-top: var(--eo-spacing-md);
    border-radius: var(--eo-radius-lg);
    box-shadow: var(--eo-shadow-lg);
  }

  .navbar-nav {
    flex-direction: column;
    align-items: stretch;
    width: 100%;
  }

  .nav-link {
    padding: var(--eo-spacing-md) !important;
  }

  .auth-buttons {
    flex-direction: column;
    width: 100%;
    margin-top: var(--eo-spacing-lg);
    margin-left: 0 !important;
  }

  .auth-buttons .btn {
    width: 100%;
  }

  .user-menu {
    width: 100%;
  }

  .dropdown-toggle {
    width: 100%;
    justify-content: center;
  }

  .dropdown-menu {
    position: static;
    margin-top: var(--eo-spacing-sm);
    width: 100%;
  }
}
</style>
