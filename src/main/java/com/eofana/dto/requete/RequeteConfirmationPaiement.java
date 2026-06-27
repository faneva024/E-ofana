package com.eofana.dto.requete;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * DTO — corps de la requête POST /api/v1/inscriptions/{id}/confirmer-paiement
 * Référence BDD v3.0 : table "paiements", colonne "numeroTransaction".
 */
public class RequeteConfirmationPaiement {

    @NotBlank(message = "Le numéro de transaction est obligatoire.")
    private String numeroTransaction;

    @NotNull(message = "Le montant est obligatoire.")
    @Positive(message = "Le montant doit être positif.")
    private Long montant;

    /** Horodatage retourné par l'opérateur Mobile Money (optionnel). */
    private String horodatage;

    public String getNumeroTransaction()          { return numeroTransaction; }
    public void setNumeroTransaction(String v)    { this.numeroTransaction = v; }
    public Long getMontant()                      { return montant; }
    public void setMontant(Long v)                { this.montant = v; }
    public String getHorodatage()                 { return horodatage; }
    public void setHorodatage(String v)           { this.horodatage = v; }
}
