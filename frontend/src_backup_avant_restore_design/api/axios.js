import axios from "axios";

const api = axios.create({
  baseURL: import.meta.env.VITE_API_URL || "http://localhost:8080/api/v1",
  headers: {
    "Content-Type": "application/json",
  },
});

api.interceptors.request.use((config) => {
  const rawAuth = localStorage.getItem("auth");
  const rawFormateur = localStorage.getItem("authFormateur");
  const rawAdmin = localStorage.getItem("authAdmin");

  let token =
    localStorage.getItem("token") ||
    localStorage.getItem("authToken") ||
    localStorage.getItem("formateurToken") ||
    localStorage.getItem("adminToken");

  if (!token && rawAuth) {
    try {
      token = JSON.parse(rawAuth)?.token;
    } catch (e) {
      console.error("Erreur lecture auth", e);
    }
  }

  if (!token && rawFormateur) {
    try {
      token = JSON.parse(rawFormateur)?.token;
    } catch (e) {
      console.error("Erreur lecture authFormateur", e);
    }
  }

  if (!token && rawAdmin) {
    try {
      token = JSON.parse(rawAdmin)?.token;
    } catch (e) {
      console.error("Erreur lecture authAdmin", e);
    }
  }

  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }

  return config;
});

export default api;