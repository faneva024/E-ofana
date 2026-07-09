import { createRouter, createWebHistory } from "vue-router";

import LayoutPrincipal from "../layouts/LayoutPrincipal.vue";
import LayoutFormateur from "../layouts/LayoutFormateur.vue";

import Home from "../pages/Home.vue";
import RechercheFormations from "../pages/RechercheFormations.vue";

import VueInscription from "../vues/authentification/VueInscription.vue";
import VueConnexion from "../vues/authentification/VueConnexion.vue";

import MonEspace from "../vues/MonEspace.vue";
import VueAccueil from "../vues/apprenant/VueAccueil.vue";
import VueMonEspace from "../vues/apprenant/VueMonEspace.vue";
import VueRecherche from "../vues/apprenant/VueRecherche.vue";
import VueDetailsFormations from "../vues/apprenant/VueDetailsFormations.vue";

import FormulaireReservation from "../composants/FormulaireReservation.vue";
import FormulaireInscription from "../composants/FormulaireInscription.vue";

import VueMesFormations from "../vues/formateur/VueMesFormations.vue";
import VueTableauDeBord from "../vues/formateur/VueTableauDeBord.vue";

const routes = [
  {
    path: "/",
    component: LayoutPrincipal,
    children: [
      {
        path: "",
        name: "Home",
        component: Home,
      },
      {
        path: "home",
        name: "HomePage",
        component: Home,
      },
      {
        path: "recherche-formations",
        name: "RechercheFormations",
        component: RechercheFormations,
      },
      {
        path: "recherche",
        name: "Recherche",
        component: VueRecherche,
      },
      {
        path: "formations",
        name: "Formations",
        component: VueRecherche,
      },
      {
        path: "formations/:id",
        name: "DetailsFormationById",
        component: VueDetailsFormations,
      },
      {
        path: "details",
        name: "DetailsFormations",
        component: VueDetailsFormations,
      },
      {
        path: "inscription",
        name: "Inscription",
        component: VueInscription,
      },
      {
        path: "connexion",
        name: "Connexion",
        component: VueConnexion,
      },
      {
        path: "mon-espace",
        name: "MonEspace",
        component: MonEspace,
        meta: { requiresAuth: true },
      },
      {
        path: "accueil",
        name: "Accueil",
        component: VueAccueil,
      },
      {
        path: "apprenant/accueil",
        name: "ApprenantAccueil",
        component: VueAccueil,
        meta: { requiresAuth: true },
      },
      {
        path: "apprenant/mon-espace",
        name: "ApprenantMonEspace",
        component: VueMonEspace,
        meta: { requiresAuth: true },
      },
      {
        path: "mon-formulaire",
        name: "FormulaireInscription",
        component: FormulaireInscription,
        meta: { requiresAuth: true },
      },
      {
        path: "mon-formulaire-reservation",
        name: "FormulaireReservation",
        component: FormulaireReservation,
        meta: { requiresAuth: true },
      },
      {
        path: "admin/tableau-de-bord",
        name: "AdminTableauDeBord",
        component: () => import("../vues/admin/VueTableauDeBordAdmin.vue"),
        meta: { requiresAuthAdmin: true },
      },
    ],
  },

  {
    path: "/formateur/connexion",
    name: "ConnexionFormateur",
    component: () =>
      import("../vues/formateur/authentification/VueConnexionFormateur.vue"),
  },

  {
    path: "/formateur",
    component: LayoutFormateur,
    children: [
      {
        path: "",
        redirect: "/formateur/dashboard",
      },
      {
        path: "dashboard",
        name: "FormateurTableauDeBord",
        component: VueTableauDeBord,
        meta: { requiresAuthFormateur: true },
      },
      {
        path: "formations",
        name: "MesFormations",
        component: VueMesFormations,
        meta: { requiresAuthFormateur: true },
      },
      {
        path: "profil-centre",
        name: "FormateurProfilCentre",
        component: () => import("../vues/formateur/VueProfilCentre.vue"),
        meta: { requiresAuthFormateur: true },
      },
      {
        path: "liste-inscrits",
        name: "FormateurListeInscrits",
        component: () => import("../vues/formateur/VueListeInscrits.vue"),
        meta: { requiresAuthFormateur: true },
      },
      {
        path: "finances",
        name: "FormateurFinances",
        component: () => import("../vues/formateur/VueFinances.vue"),
        meta: { requiresAuthFormateur: true },
      },
      {
        path: "creation",
        name: "FormateurCreation",
        component: () => import("../vues/formateur/VueFormulaireFormation.vue"),
        meta: { requiresAuthFormateur: true },
      },
      {
        path: "modification/:id",
        name: "FormateurModification",
        component: () => import("../vues/formateur/VueFormulaireFormation.vue"),
        meta: { requiresAuthFormateur: true },
      },
    ],
  },

  {
    path: "/:pathMatch(.*)*",
    redirect: "/home",
  },
];

const router = createRouter({
  history: createWebHistory(),
  routes,
});

router.beforeEach((to, from, next) => {
  const token = localStorage.getItem("token") || localStorage.getItem("authToken");
  const formateurToken = localStorage.getItem("formateurToken");
  const adminToken = localStorage.getItem("adminToken");

  if (to.meta.requiresAuth && !token) {
    next("/connexion");
    return;
  }

  if (to.meta.requiresAuthFormateur && !formateurToken) {
    next("/formateur/connexion");
    return;
  }

  if (to.meta.requiresAuthAdmin && !adminToken) {
    next("/connexion");
    return;
  }

  next();
});

export default router;