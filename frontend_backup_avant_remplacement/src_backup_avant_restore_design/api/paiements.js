import api from "./axios";

export const payerInscription = (data) => {
  return api.post("/paiements/payer", {
    idInscription: data.idInscription,
    montant: data.montant,
    operateur: data.operateur,
    numeroTransaction: data.numeroTransaction || `TXN-${Date.now()}`,
  });
};