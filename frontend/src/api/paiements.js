import api from "./api";

// Remplace l'ancienne route POST /paiements/payer, retirée car elle
// remettait remise/commission à zéro et bloquait tout second paiement
// (impossible de gérer l'acompte + solde d'une réservation).
export const payerInscription = (idInscription, data) => {
  return api.post(`/inscriptions/${idInscription}/confirmer-paiement`, data);
};
