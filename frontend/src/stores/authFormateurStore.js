import { defineStore } from "pinia";
import api from "../api/api";

export const useAuthFormateurStore = defineStore("authFormateur", {
  state: () => ({
    formateur: null,
    token: null,
    loading: false,
    error: null,
  }),

  getters: {
    isAuthenticated: (state) => !!state.token,
    currentFormateur: (state) => state.formateur,
  },

  actions: {
    setFormateur(formateur, token) {
      this.formateur = formateur;
      this.token = token;
      this.error = null;

      localStorage.setItem("authFormateur", JSON.stringify({ formateur, token }));
      localStorage.setItem("formateurToken", token);
    },

    loadFromStorage() {
      const raw = localStorage.getItem("authFormateur");

      if (!raw) return;

      try {
        const { formateur, token } = JSON.parse(raw);

        this.formateur = formateur;
        this.token = token;

        if (token) {
          localStorage.setItem("formateurToken", token);
        }
      } catch (e) {
        console.error("Erreur lecture authFormateur", e);
        this.logout();
      }
    },

    logout() {
      this.formateur = null;
      this.token = null;
      this.error = null;

      localStorage.removeItem("authFormateur");
      localStorage.removeItem("formateurToken");
    },

    async login(email, password) {
      this.loading = true;
      this.error = null;

      try {
        const response = await api.post("/auth-formateur/connexion", {
          email,
          motDePasse: password,
        });

        const data = response.data;

        const formateur = {
          idUser: data.idUser,
          id: data.idUser,
          nom: data.nom,
          prenom: data.prenom,
          email: data.email,
          telephone: data.telephone,
          role: data.typeUtilisateur || "formateur",
        };

        this.setFormateur(
          formateur,
          data.token || `TOKEN-DEMO-FORMATEUR-${data.idUser}`
        );

        return { success: true };
      } catch (error) {
        this.error =
          error.response?.data?.message || "Erreur de connexion formateur";

        return { success: false, error: this.error };
      } finally {
        this.loading = false;
      }
    },
  },
});