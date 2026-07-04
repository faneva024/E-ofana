package com.eofana.dto;

/**
 * Simulation financière d'une inscription avant persistance (T-B-013).
 * Permet au frontend d'afficher le détail prix/remise/commission
 * avant que l'apprenant ne confirme son inscription.
 */
public record PricePreviewDto(
        long prixOriginal,
        long remise,
        long prixApresRemise,
        long commission,
        long montantFormateur
) {
}
