package com.eofana.dto;

import java.math.BigDecimal;

public class InscriptionReponse {
    private Long idInscription;
    private BigDecimal montantAPayer;
    private String idTransactionProvisoire;
    private String numeroRecu;
    private String message;

    public InscriptionReponse() {}

    public Long getIdInscription() { return idInscription; }
    public void setIdInscription(Long idInscription) { this.idInscription = idInscription; }
    public BigDecimal getMontantAPayer() { return montantAPayer; }
    public void setMontantAPayer(BigDecimal montantAPayer) { this.montantAPayer = montantAPayer; }
    public String getIdTransactionProvisoire() { return idTransactionProvisoire; }
    public void setIdTransactionProvisoire(String idTransactionProvisoire) { this.idTransactionProvisoire = idTransactionProvisoire; }
    public String getNumeroRecu() { return numeroRecu; }
    public void setNumeroRecu(String numeroRecu) { this.numeroRecu = numeroRecu; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
