package com.eofana.service;

import com.eofana.dto.PricePreviewDto;
import com.eofana.enums.OperateurMm;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Service métier portant tous les calculs financiers liés à une
 * inscription (T-B-013).
 *
 * Remarque : contrairement au script SQL complet fourni à titre de
 * référence, les migrations Flyway réellement déployées (V1 à V7)
 * ne contiennent aucun trigger PostgreSQL de calcul automatique —
 * remise, commission et part du formateur sont donc calculées ici,
 * en Java, avant persistance de l'Inscription. C'est ce service qui
 * fait foi.
 */
@Service
public class PaymentService {

    /** Taux de remise fixe appliqué par E-OFANA sur le prix normal d'une formation. */
    private static final BigDecimal TAUX_REMISE_EOFANA = new BigDecimal("0.05");

    /**
     * Applique la remise fixe de 5 % E-OFANA sur le prix normal
     * de la formation.
     *
     * @param originalPrice prix normal (Formation.prix), en Ariary
     * @return prix arrondi après remise
     */
    public long calculateDiscountedPrice(long originalPrice) {
        return BigDecimal.valueOf(originalPrice)
                .multiply(BigDecimal.ONE.subtract(TAUX_REMISE_EOFANA))
                .setScale(0, RoundingMode.HALF_UP)
                .longValueExact();
    }

    /**
     * Calcule la commission prélevée par la plateforme E-OFANA sur un
     * montant payé, selon le taux de commission du centre concerné
     * (Centre.tauxCommission : 7.00 pour un abonnement basic, 15.00
     * pour un abonnement premium).
     *
     * @param montantPaye    montant sur lequel s'applique la commission
     * @param tauxCommission taux en pourcentage (ex : 7.00 ou 15.00)
     */
    public long calculateCommissionFee(long montantPaye, BigDecimal tauxCommission) {
        if (tauxCommission == null) {
            throw new IllegalArgumentException("Le taux de commission est obligatoire");
        }

        return BigDecimal.valueOf(montantPaye)
                .multiply(tauxCommission)
                .divide(BigDecimal.valueOf(100), 0, RoundingMode.HALF_UP)
                .longValueExact();
    }

    /**
     * Calcule la part nette finale revenant au formateur une fois la
     * commission déduite du montant payé.
     */
    public long calculateTrainerShare(long montantPaye, long commission) {
        return montantPaye - commission;
    }

    /**
     * Vérifie que l'opérateur Mobile Money fourni par le client fait
     * bien partie des opérateurs autorisés à Madagascar (mvola,
     * orange, airtel — cf. enum OperateurMm).
     */
    public boolean isValidOperator(String operator) {
        if (operator == null || operator.isBlank()) {
            return false;
        }

        try {
            OperateurMm.valueOf(operator.trim().toLowerCase());
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * Construit la simulation financière complète d'une inscription
     * complète (typeInsc = inscription) : remise, prix après remise,
     * commission, part du formateur.
     */
    public PricePreviewDto previewInscription(long prixOriginal, BigDecimal tauxCommission) {
        long prixApresRemise = calculateDiscountedPrice(prixOriginal);
        long remise = prixOriginal - prixApresRemise;
        long commission = calculateCommissionFee(prixApresRemise, tauxCommission);
        long montantFormateur = calculateTrainerShare(prixApresRemise, commission);

        return new PricePreviewDto(prixOriginal, remise, prixApresRemise, commission, montantFormateur);
    }

    /**
     * Construit la simulation financière d'une réservation : le total
     * dû à terme est identique à une inscription complète (remise
     * incluse), mais l'acompte à encaisser immédiatement correspond
     * uniquement à la commission (cf. cahier des charges §4.6).
     */
    public long calculateReservationDeposit(long prixOriginal, BigDecimal tauxCommission) {
        long prixApresRemise = calculateDiscountedPrice(prixOriginal);
        return calculateCommissionFee(prixApresRemise, tauxCommission);
    }
}
