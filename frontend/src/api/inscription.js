import api from "./api";

// idUser n'est plus envoyé : le backend lit l'utilisateur connecté
// depuis le token JWT (Authorization: Bearer ...), jamais depuis le
// corps de la requête.
export const inscrireFormation = (idSession) => {
  return api.post("/inscriptions/inscrire", { idSession });
};

export const getSessionsOuvertes = (idFormation) => {
  return api.get(`/formations/${idFormation}/sessions`);
};
