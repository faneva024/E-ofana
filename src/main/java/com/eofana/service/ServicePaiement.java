package com.eofana.service;

import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class ServicePaiement {

    private static final List<String> OPERATEURS_AUTORISES = List.of("Mvola", "OrangeMoney", "AirtelMoney");
    private static final AtomicLong compteur = new AtomicLong(1);

    /**
     * Calcul prix inscription avec remise 5%
     */
    public Map<String, BigDecimal> calculerPrixInscription(BigDecimal prixFormation) {
        Map<String, BigDecimal> resultat = new HashMap<>();
        BigDecimal remise = prixFormation.multiply(new BigDecimal("0.05")).setScale(2, RoundingMode.HALF_UP);
        BigDecimal apprenantPaie = prixFormation.subtract(remise).setScale(2, RoundingMode.HALF_UP);
        BigDecimal commissionEofana = prixFormation.multiply(new BigDecimal("0.07")).setScale(2, RoundingMode.HALF_UP);
        BigDecimal formateurRecoit = apprenantPaie.subtract(commissionEofana).setScale(2, RoundingMode.HALF_UP);

        resultat.put("prixAffiche", prixFormation);
        resultat.put("remise", remise);
        resultat.put("apprenantPaie", apprenantPaie);
        resultat.put("commissionEofana", commissionEofana);
        resultat.put("formateurRecoit", formateurRecoit);
        return resultat;
    }

    /**
     * Calcul prix réservation (commission uniquement)
     * Abonnement standard : 15%, premium : 7%
     */
    public BigDecimal calculerPrixReservation(BigDecimal prixFormation, boolean formateurPremium) {
        BigDecimal tauxCommission = formateurPremium ? new BigDecimal("0.07") : new BigDecimal("0.15");
        return prixFormation.multiply(tauxCommission).setScale(2, RoundingMode.HALF_UP);
    }

    public boolean validerOperateurPaiement(String operateur) {
        return OPERATEURS_AUTORISES.contains(operateur);
    }

    public String genererNumeroRecu() {
        String annee = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy"));
        long numero = compteur.getAndIncrement();
        return String.format("REC-%s-%05d", annee, numero);
    }

    public String genererIdTransaction() {
        return "TRX-" + System.currentTimeMillis() + "-" + (int)(Math.random() * 10000);
    }
}
