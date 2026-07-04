package com.eofana.dto;

/** Réponse renvoyée après inscription ou réservation (T-B-011). */
public record EnrollResponse(
        Long idInscription,
        String typeInsc,
        String statut,
        long montantDu,
        long montantPayeMaintenant,
        long remise,
        long commission,
        String numeroRecu
) {
}
