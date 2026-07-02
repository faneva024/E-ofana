import api from "./api";

export const inscrireFormation = (data) => {
  return api.post("/inscriptions/inscrire", data);
};