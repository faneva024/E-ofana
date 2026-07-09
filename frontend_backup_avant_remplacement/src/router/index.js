import { createRouter, createWebHistory } from "vue-router";

import Home from "../pages/Home.vue";
import RechercheFormations from "../pages/RechercheFormations.vue";
import DetailFormation from "../pages/DetailFormation.vue";

import VueConnexion from "../vues/authentification/VueConnexion.vue";
import VueInscription from "../vues/authentification/VueInscription.vue";
import MonEspace from "../vues/MonEspace.vue";
import VueRecherche from "../vues/apprenant/VueRecherche.vue";

import LayoutFormateur from "../layouts/LayoutFormateur.vue";
import VueConnexionFormateur from "../vues/formateur/authentification/VueConnexionFormateur.vue";
import VueTableauDeBordFormateur from "../vues/formateur/VueTableauDeBord.vue";
import VueMesFormations from "../vues/formateur/VueMesFormations.vue";
import VueFormulaireFormation from "../vues/formateur/VueFormulaireFormation.vue";
import VueProfilCentre from "../vues/formateur/VueProfilCentre.vue";
import VueListeInscrits from "../vues/formateur/VueListeInscrits.vue";
import VueFinances from "../vues/formateur/VueFinances.vue";

import VueTableauDeBordAdmin from "../vues/admin/VueTableauDeBordAdmin.vue";

const routes = [
  {
    path: "/",
    redirect: "/home",
  },
  {
    path: "/home",
    name: "Home",
    component: Home,
  },
  {
    path: "/formations",
    name: "RechercheFormations",
    component: RechercheFormations,
  },
  {
    path: "/recherche",
    name: "Recherche",
    component: VueRecherche,
  },
  {
    path: "/formations/:id",
    name: "DetailFormation",
    component: DetailFormation,
  },
  {
    path: "/details/:id",
    name: "DetailsFormations",
    component: DetailFormation,
  },
  {
    path: "/details",
    redirect: "/formations/1",
  },
  {
    path: "/connexion",
    name: "Connexion",
    component: VueConnexion,
  },
  {
    path: "/inscription",
    name: "Inscription",
    component: VueInscription,
  },
  {
    path: "/mon-espace",
    name: "MonEspace",
    component: MonEspace,
    meta: {
      requiresAuth: true,
    },
  },

  {
    path: "/formateur/connexion",
    name: "ConnexionFormateur",
    component: VueConnexionFormateur,
  },
  {
    path: "/formateur",
    component: LayoutFormateur,
    meta: {
      requiresAuthFormateur: true,
    },
    children: [
      {
        path: "",
        redirect: "/formateur/dashboard",
      },
      {
        path: "dashboard",
        name: "TableauDeBordFormateur",
        component: VueTableauDeBordFormateur,
      },
      {
        path: "formations",
        name: "MesFormations",
        component: VueMesFormations,
      },
      {
        path: "formations/nouveau",
        name: "NouvelleFormation",
        component: VueFormulaireFormation,
      },
      {
        path: "formations/:id/modifier",
        name: "ModifierFormation",
        component: VueFormulaireFormation,
      },
      {
        path: "profil-centre",
        name: "ProfilCentre",
        component: VueProfilCentre,
      },
      {
        path: "liste-inscrits",
        name: "ListeInscrits",
        component: VueListeInscrits,
      },
      {
        path: "finances",
        name: "FinancesFormateur",
        component: VueFinances,
      },
    ],
  },

  {
    path: "/admin/dashboard",
    name: "TableauDeBordAdmin",
    component: VueTableauDeBordAdmin,
    meta: {
      requiresAuthAdmin: true,
    },
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
  const apprenantToken =
    localStorage.getItem("token") || localStorage.getItem("authToken");

  const formateurToken = localStorage.getItem("formateurToken");
  const adminToken = localStorage.getItem("adminToken");

  if (to.meta.requiresAuth && !apprenantToken) {
    next("/connexion");
    return;
  }

  if (to.meta.requiresAuthFormateur && !formateurToken) {
    next("/formateur/connexion");
    return;
  }

  if (to.meta.requiresAuthAdmin && !adminToken) {
    next("/home");
    return;
  }

  next();
});

export default router;