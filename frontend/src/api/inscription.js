import api from "./api";

export const inscrireFormation = (data) => {
  return api.post("/inscriptions/inscrire", data);
};

export const reserverFormation = (data) => {
  return api.post("/inscriptions/reserver", data);
};