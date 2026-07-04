package com.eofana.dto;

import java.time.OffsetDateTime;

/** Un élément de l'historique GET /api/v1/inscriptions/mes-inscriptions (T-B-011). */
public record MyEnrollmentResponse(
        Long idInscription,
        String titreFormation,
        String nomCentre,
        String typeInsc,
        String statut,
        long montantPaye,
        String numeroRecu,
        OffsetDateTime createdAt
) {
}
