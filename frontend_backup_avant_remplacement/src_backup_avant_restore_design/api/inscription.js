import api from "./axios";

export const inscrireFormation = (data) => {
  return api.post("/inscriptions/inscrire", data);
};