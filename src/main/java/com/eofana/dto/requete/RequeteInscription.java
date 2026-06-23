package com.eofana.dto.requete;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public class RequeteInscription {

    @NotNull(message = "L'identifiant de la formation est obligatoire.")
    private Long formationId;

    @NotNull(message = "L'opérateur Mobile Money est obligatoire.")
    @Pattern(
        regexp = "mvola|orange_money|airtel_money",
        message = "Opérateur invalide. Valeurs acceptées : mvola, orange_money, airtel_money."
    )
    private String operateurPaiement;

    public Long getFormationId()              { return formationId; }
    public void setFormationId(Long v)        { this.formationId = v; }
    public String getOperateurPaiement()      { return operateurPaiement; }
    public void setOperateurPaiement(String v){ this.operateurPaiement = v; }
}
