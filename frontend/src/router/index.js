import { createRouter, createWebHistory } from 'vue-router'

import LayoutPrincipal from '../layouts/LayoutPrincipal.vue'
import Home from '../pages/Home.vue'
import RechercheFormations from '../pages/RechercheFormations.vue'
import VueInscription from '../vues/authentification/VueInscription.vue'
import VueConnexion from '../vues/authentification/VueConnexion.vue'
import MonEspace from '../vues/MonEspace.vue'
import VueAccueil from '../vues/apprenant/VueAccueil.vue'
import VueMonEspace from '../vues/apprenant/VueMonEspace.vue'

const routes = [
  {
    path: '/',
    component: LayoutPrincipal,
    children: [
      {
        path: '',
        name: 'Home',
        component: Home
      },
      {
        path: 'home',
        name: 'HomePage',
        component: Home
      },
      {
        path: 'recherche-formations',
        name: 'RechercheFormations',
        component: RechercheFormations
      },
      {
        path: 'formations',
        name: 'Formations',
        component: RechercheFormations
      },
      {
        path: 'inscription',
        name: 'Inscription',
        component: VueInscription
      },
      {
        path: 'connexion',
        name: 'Connexion',
        component: VueConnexion
      },
      {
        path: 'mon-espace',
        name: 'MonEspace',
        component: MonEspace,
        meta: { requiresAuth: true }
      },
      {
        path: 'accueil',
        name: 'Accueil',
        component: VueAccueil
      },
      {
        path: 'mon-espace-apprenant',
        name: 'MonEspaceApprenant',
        component: VueMonEspace
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router