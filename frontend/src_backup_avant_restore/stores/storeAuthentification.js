import { defineStore } from "pinia";

export const useAuthStore = defineStore("auth", {
  state: () => ({
    token: null,
    utilisateur: null
  }),

  actions: {
    definirToken(token) {
      this.token = token;
      localStorage.setItem("token", token);
    },

    obtenirToken() {
      if (!this.token) {
        this.token = localStorage.getItem("token");
      }
      return this.token;
    },

    estConnecte() {
      return !!this.obtenirToken();
    },

    seDeconnecter() {
      this.token = null;
      this.utilisateur = null;
      localStorage.removeItem("token");
    }
  }
});