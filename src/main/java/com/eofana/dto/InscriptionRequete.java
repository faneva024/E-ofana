package com.eofana.dto;

import jakarta.validation.constraints.NotNull;

public class InscriptionRequete {
    @NotNull private Long idFormation;
    @NotNull private String operateurPaiement;

    public Long getIdFormation() { return idFormation; }
    public void setIdFormation(Long idFormation) { this.idFormation = idFormation; }
    public String getOperateurPaiement() { return operateurPaiement; }
    public void setOperateurPaiement(String operateurPaiement) { this.operateurPaiement = operateurPaiement; }
}
