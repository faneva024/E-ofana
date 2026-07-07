package com.eofana.service;

import com.eofana.entity.Centre;
import com.eofana.enums.FrequenceReversement;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;

/**
 * T-B-112 : calculs financiers pour un centre de formation.
 *
 * Réutilise la même logique de commission que le module apprenant
 * (inscriptions.montantFormateur, déjà calculé au moment du paiement,
 * cf. PaymentService / trigger applicatif du module apprenant) plutôt
 * que de recalculer un taux séparé ici — centre.tauxCommission reste
 * la source de vérité, synchronisée par le trigger fnSyncTauxCommission.
 */
@Service
@RequiredArgsConstructor
public class FinanceCalculationService {

    private final JdbcTemplate jdbcTemplate;

    /** Total des inscriptions VALIDE avant déduction de la commission. */
    public long calculateGrossRevenue(Centre centre) {
        Long total = jdbcTemplate.queryForObject("""
                SELECT COALESCE(SUM(i."montantPaye"), 0)
                FROM eofana.inscriptions i
                JOIN eofana."sessionsFormation" s ON s."idSession" = i."idSession"
                JOIN eofana.formations f ON f."idFormation" = s."idFormation"
                WHERE f."idCentre" = ? AND i.statut = 'valide'
                """, Long.class, centre.getIdCentre());
        return total != null ? total : 0L;
    }

    /**
     * Montant retenu par E-OFANA, à partir de centre.tauxCommission
     * (7% Basic / 15% Premium, synchronisé par fnSyncTauxCommission —
     * pas de switch séparé sur abonnement ici).
     */
    public long calculateCommissionCollected(Centre centre) {
        long brut = calculateGrossRevenue(centre);
        return java.math.BigDecimal.valueOf(brut)
                .multiply(centre.getTauxCommission())
                .divide(java.math.BigDecimal.valueOf(100))
                .longValue();
    }

    /** Ce que le formateur reçoit réellement. */
    public long calculateNetRevenue(Centre centre) {
        return calculateGrossRevenue(centre) - calculateCommissionCollected(centre);
    }

    /**
     * Date et montant estimé du prochain virement, selon
     * centre.frequenceReversement (hebdomadaire = chaque lundi,
     * mensuel = le 1er du mois), calculé sur les inscriptions VALIDE
     * non encore incluses dans un virement EFFECTUE.
     */
    public ProchainVirement calculateNextTransfer(Centre centre) {
        LocalDate prochaineDate = centre.getFrequenceReversement() == FrequenceReversement.hebdomadaire
                ? LocalDate.now().with(TemporalAdjusters.next(DayOfWeek.MONDAY))
                : LocalDate.now().with(TemporalAdjusters.firstDayOfNextMonth());

        Long montantNonVire = jdbcTemplate.queryForObject("""
                SELECT COALESCE(SUM(i."montantFormateur"), 0)
                FROM eofana.inscriptions i
                JOIN eofana."sessionsFormation" s ON s."idSession" = i."idSession"
                JOIN eofana.formations f ON f."idFormation" = s."idFormation"
                WHERE f."idCentre" = ?
                  AND i.statut = 'valide'
                  AND NOT EXISTS (
                      SELECT 1 FROM eofana.virements v
                      WHERE v."idCentre" = f."idCentre"
                        AND v.statut = 'effectue'
                        AND i."createdAt"::date BETWEEN v."periodeDebut" AND v."periodeFin"
                  )
                """, Long.class, centre.getIdCentre());

        return new ProchainVirement(prochaineDate, montantNonVire != null ? montantNonVire : 0L);
    }

    public record ProchainVirement(LocalDate date, long montantEstime) {
    }
}
