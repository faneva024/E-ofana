import { defineStore } from "pinia";
import router from "../router";
import api from "../api/axios";

export const useAuthStore = defineStore("auth", {
  state: () => ({
    user: null,
    token: null,
    loading: false,
    error: null,
  }),

  getters: {
    isAuthenticated: (state) => !!state.token,
    currentUser: (state) => state.user,
  },

  actions: {
    setUser(user, token) {
      this.user = user;
      this.token = token;
      this.error = null;

      localStorage.setItem("auth", JSON.stringify({ user, token }));
      localStorage.setItem("token", token);
      localStorage.setItem("authToken", token);
      localStorage.setItem("utilisateur", JSON.stringify(user));
    },

    loadFromStorage() {
      const raw = localStorage.getItem("auth");

      if (!raw) {
        return;
      }

      try {
        const { user, token } = JSON.parse(raw);

        this.user = user;
        this.token = token;

        if (token) {
          localStorage.setItem("token", token);
          localStorage.setItem("authToken", token);
        }

        if (user) {
          localStorage.setItem("utilisateur", JSON.stringify(user));
        }
      } catch (e) {
        console.error("Erreur lecture auth", e);
        this.logout();
      }
    },

    logout() {
      this.user = null;
      this.token = null;
      this.error = null;

      localStorage.removeItem("auth");
      localStorage.removeItem("token");
      localStorage.removeItem("authToken");
      localStorage.removeItem("utilisateur");

      router.push({ name: "Home" });
    },

    async login(email, password) {
      this.loading = true;
      this.error = null;

      try {
        const response = await api.post("/auth/connexion", {
          email,
          motDePasse: password,
        });

        const data = response.data;

        const user = {
          idUser: data.idUser,
          id: data.idUser,
          nom: data.nom,
          prenom: data.prenom,
          email: data.email,
          telephone: data.telephone,
          role: data.role,
        };

        this.setUser(user, data.token || `TOKEN-DEMO-${data.idUser}`);

        router.push({ name: "MonEspace" });

        return { success: true };
      } catch (error) {
        this.error =
          error.response?.data?.message || "Erreur de connexion apprenant";
        return { success: false, error: this.error };
      } finally {
        this.loading = false;
      }
    },

    async register(userData) {
      this.loading = true;
      this.error = null;

      try {
        const response = await api.post("/utilisateurs", {
          nom: userData.nom,
          prenom: userData.prenom,
          email: userData.email,
          motDePasse: userData.password || userData.motDePasse,
          telephone: userData.telephone,
          role: "apprenant",
          avatar: "",
        });

        const data = response.data;

        const user = {
          idUser: data.idUser,
          id: data.idUser,
          nom: data.nom,
          prenom: data.prenom,
          email: data.email,
          telephone: data.telephone,
          role: data.role,
        };

        this.setUser(user, `TOKEN-DEMO-${data.idUser}`);

        router.push({ name: "MonEspace" });

        return { success: true };
      } catch (error) {
        this.error =
          error.response?.data?.message || "Erreur lors de l'inscription";
        return { success: false, error: this.error };
      } finally {
        this.loading = false;
      }
    },
  },
});