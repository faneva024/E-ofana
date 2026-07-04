package com.eofana.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * Corps de la requête POST /api/v1/inscriptions/{id}/confirmer-paiement
 * (T-B-011). Peut être appelée deux fois pour une même inscription
 * en cas de réservation (acompte, puis solde).
 *
 * numeroTransaction est optionnel : en l'absence d'une véritable
 * intégration avec un agrégateur Mobile Money (hors périmètre de ce
 * sprint), un identifiant est généré côté serveur si le frontend n'en
 * fournit pas.
 */
public record ConfirmationPaymentRequest(

        @NotNull(message = "Le montant est obligatoire")
        @Positive(message = "Le montant doit être strictement positif")
        Long montant,

        @NotBlank(message = "L'opérateur Mobile Money est obligatoire")
        String operateur,

        String numeroTransaction
) {
}
