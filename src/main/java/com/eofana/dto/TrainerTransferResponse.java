package com.eofana.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrainerTransferResponse {
    private Long transferId;
    private LocalDateTime transferDate;
    private BigDecimal amount;
    private String mobileMoneyOperator;    // Mvola, Orange Money, Airtel Money
    private String status;                 // EN_COURS, SUCCES, ECHEC
    private String receiptPdfUrl;          // Lien vers le justificatif PDF du virement
}