import axios from "axios";

const api = axios.create({
  baseURL: import.meta.env.VITE_API_URL || "http://localhost:8081/api/v1",
  headers: {
    "Content-Type": "application/json",
  },
});

api.interceptors.request.use(
  (config) => {
    const formateurToken = localStorage.getItem("formateurToken");
    const adminToken = localStorage.getItem("adminToken");
    const apprenantToken =
      localStorage.getItem("token") || localStorage.getItem("authToken");

    const token = formateurToken || adminToken || apprenantToken;

    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }

    return config;
  },
  (error) => Promise.reject(error)
);

export default api;