import api from "./api";

export const creerUtilisateur = (data) => {
  return api.post("/utilisateurs", data);
};

export const getUtilisateurs = () => {
  return api.get("/utilisateurs");
};