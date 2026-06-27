package com.eofana.dto.reponse;

/**
 * DTO — réponse JSON renvoyée après création d'une inscription ou réservation.
 * Tous les montants sont en Ariary Malagasy (entiers).
 */
public class ReponseInscription {

    private Long    idInscription;
    private String  typeInscription;   // "inscription" ou "reservation"
    private String  statut;
    private String  numeroRecu;
    private long    montantPaye;
    private long    remise;
    private long    commission;
    private long    montantFormateur;
    private String  operateur;
    private String  message;

    // Constructeur complet
    public ReponseInscription(Long idInscription, String typeInscription,
                               String statut, String numeroRecu,
                               long montantPaye, long remise,
                               long commission, long montantFormateur,
                               String operateur, String message) {
        this.idInscription   = idInscription;
        this.typeInscription = typeInscription;
        this.statut          = statut;
        this.numeroRecu      = numeroRecu;
        this.montantPaye     = montantPaye;
        this.remise          = remise;
        this.commission      = commission;
        this.montantFormateur= montantFormateur;
        this.operateur       = operateur;
        this.message         = message;
    }

    public Long    getIdInscription()    { return idInscription; }
    public String  getTypeInscription()  { return typeInscription; }
    public String  getStatut()           { return statut; }
    public String  getNumeroRecu()       { return numeroRecu; }
    public long    getMontantPaye()      { return montantPaye; }
    public long    getRemise()           { return remise; }
    public long    getCommission()       { return commission; }
    public long    getMontantFormateur() { return montantFormateur; }
    public String  getOperateur()        { return operateur; }
    public String  getMessage()          { return message; }
}
