<template>
  <div class="mon-espace-container">
    <div class="espace-header">
      <div class="header-content">
        <div class="user-welcome">
          <div class="avatar-section">
            <div class="avatar">
              <i class="bi bi-person-fill"></i>
            </div>
          </div>
          <div class="welcome-text">
            <h1 class="welcome-title">Bienvenue, {{ displayName }} !</h1>
            <p class="welcome-subtitle">Gérez votre espace apprenant</p>
          </div>
        </div>
      </div>
    </div>

    <div class="espace-content">
      <div class="container">
        <div class="row">
          <!-- Sidebar -->
          <div class="col-lg-3 mb-4">
            <div class="sidebar-card">
              <div class="sidebar-menu">
                <div
                  class="menu-item"
                  :class="{ active: activeTab === 'overview' }"
                  @click="activeTab = 'overview'"
                >
                  <i class="bi bi-grid-fill me-2"></i>
                  Vue d'ensemble
                </div>
                <div
                  class="menu-item"
                  :class="{ active: activeTab === 'courses' }"
                  @click="activeTab = 'courses'"
                >
                  <i class="bi bi-book-fill me-2"></i>
                  Mes formations
                </div>
                <div
                  class="menu-item"
                  :class="{ active: activeTab === 'certificates' }"
                  @click="activeTab = 'certificates'"
                >
                  <i class="bi bi-award-fill me-2"></i>
                  Certificats
                </div>
                <div
                  class="menu-item"
                  :class="{ active: activeTab === 'profile' }"
                  @click="activeTab = 'profile'"
                >
                  <i class="bi bi-person-gear me-2"></i>
                  Mon profil
                </div>
                <div
                  class="menu-item"
                  :class="{ active: activeTab === 'settings' }"
                  @click="activeTab = 'settings'"
                >
                  <i class="bi bi-gear-fill me-2"></i>
                  Paramètres
                </div>
              </div>

              <div class="sidebar-footer">
                <button class="btn btn-outline-danger w-100" @click="handleLogout">
                  <i class="bi bi-box-arrow-right me-2"></i>
                  Déconnexion
                </button>
              </div>
            </div>
          </div>

          <!-- Main Content -->
          <div class="col-lg-9">
            <!-- Overview Tab -->
            <div v-if="activeTab === 'overview'" class="content-section">
              <div class="stats-grid">
                <div class="stat-card">
                  <div class="stat-icon courses">
                    <i class="bi bi-book-fill"></i>
                  </div>
                  <div class="stat-content">
                    <h3 class="stat-value">3</h3>
                    <p class="stat-label">Formations en cours</p>
                  </div>
                </div>
                <div class="stat-card">
                  <div class="stat-icon completed">
                    <i class="bi bi-check-circle-fill"></i>
                  </div>
                  <div class="stat-content">
                    <h3 class="stat-value">12</h3>
                    <p class="stat-label">Formations terminées</p>
                  </div>
                </div>
                <div class="stat-card">
                  <div class="stat-icon certificates">
                    <i class="bi bi-award-fill"></i>
                  </div>
                  <div class="stat-content">
                    <h3 class="stat-value">8</h3>
                    <p class="stat-label">Certificats obtenus</p>
                  </div>
                </div>
                <div class="stat-card">
                  <div class="stat-icon hours">
                    <i class="bi bi-clock-fill"></i>
                  </div>
                  <div class="stat-content">
                    <h3 class="stat-value">45h</h3>
                    <p class="stat-label">Temps de formation</p>
                  </div>
                </div>
              </div>

              <div class="recent-activity">
                <h3 class="section-title">
                  <i class="bi bi-clock-history me-2"></i>
                  Activité récente
                </h3>
                <div class="activity-list">
                  <div class="activity-item">
                    <div class="activity-icon">
                      <i class="bi bi-play-circle-fill"></i>
                    </div>
                    <div class="activity-content">
                      <h4 class="activity-title">Développement Web avec Vue.js</h4>
                      <p class="activity-meta">Il y a 2 heures • Leçon 5/20</p>
                    </div>
                    <div class="activity-progress">
                      <div class="progress" style="height: 6px;">
                        <div class="progress-bar" style="width: 25%"></div>
                      </div>
                      <small>25%</small>
                    </div>
                  </div>
                  <div class="activity-item">
                    <div class="activity-icon">
                      <i class="bi bi-play-circle-fill"></i>
                    </div>
                    <div class="activity-content">
                      <h4 class="activity-title">Introduction à Python</h4>
                      <p class="activity-meta">Hier • Leçon 12/15</p>
                    </div>
                    <div class="activity-progress">
                      <div class="progress" style="height: 6px;">
                        <div class="progress-bar" style="width: 80%"></div>
                      </div>
                      <small>80%</small>
                    </div>
                  </div>
                  <div class="activity-item">
                    <div class="activity-icon completed">
                      <i class="bi bi-check-circle-fill"></i>
                    </div>
                    <div class="activity-content">
                      <h4 class="activity-title">Bases de JavaScript</h4>
                      <p class="activity-meta">Il y a 3 jours • Terminé</p>
                    </div>
                    <div class="activity-badge">
                      <span class="badge bg-success">Certificat</span>
                    </div>
                  </div>
                </div>
              </div>
            </div>

            <!-- Courses Tab -->
            <div v-if="activeTab === 'courses'" class="content-section">
              <h3 class="section-title">
                <i class="bi bi-book-fill me-2"></i>
                Mes formations
              </h3>
              <div class="courses-grid">
                <div class="course-card">
                  <div class="course-image">
                    <div class="course-category">Développement</div>
                  </div>
                  <div class="course-content">
                    <h4 class="course-title">Développement Web avec Vue.js</h4>
                    <p class="course-description">Apprenez à créer des applications web modernes avec Vue 3</p>
                    <div class="course-meta">
                      <span><i class="bi bi-clock me-1"></i> 20h</span>
                      <span><i class="bi bi-play-circle me-1"></i> 20 leçons</span>
                    </div>
                    <div class="course-progress">
                      <div class="progress" style="height: 8px;">
                        <div class="progress-bar" style="width: 25%"></div>
                      </div>
                      <small>25% complété</small>
                    </div>
                    <button class="btn btn-primary w-100 mt-3">Continuer</button>
                  </div>
                </div>
                <div class="course-card">
                  <div class="course-image">
                    <div class="course-category">Data Science</div>
                  </div>
                  <div class="course-content">
                    <h4 class="course-title">Introduction à Python</h4>
                    <p class="course-description">Les fondamentaux de la programmation en Python</p>
                    <div class="course-meta">
                      <span><i class="bi bi-clock me-1"></i> 15h</span>
                      <span><i class="bi bi-play-circle me-1"></i> 15 leçons</span>
                    </div>
                    <div class="course-progress">
                      <div class="progress" style="height: 8px;">
                        <div class="progress-bar" style="width: 80%"></div>
                      </div>
                      <small>80% complété</small>
                    </div>
                    <button class="btn btn-primary w-100 mt-3">Continuer</button>
                  </div>
                </div>
              </div>
            </div>

            <!-- Certificates Tab -->
            <div v-if="activeTab === 'certificates'" class="content-section">
              <h3 class="section-title">
                <i class="bi bi-award-fill me-2"></i>
                Mes certificats
              </h3>
              <div class="certificates-grid">
                <div class="certificate-card">
                  <div class="certificate-icon">
                    <i class="bi bi-award-fill"></i>
                  </div>
                  <div class="certificate-content">
                    <h4 class="certificate-title">Bases de JavaScript</h4>
                    <p class="certificate-date">Obtenu le 15 Janvier 2026</p>
                    <button class="btn btn-outline-primary">
                      <i class="bi bi-download me-2"></i>
                      Télécharger
                    </button>
                  </div>
                </div>
                <div class="certificate-card">
                  <div class="certificate-icon">
                    <i class="bi bi-award-fill"></i>
                  </div>
                  <div class="certificate-content">
                    <h4 class="certificate-title">HTML & CSS Fundamentals</h4>
                    <p class="certificate-date">Obtenu le 10 Décembre 2025</p>
                    <button class="btn btn-outline-primary">
                      <i class="bi bi-download me-2"></i>
                      Télécharger
                    </button>
                  </div>
                </div>
              </div>
            </div>

            <!-- Profile Tab -->
            <div v-if="activeTab === 'profile'" class="content-section">
              <h3 class="section-title">
                <i class="bi bi-person-gear me-2"></i>
                Mon profil
              </h3>
              <div class="profile-card">
                <form @submit.prevent="handleProfileUpdate">
                  <div class="row">
                    <div class="col-md-6 mb-3">
                      <label class="form-label">Nom</label>
                      <input type="text" class="form-control" :value="authStore.user?.nom || ''" />
                    </div>
                    <div class="col-md-6 mb-3">
                      <label class="form-label">Prénom</label>
                      <input type="text" class="form-control" :value="authStore.user?.prenom || ''" />
                    </div>
                  </div>
                  <div class="mb-3">
                    <label class="form-label">Email</label>
                    <input type="email" class="form-control" :value="authStore.user?.email || ''" />
                  </div>
                  <div class="mb-3">
                    <label class="form-label">Téléphone</label>
                    <input type="tel" class="form-control" :value="authStore.user?.telephone || ''" />
                  </div>
                  <button type="submit" class="btn btn-primary">
                    <i class="bi bi-check-lg me-2"></i>
                    Enregistrer les modifications
                  </button>
                </form>
              </div>
            </div>

            <!-- Settings Tab -->
            <div v-if="activeTab === 'settings'" class="content-section">
              <h3 class="section-title">
                <i class="bi bi-gear-fill me-2"></i>
                Paramètres
              </h3>
              <div class="settings-card">
                <div class="setting-item">
                  <div class="setting-info">
                    <h4>Notifications par email</h4>
                    <p>Recevoir des notifications sur votre progression</p>
                  </div>
                  <div class="form-check form-switch">
                    <input class="form-check-input" type="checkbox" checked />
                  </div>
                </div>
                <div class="setting-item">
                  <div class="setting-info">
                    <h4>Mode sombre</h4>
                    <p>Activer le thème sombre</p>
                  </div>
                  <div class="form-check form-switch">
                    <input class="form-check-input" type="checkbox" />
                  </div>
                </div>
                <div class="setting-item">
                  <div class="setting-info">
                    <h4>Langue</h4>
                    <p>Choisir la langue de l'interface</p>
                  </div>
                  <select class="form-select" style="width: auto;">
                    <option>Français</option>
                    <option>English</option>
                    <option>Malagasy</option>
                  </select>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/authStore'

