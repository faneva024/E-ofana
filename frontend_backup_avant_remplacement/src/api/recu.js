import api from "./api";

export const telechargerRecuPdf = (idInscription) => {
  return api.get(`/recus/${idInscription}/pdf`, {
    responseType: "blob",
  });
};