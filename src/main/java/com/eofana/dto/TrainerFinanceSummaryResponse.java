package com.eofana.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrainerFinanceSummaryResponse {
    private BigDecimal grossEarnings;       // Revenus bruts cumulés
    private BigDecimal commissionDeducted;  // Commissions prélevées par la plateforme
    private BigDecimal netEarnings;         // Revenus nets (bruts - commissions)
    private BigDecimal estimatedNextPayout; // Montant estimé du prochain virement
    private LocalDate nextPayoutDate;       // Date prévue du prochain virement
}