const router = useRouter()
const authStore = useAuthStore()

const activeTab = ref('overview')

const displayName = computed(() => {
  if (authStore.user) {
    return authStore.user.prenom || authStore.user.email?.split('@')[0] || 'Utilisateur'
  }
  return 'Utilisateur'
})

const handleLogout = () => {
  authStore.logout()
}

const handleProfileUpdate = () => {
  // TODO: Implement profile update
  console.log('Profile update')
}
</script>

<style scoped>
.mon-espace-container {
  min-height: 100vh;
  background: var(--eo-gray-50);
}

.espace-header {
  background: linear-gradient(135deg, var(--eo-primary) 0%, var(--eo-primary-dark) 100%);
  padding: var(--eo-spacing-2xl) 0;
  color: white;
}

.header-content {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 var(--eo-spacing-md);
}

.user-welcome {
  display: flex;
  align-items: center;
  gap: var(--eo-spacing-lg);
}

.avatar-section {
  flex-shrink: 0;
}

.avatar {
  width: 80px;
  height: 80px;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 2.5rem;
  border: 3px solid rgba(255, 255, 255, 0.3);
}

.welcome-text {
  flex: 1;
}

.welcome-title {
  font-size: var(--eo-font-size-3xl);
  font-weight: 700;
  margin: 0;
  font-family: var(--eo-font-family);
}

