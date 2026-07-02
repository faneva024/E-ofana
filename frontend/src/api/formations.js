import api from "./api";

export const getFormations = (params = {}) => {
  return api.get("/formations", { params });
};

export const getFormationById = (id) => {
  return api.get(`/formations/${id}`);
};

export const createFormation = (data) => {
  return api.post("/formations", data);
};