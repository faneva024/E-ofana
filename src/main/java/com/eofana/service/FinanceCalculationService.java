package com.eofana.service;

import com.eofana.entity.Centre;
import com.eofana.enums.FrequenceReversement;
import com.eofana.repository.InscriptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;

/**
 * Service de calcul des indicateurs financiers d'un centre de formation.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FinanceCalculationService {

    private final InscriptionRepository inscriptionRepository;

    /**
     * Calcule le revenu brut total du centre.
     */
    public Long calculateGrossRevenue(Centre centre) {
        return inscriptionRepository
                .sumMontantPayeValideParCentre(centre.getIdCentre());
    }

    /**
     * Calcule la commission totale collectée par E-OFANA sur ce centre.
     */
    public Long calculateCommissionCollected(Centre centre) {
        return inscriptionRepository
                .sumCommissionValideParCentre(centre.getIdCentre());
    }

    /**
     * Calcule le revenu net total dû au formateur.
     */
    public Long calculateNetRevenue(Centre centre) {
        return inscriptionRepository
                .sumMontantFormateurValideParCentre(centre.getIdCentre());
    }

    /**
     * Calcule la date et le montant estimé du prochain reversement.
     */
    public ProchainVirementInfo calculateNextTransfer(Centre centre) {
        Long montantEstime = inscriptionRepository
                .sumMontantNonVireParCentre(centre.getIdCentre());

        LocalDate prochaineDate = prochaineDateVirement(centre.getFrequenceReversement());

        return new ProchainVirementInfo(prochaineDate, montantEstime, "MGA");
    }

    /**
     * Construit un résumé financier complet en un seul appel (amélioration).
     */
    public FinanceSummary buildSummary(Centre centre) {
        Long grossRevenue          = calculateGrossRevenue(centre);
        Long commissionCollected   = calculateCommissionCollected(centre);
        Long netRevenue            = calculateNetRevenue(centre);
        ProchainVirementInfo next  = calculateNextTransfer(centre);

        return new FinanceSummary(
                grossRevenue,
                commissionCollected,
                netRevenue,
                next,
                centre.getTauxCommission(),
                centre.getAbonnement() != null ? centre.getAbonnement().name() : null,
                centre.getFrequenceReversement() != null
                        ? centre.getFrequenceReversement().name() : null
        );
    }


    /**
     * Calcule la date du prochain reversement selon la fréquence du centre.
     */
    private LocalDate prochaineDateVirement(FrequenceReversement frequence) {
        LocalDate aujourd_hui = LocalDate.now();

        if (frequence == null) {
            // Valeur par défaut sécurisée si le champ n'est pas configuré
            return premier_du_mois_prochain(aujourd_hui);
        }

        return switch (frequence) {
            case hebdomadaire -> {
                // Nombre de jours jusqu'au prochain lundi
                int joursJusquAuLundi = DayOfWeek.MONDAY.getValue()
                        - aujourd_hui.getDayOfWeek().getValue();
                // Si aujourd'hui est lundi (0) ou un jour déjà passé dans la semaine
                // → on attend le lundi SUIVANT (au minimum 1 jour, au maximum 7)
                if (joursJusquAuLundi <= 0) joursJusquAuLundi += 7;
                yield aujourd_hui.plusDays(joursJusquAuLundi);
            }
            case mensuel -> premier_du_mois_prochain(aujourd_hui);
        };
    }

    /** Retourne le 1er du mois suivant la date donnée. */
    private LocalDate premier_du_mois_prochain(LocalDate date) {
        return date.withDayOfMonth(1).plusMonths(1);
    }

    /**
     * Date et montant estimé du prochain virement vers le formateur.
     */
    public record ProchainVirementInfo(
            LocalDate date,
            Long      montantEstime,
            String    devise
    ) {}

    /**
     * Résumé financier complet d'un centre, retourné par {@link #buildSummary}.
     */
    public record FinanceSummary(
            Long                 grossRevenue,
            Long                 commissionCollected,
            Long                 netRevenue,
            ProchainVirementInfo prochainVirement,
            BigDecimal           tauxCommissionActuel,
            String               abonnement,
            String               frequenceReversement
    ) {}
}
