import api from "./api";

export const getFormations = () => {
  return api.get("/formations");
};

export const getFormationById = (id) => {
  return api.get(`/formations/${id}`);
};

export const createFormation = (data) => {
  return api.post("/formations", data);
};