package com.eofana.dto.requete;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

/**
 * DTO — corps de la requête POST /api/v1/inscriptions/inscrire
 * Référence BDD v3.0 : colonne "operateur" de type "operateurMm"
 */
public class RequeteInscription {

    @NotNull(message = "L'identifiant de la session est obligatoire.")
    private Long idSession;

    @NotNull(message = "L'opérateur Mobile Money est obligatoire.")
    @Pattern(
        regexp  = "mvola|orange|airtel",
        message = "Opérateur invalide. Valeurs acceptées : mvola, orange, airtel."
    )
    private String operateur;

    public Long getIdSession()            { return idSession; }
    public void setIdSession(Long v)      { this.idSession = v; }
    public String getOperateur()          { return operateur; }
    public void setOperateur(String v)    { this.operateur = v; }
}
