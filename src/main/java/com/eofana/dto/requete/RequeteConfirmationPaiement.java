package com.eofana.dto.requete;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class RequeteConfirmationPaiement {

    @NotBlank(message = "L'identifiant de transaction est obligatoire.")
    private String idTransaction;

    @NotNull(message = "Le montant est obligatoire.")
    @Positive(message = "Le montant doit être positif.")
    private Long montant;

    private String horodatage;

    public String getIdTransaction()       { return idTransaction; }
    public void setIdTransaction(String v) { this.idTransaction = v; }
    public Long getMontant()               { return montant; }
    public void setMontant(Long v)         { this.montant = v; }
    public String getHorodatage()          { return horodatage; }
    public void setHorodatage(String v)    { this.horodatage = v; }
}
