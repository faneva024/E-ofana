package com.eofana.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public class ConfirmationPaiementRequete {
    @NotBlank private String idTransaction;
    @NotNull private BigDecimal montant;
    private String horodatage;

    public String getIdTransaction() { return idTransaction; }
    public void setIdTransaction(String idTransaction) { this.idTransaction = idTransaction; }
    public BigDecimal getMontant() { return montant; }
    public void setMontant(BigDecimal montant) { this.montant = montant; }
    public String getHorodatage() { return horodatage; }
    public void setHorodatage(String horodatage) { this.horodatage = horodatage; }
}
