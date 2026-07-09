<template>
    <div class="navigation-links-wrapper">
        <div class="navigation-links">
            <router-link v-for="item in menuItems" :key="item.path" :to="item.path" class="sidebar-link"
                @click="$emit('link-clicked')">
                <component :is="item.icon" />
                <span>{{ item.label }}</span>
            </router-link>
        </div>

        <div>
            <div class="sidebar-divider"></div>
            <a class="sidebar-link logout-btn" @click.prevent="$emit('logout')" href="">
                <svg fill="none" height="18" stroke="currentColor" stroke-linecap="round" stroke-linejoin="round"
                    stroke-width="2" width="18" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24">
                    <path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"></path>
                    <polyline points="16 17 21 12 16 7"></polyline>
                    <line x1="21" y1="12" x2="9" y2="12"></line>
                </svg>
                <span>Déconnexion</span>
            </a>
        </div>
    </div>
</template>

<script setup>
import { h } from 'vue'

defineEmits(['logout', 'link-clicked'])

// Icônes Inline valides pour Vue 3 (Composants fonctionnels)
const IconDashboard = () => h('svg', { fill: 'none', height: '18', stroke: 'currentColor', 'stroke-linecap': 'round', 'stroke-linejoin': 'round', 'stroke-width': '2', width: '18', viewBox: '0 0 24 24' }, [
  h('rect', { x: '3', y: '3', width: '7', height: '9' }),
  h('rect', { x: '14', y: '3', width: '7', height: '5' }),
  h('rect', { x: '14', y: '12', width: '7', height: '9' }),
  h('rect', { x: '3', y: '16', width: '7', height: '5' })
])

const IconFormations = () => h('svg', { fill: 'none', height: '18', stroke: 'currentColor', 'stroke-linecap': 'round', 'stroke-linejoin': 'round', 'stroke-width': '2', width: '18', viewBox: '0 0 24 24' }, [
  h('path', { d: 'M4 19.5A2.5 2.5 0 0 1 6.5 17H20' }),
  h('path', { d: 'M6.5 2H20v20H6.5A2.5 2.5 0 0 1 4 19.5v-15A2.5 2.5 0 0 1 6.5 2z' })
])

const IconProfil = () => h('svg', { fill: 'none', height: '18', stroke: 'currentColor', 'stroke-linecap': 'round', 'stroke-linejoin': 'round', 'stroke-width': '2', width: '18', viewBox: '0 0 24 24' }, [
  h('path', { d: 'M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2' }),
  h('circle', { cx: '12', cy: '7', r: '4' })
])

const IconFinances = () => h('svg', { fill: 'none', height: '18', stroke: 'currentColor', 'stroke-linecap': 'round', 'stroke-linejoin': 'round', 'stroke-width': '2', width: '18', viewBox: '0 0 24 24' }, [
  h('line', { x1: '18', y1: '20', x2: '18', y2: '10' }),
  h('line', { x1: '12', y1: '20', x2: '12', y2: '4' }),
  h('line', { x1: '6', y1: '20', x2: '6', y2: '14' })
])


const IconInscrits = () => h('svg', { fill: 'none', height: '18', stroke: 'currentColor', 'stroke-linecap': 'round', 'stroke-linejoin': 'round', 'stroke-width': '2', width: '18', viewBox: '0 0 24 24' }, [
  h('path', { d: 'M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2' }),
  h('circle', { cx: '9', cy: '7', r: '4' }),
  h('path', { d: 'M23 21v-2a4 4 0 0 0-3-3.87' }),
  h('path', { d: 'M16 3.13a4 4 0 0 1 0 7.75' })
])
const menuItems = [
  { label: 'Tableau de bord', path: '/formateur/dashboard', icon: IconDashboard },
  { label: 'Mes formations', path: '/formateur/formations', icon: IconFormations },
  { label: 'Liste des inscrits', path: '/formateur/liste-inscrits', icon: IconInscrits },
  { label: 'Mon profil', path: '/formateur/profil-centre', icon: IconProfil },
  { label: 'Finances', path: '/formateur/finances', icon: IconFinances }
]
</script>

<style scoped>
.navigation-links-wrapper {
    display: flex;
    flex-direction: column;
    justify-content: space-between;
    height: 100%;
}

.sidebar-link {
    display: flex;
    align-items: center;
    gap: 12px;
    padding: 0.85rem 1.5rem;
    color: #a0a0a0;
    text-decoration: none;
    font-weight: 500;
    font-size: 0.95rem;
    transition: all 0.2s ease;
    cursor: pointer;
}

.sidebar-link:hover,
.router-link-active {
    color: #ffffff;
    background-color: rgba(255, 255, 255, 0.03);
}

.router-link-active {
    background-color: rgba(198, 156, 80, 0.1) !important;
    color: #c69c50 !important;
    font-weight: 600;
}

.sidebar-divider {
    border-top: 1px solid #2d2d2d;
    margin: 1rem 0;
}

.logout-btn:hover {
    background-color: rgba(211, 47, 47, 0.1);
    color: #d32f2f;
}
</style>