.welcome-subtitle {
  font-size: var(--eo-font-size-lg);
  margin: var(--eo-spacing-sm) 0 0 0;
  opacity: 0.9;
}

.espace-content {
  padding: var(--eo-spacing-2xl) 0;
}

.sidebar-card {
  background: white;
  border-radius: var(--eo-radius-xl);
  box-shadow: var(--eo-shadow-md);
  padding: var(--eo-spacing-lg);
  position: sticky;
  top: var(--eo-spacing-lg);
}

.sidebar-menu {
  display: flex;
  flex-direction: column;
  gap: var(--eo-spacing-xs);
  margin-bottom: var(--eo-spacing-xl);
}

.menu-item {
  display: flex;
  align-items: center;
  padding: var(--eo-spacing-md);
  border-radius: var(--eo-radius-lg);
  cursor: pointer;
  transition: all var(--eo-transition-base);
  color: var(--eo-gray-600);
  font-weight: 500;
}

.menu-item:hover {
  background: var(--eo-gray-100);
  color: var(--eo-primary);
}

.menu-item.active {
  background: var(--eo-primary);
  color: white;
}

.menu-item i {
  font-size: 1.1rem;
}

.sidebar-footer {
  padding-top: var(--eo-spacing-lg);
  border-top: 1px solid var(--eo-gray-200);
}

