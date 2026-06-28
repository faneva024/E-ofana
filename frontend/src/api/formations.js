import axios from "axios";

const API_URL = import.meta.env.VITE_API_URL || "/api/v1";

export const mockFormations = [];

export function getFormations() {
  return axios.get(`${API_URL}/formations`);
}