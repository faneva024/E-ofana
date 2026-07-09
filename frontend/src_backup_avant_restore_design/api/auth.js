import api from "./api";

export const connexion = (data) => {
  return api.post("/auth/connexion", data);
};