.content-section {
  background: white;
  border-radius: var(--eo-radius-xl);
  box-shadow: var(--eo-shadow-md);
  padding: var(--eo-spacing-xl);
}

.section-title {
  color: var(--eo-primary);
  font-size: var(--eo-font-size-2xl);
  font-weight: 700;
  margin-bottom: var(--eo-spacing-xl);
  font-family: var(--eo-font-family);
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: var(--eo-spacing-lg);
  margin-bottom: var(--eo-spacing-2xl);
}

.stat-card {
  background: var(--eo-gray-50);
  border-radius: var(--eo-radius-xl);
  padding: var(--eo-spacing-lg);
  display: flex;
  align-items: center;
  gap: var(--eo-spacing-md);
  transition: all var(--eo-transition-base);
}

.stat-card:hover {
  transform: translateY(-4px);
  box-shadow: var(--eo-shadow-md);
}

.stat-icon {
  width: 60px;
  height: 60px;
  border-radius: var(--eo-radius-lg);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.5rem;
  color: white;
  flex-shrink: 0;
}

.stat-icon.courses {
  background: var(--eo-primary);
}

.stat-icon.completed {
  background: var(--eo-success);
}

.stat-icon.certificates {
  background: var(--eo-secondary);
}

.stat-icon.hours {
  background: var(--eo-info);
}

.stat-content {
  flex: 1;
}

.stat-value {
  font-size: var(--eo-font-size-2xl);
  font-weight: 700;
  margin: 0;
  color: var(--eo-gray-800);
}

.stat-label {
  font-size: var(--eo-font-size-sm);
  color: var(--eo-gray-500);
  margin: 0;
}

.recent-activity {
  margin-top: var(--eo-spacing-2xl);
}

.activity-list {
  display: flex;
  flex-direction: column;
  gap: var(--eo-spacing-md);
}

.activity-item {
  display: flex;
  align-items: center;
  gap: var(--eo-spacing-md);
  padding: var(--eo-spacing-md);
  border-radius: var(--eo-radius-lg);
  background: var(--eo-gray-50);
  transition: all var(--eo-transition-base);
}

.activity-item:hover {
  background: var(--eo-gray-100);
}

.activity-icon {
  width: 50px;
  height: 50px;
  border-radius: var(--eo-radius-lg);
  background: var(--eo-primary);
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.3rem;
  flex-shrink: 0;
}

.activity-icon.completed {
  background: var(--eo-success);
}

.activity-content {
  flex: 1;
}

.activity-title {
  font-size: var(--eo-font-size-lg);
  font-weight: 600;
  margin: 0;
  color: var(--eo-gray-800);
}

.activity-meta {
  font-size: var(--eo-font-size-sm);
  color: var(--eo-gray-500);
  margin: var(--eo-spacing-xs) 0 0 0;
}

.activity-progress {
  width: 120px;
  text-align: right;
}

.activity-progress small {
  display: block;
  margin-top: var(--eo-spacing-xs);
  font-size: var(--eo-font-size-xs);
  color: var(--eo-gray-500);
}

.activity-badge {
  flex-shrink: 0;
}

.courses-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: var(--eo-spacing-lg);
}

.course-card {
  border: 1px solid var(--eo-gray-200);
  border-radius: var(--eo-radius-xl);
  overflow: hidden;
  transition: all var(--eo-transition-base);
}

.course-card:hover {
  box-shadow: var(--eo-shadow-lg);
  transform: translateY(-4px);
}

.course-image {
  height: 150px;
  background: linear-gradient(135deg, var(--eo-primary) 0%, var(--eo-primary-light) 100%);
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
}

