package com.eofana.entite;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Entité Paiement — table "paiements" de eofanaDb v3.0
 */
@Entity
@Table(name = "\"paiements\"")
public class Paiement {

    public enum StatutPaiement { enAttente, confirme, echoue }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "\"idPaiement\"")
    private Long idPaiement;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "\"idInscription\"", nullable = false)
    private Inscription inscription;

    @Column(name = "\"montant\"", nullable = false)
    private long montant;

    @Enumerated(EnumType.STRING)
    @Column(name = "\"operateur\"", nullable = false)
    private Inscription.Operateur operateur;

    @Column(name = "\"numeroTransaction\"", nullable = false, length = 100)
    private String numeroTransaction;

    @Enumerated(EnumType.STRING)
    @Column(name = "\"statut\"", nullable = false)
    private StatutPaiement statut = StatutPaiement.enAttente;

    @Column(name = "\"createdAt\"", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public Paiement() {}

    public Paiement(Inscription inscription, long montant,
                    Inscription.Operateur operateur, String numeroTransaction) {
        this.inscription       = inscription;
        this.montant           = montant;
        this.operateur         = operateur;
        this.numeroTransaction = numeroTransaction;
    }

    public Long getIdPaiement()                           { return idPaiement; }
    public Inscription getInscription()                   { return inscription; }
    public void setInscription(Inscription i)             { this.inscription = i; }
    public long getMontant()                              { return montant; }
    public void setMontant(long m)                        { this.montant = m; }
    public Inscription.Operateur getOperateur()           { return operateur; }
    public void setOperateur(Inscription.Operateur o)     { this.operateur = o; }
    public String getNumeroTransaction()                  { return numeroTransaction; }
    public void setNumeroTransaction(String n)            { this.numeroTransaction = n; }
    public StatutPaiement getStatut()                     { return statut; }
    public void setStatut(StatutPaiement s)               { this.statut = s; }
    public LocalDateTime getCreatedAt()                   { return createdAt; }
}
