import { createRouter, createWebHistory } from "vue-router";

const routes = [
  {
    path: "/",
    component: () => import("../layouts/LayoutPrincipal.vue"),
    children: [
      {
        path: "",
        name: "Home",
        component: () => import("../pages/Home.vue"),
      },
      {
        path: "home",
        name: "HomePage",
        component: () => import("../pages/Home.vue"),
      },
      {
        path: "recherche",
        name: "Recherche",
        component: () => import("../pages/RechercheFormations.vue"),
      },
      {
        path: "recherche-formations",
        name: "RechercheFormations",
        component: () => import("../pages/RechercheFormations.vue"),
      },
      {
        path: "formations",
        name: "Formations",
        component: () => import("../pages/RechercheFormations.vue"),
      },
      {
        path: "formations/:id",
        name: "DetailsFormations",
        component: () =>
          import("../vues/apprenant/VueDetailsFormations.vue"),
      },
      {
        path: "details",
        redirect: "/formations/1",
      },
      {
        path: "connexion",
        name: "Connexion",
        component: () =>
          import("../vues/authentification/VueConnexion.vue"),
      },
      {
        path: "inscription",
        name: "Inscription",
        component: () =>
          import("../vues/authentification/VueInscription.vue"),
      },
      {
        path: "mon-espace",
        name: "MonEspace",
        component: () => import("../vues/MonEspace.vue"),
        meta: { requiresAuth: true },
      },
      {
        path: "accueil",
        name: "Accueil",
        component: () => import("../vues/apprenant/VueAccueil.vue"),
      },
      {
        path: "apprenant/accueil",
        name: "ApprenantAccueil",
        component: () => import("../vues/apprenant/VueAccueil.vue"),
        meta: { requiresAuth: true },
      },
      {
        path: "apprenant/mon-espace",
        name: "ApprenantMonEspace",
        component: () => import("../vues/apprenant/VueMonEspace.vue"),
        meta: { requiresAuth: true },
      },
      {
        path: "mon-formulaire",
        name: "FormulaireInscription",
        component: () =>
          import("../composants/FormulaireInscription.vue"),
        meta: { requiresAuth: true },
      },
      {
        path: "mon-formulaire-reservation",
        name: "FormulaireReservation",
        component: () =>
          import("../composants/FormulaireReservation.vue"),
        meta: { requiresAuth: true },
      },
      {
        path: "admin/tableau-de-bord",
        name: "AdminTableauDeBord",
        component: () =>
          import("../vues/admin/VueTableauDeBordAdmin.vue"),
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
    component: () => import("../layouts/LayoutFormateur.vue"),
    children: [
      {
        path: "",
        redirect: "/formateur/dashboard",
      },
      {
        path: "dashboard",
        name: "FormateurTableauDeBord",
        component: () =>
          import("../vues/formateur/VueTableauDeBord.vue"),
        meta: { requiresAuthFormateur: true },
      },
      {
        path: "formations",
        name: "MesFormations",
        component: () =>
          import("../vues/formateur/VueMesFormations.vue"),
        meta: { requiresAuthFormateur: true },
      },
      {
        path: "creation",
        name: "FormateurCreation",
        component: () =>
          import("../vues/formateur/VueFormulaireFormation.vue"),
        meta: { requiresAuthFormateur: true },
      },
      {
        path: "modification/:id",
        name: "FormateurModification",
        component: () =>
          import("../vues/formateur/VueFormulaireFormation.vue"),
        meta: { requiresAuthFormateur: true },
      },
      {
        path: "profil-centre",
        name: "FormateurProfilCentre",
        component: () =>
          import("../vues/formateur/VueProfilCentre.vue"),
        meta: { requiresAuthFormateur: true },
      },
      {
        path: "liste-inscrits",
        name: "FormateurListeInscrits",
        component: () =>
          import("../vues/formateur/VueListeInscrits.vue"),
        meta: { requiresAuthFormateur: true },
      },
      {
        path: "finances",
        name: "FormateurFinances",
        component: () =>
          import("../vues/formateur/VueFinances.vue"),
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
  const auth = localStorage.getItem("auth");
  const token = localStorage.getItem("token") || localStorage.getItem("authToken");

  const authFormateur = localStorage.getItem("authFormateur");
  const formateurToken = localStorage.getItem("formateurToken");

  const authAdmin = localStorage.getItem("authAdmin");
  const adminToken = localStorage.getItem("adminToken");

  if (to.meta.requiresAuth && !auth && !token) {
    next("/connexion");
    return;
  }

  if (to.meta.requiresAuthFormateur && !authFormateur && !formateurToken) {
    next("/formateur/connexion");
    return;
  }

  if (to.meta.requiresAuthAdmin && !authAdmin && !adminToken) {
    next("/connexion");
    return;
  }

  next();
});

export default router;