.course-category {
  background: rgba(255, 255, 255, 0.9);
  padding: var(--eo-spacing-sm) var(--eo-spacing-md);
  border-radius: var(--eo-radius-full);
  font-size: var(--eo-font-size-sm);
  font-weight: 600;
  color: var(--eo-primary);
}

.course-content {
  padding: var(--eo-spacing-lg);
}

.course-title {
  font-size: var(--eo-font-size-lg);
  font-weight: 600;
  margin: 0 0 var(--eo-spacing-sm) 0;
  color: var(--eo-gray-800);
}

.course-description {
  font-size: var(--eo-font-size-sm);
  color: var(--eo-gray-600);
  margin: 0 0 var(--eo-spacing-md) 0;
}

.course-meta {
  display: flex;
  gap: var(--eo-spacing-md);
  font-size: var(--eo-font-size-sm);
  color: var(--eo-gray-500);
  margin-bottom: var(--eo-spacing-md);
}

.course-progress {
  margin-bottom: var(--eo-spacing-md);
}

.course-progress small {
  display: block;
  margin-top: var(--eo-spacing-xs);
  font-size: var(--eo-font-size-xs);
  color: var(--eo-gray-500);
}

.certificates-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: var(--eo-spacing-lg);
}

.certificate-card {
  display: flex;
  align-items: center;
  gap: var(--eo-spacing-md);
  padding: var(--eo-spacing-lg);
  border: 1px solid var(--eo-gray-200);
  border-radius: var(--eo-radius-xl);
  transition: all var(--eo-transition-base);
}

.certificate-card:hover {
  box-shadow: var(--eo-shadow-md);
  border-color: var(--eo-primary);
}

.certificate-icon {
  width: 60px;
  height: 60px;
  border-radius: var(--eo-radius-lg);
  background: var(--eo-secondary);
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.5rem;
  flex-shrink: 0;
}

.certificate-content {
  flex: 1;
}

.certificate-title {
  font-size: var(--eo-font-size-lg);
  font-weight: 600;
  margin: 0 0 var(--eo-spacing-xs) 0;
  color: var(--eo-gray-800);
}

.certificate-date {
  font-size: var(--eo-font-size-sm);
  color: var(--eo-gray-500);
  margin: 0 0 var(--eo-spacing-md) 0;
}

.profile-card,
.settings-card {
  background: var(--eo-gray-50);
  border-radius: var(--eo-radius-xl);
  padding: var(--eo-spacing-xl);
}

.setting-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: var(--eo-spacing-lg) 0;
  border-bottom: 1px solid var(--eo-gray-200);
}

.setting-item:last-child {
  border-bottom: none;
}

.setting-info h4 {
  font-size: var(--eo-font-size-lg);
  font-weight: 600;
  margin: 0 0 var(--eo-spacing-xs) 0;
  color: var(--eo-gray-800);
}

.setting-info p {
  font-size: var(--eo-font-size-sm);
  color: var(--eo-gray-500);
  margin: 0;
}

.form-check-input:checked {
  background-color: var(--eo-primary);
  border-color: var(--eo-primary);
}

.btn-primary {
  background-color: var(--eo-primary);
  border-color: var(--eo-primary);
}

.btn-primary:hover {
  background-color: var(--eo-primary-dark);
  border-color: var(--eo-primary-dark);
}

.btn-outline-primary {
  color: var(--eo-primary);
  border-color: var(--eo-primary);
}

.btn-outline-primary:hover {
  background-color: var(--eo-primary);
  border-color: var(--eo-primary);
}

.progress-bar {
  background-color: var(--eo-primary);
}

@media (max-width: 992px) {
  .sidebar-card {
    position: static;
  }
  
  .stats-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 768px) {
  .user-welcome {
    flex-direction: column;
    text-align: center;
  }
  
  .welcome-title {
    font-size: var(--eo-font-size-2xl);
  }
  
  .stats-grid {
    grid-template-columns: 1fr;
  }
  
  .courses-grid,
  .certificates-grid {
    grid-template-columns: 1fr;
  }
  
  .activity-item {
    flex-direction: column;
    text-align: center;
  }
  
  .activity-progress {
    width: 100%;
    text-align: center;
  }
}
</style>
