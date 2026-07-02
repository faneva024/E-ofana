import api from "./api";

export const payerInscription = (data) => {
  return api.post("/paiements/payer", data);
};