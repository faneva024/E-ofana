package com.eofana.entite;

import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * Entité Inscription — alignée sur la table "inscriptions" de eofanaDb v3.0
 * Couvre les inscriptions complètes et les réservations (acompte).
 * Les montants (remise, commission, montantFormateur) sont calculés
 * automatiquement par le trigger PostgreSQL fnCalculerCommission.
 */
@Entity
@Table(name = "\"inscriptions\"",
       uniqueConstraints = @UniqueConstraint(
           name = "uqUserSession",
           columnNames = {"\"idUser\"", "\"idSession\""}
       ))
@EntityListeners(AuditingEntityListener.class)
public class Inscription {

    public enum TypeInscription { inscription, reservation }

    public enum StatutInscription { enAttente, valide, annule, rembourse, termine }

    public enum Operateur { mvola, orange, airtel }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "\"idInscription\"")
    private Long idInscription;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "\"idUser\"", nullable = false)
    private Utilisateur utilisateur;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "\"idSession\"", nullable = false)
    private SessionFormation session;

    @Enumerated(EnumType.STRING)
    @Column(name = "\"typeInsc\"", nullable = false)
    private TypeInscription typeInsc = TypeInscription.inscription;

    @Enumerated(EnumType.STRING)
    @Column(name = "\"statut\"", nullable = false)
    private StatutInscription statut = StatutInscription.enAttente;

    /** Montant réellement encaissé en Ariary (après remise 5%). Calculé par trigger. */
    @Column(name = "\"montantPaye\"", nullable = false)
    private long montantPaye = 0;

    /** Remise 5% offerte par E-OFANA. Calculée par trigger. */
    @Column(name = "\"remise\"", nullable = false)
    private long remise = 0;

    /** Commission E-OFANA (7% basic ou 15% premium). Calculée par trigger. */
    @Column(name = "\"commission\"", nullable = false)
    private long commission = 0;

    /** Part reversée au formateur = montantPaye - commission. Calculée par trigger. */
    @Column(name = "\"montantFormateur\"", nullable = false)
    private long montantFormateur = 0;

    @Enumerated(EnumType.STRING)
    @Column(name = "\"operateur\"")
    private Operateur operateur;

    /** Référence de transaction retournée par l'API Mobile Money. */
    @Column(name = "\"transactionId\"", length = 100)
    private String transactionId;

    /** Numéro de reçu unique. Format : TXN-YYYYMMDD-NNNNN. Généré par trigger. */
    @Column(name = "\"numeroRecu\"", unique = true, length = 50)
    private String numeroRecu;

    @Column(name = "\"recuPdf\"")
    private String recuPdf;

    @CreatedDate
    @Column(name = "\"createdAt\"", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "\"updatedAt\"", nullable = false)
    private LocalDateTime updatedAt;

    // ── Constructeurs ────────────────────────────────────────────────────────
    public Inscription() {}

    public Inscription(Utilisateur utilisateur, SessionFormation session,
                       TypeInscription typeInsc, Operateur operateur) {
        this.utilisateur = utilisateur;
        this.session     = session;
        this.typeInsc    = typeInsc;
        this.operateur   = operateur;
    }

    // ── Méthodes utilitaires ─────────────────────────────────────────────────
    public boolean estValide()      { return StatutInscription.valide.equals(this.statut); }
    public boolean estReservation() { return TypeInscription.reservation.equals(this.typeInsc); }

    // ── Getters / Setters ────────────────────────────────────────────────────
    public Long getIdInscription()                          { return idInscription; }
    public Utilisateur getUtilisateur()                     { return utilisateur; }
    public void setUtilisateur(Utilisateur u)               { this.utilisateur = u; }
    public SessionFormation getSession()                    { return session; }
    public void setSession(SessionFormation s)              { this.session = s; }
    public TypeInscription getTypeInsc()                    { return typeInsc; }
    public void setTypeInsc(TypeInscription t)              { this.typeInsc = t; }
    public StatutInscription getStatut()                    { return statut; }
    public void setStatut(StatutInscription statut)         { this.statut = statut; }
    public long getMontantPaye()                            { return montantPaye; }
    public void setMontantPaye(long montantPaye)            { this.montantPaye = montantPaye; }
    public long getRemise()                                 { return remise; }
    public void setRemise(long remise)                      { this.remise = remise; }
    public long getCommission()                             { return commission; }
    public void setCommission(long commission)              { this.commission = commission; }
    public long getMontantFormateur()                       { return montantFormateur; }
    public void setMontantFormateur(long montantFormateur)  { this.montantFormateur = montantFormateur; }
    public Operateur getOperateur()                         { return operateur; }
    public void setOperateur(Operateur operateur)           { this.operateur = operateur; }
    public String getTransactionId()                        { return transactionId; }
    public void setTransactionId(String transactionId)      { this.transactionId = transactionId; }
    public String getNumeroRecu()                           { return numeroRecu; }
    public void setNumeroRecu(String numeroRecu)            { this.numeroRecu = numeroRecu; }
    public String getRecuPdf()                              { return recuPdf; }
    public void setRecuPdf(String recuPdf)                  { this.recuPdf = recuPdf; }
    public LocalDateTime getCreatedAt()                     { return createdAt; }
    public LocalDateTime getUpdatedAt()                     { return updatedAt; }
}
