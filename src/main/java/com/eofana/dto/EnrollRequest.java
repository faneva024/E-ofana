package com.eofana.dto;

import jakarta.validation.constraints.NotNull;

/**
 * Corps des requêtes POST /api/v1/inscriptions/inscrire et
 * /api/v1/inscriptions/reserver (T-B-011).
 *
 * idSession, PAS idFormation : l'apprenant choisit une session
 * précise d'une formation (une même formation peut avoir plusieurs
 * sessions à des dates différentes). Le frontend récupère l'idSession
 * via GET /api/v1/formations/{id}/sessions avant d'appeler cette route.
 *
 * Ne contient volontairement pas d'informations de paiement :
 * l'inscription est créée en statut "enAttente" d'abord, le paiement
 * Mobile Money est confirmé dans une étape séparée
 * (POST /api/v1/inscriptions/{id}/confirmer-paiement), pour rester
 * cohérent avec le flux en deux temps déjà utilisé par le frontend.
 */
public record EnrollRequest(

        @NotNull(message = "L'identifiant de la session est obligatoire")
        Long idSession
) {
}
