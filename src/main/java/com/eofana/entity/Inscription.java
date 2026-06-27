package com.eofana.entity;

import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import java.time.LocalDateTime;

@Entity
@Table(name = "inscriptions")
@EntityListeners(AuditingEntityListener.class)
public class Inscription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_inscription")
    private Long idInscription;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_utilisateur", nullable = false)
    private Utilisateur utilisateur;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_formation", nullable = false)
    private Formation formation;

    @Column(name = "statut_paiement", nullable = false)
    private String statutPaiement = "enAttente";

    @Column(name = "reservation_uniquement", nullable = false)
    private Boolean reservationUniquement = false;

    @Column(name = "date_inscription", nullable = false)
    private LocalDateTime dateInscription = LocalDateTime.now();

    @Column(name = "numero_recu")
    private String numeroRecu;

    @Column(name = "operateur_paiement")
    private String operateurPaiement;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public Inscription() {}

    public Long getIdInscription() { return idInscription; }
    public void setIdInscription(Long idInscription) { this.idInscription = idInscription; }
    public Utilisateur getUtilisateur() { return utilisateur; }
    public void setUtilisateur(Utilisateur utilisateur) { this.utilisateur = utilisateur; }
    public Formation getFormation() { return formation; }
    public void setFormation(Formation formation) { this.formation = formation; }
    public String getStatutPaiement() { return statutPaiement; }
    public void setStatutPaiement(String statutPaiement) { this.statutPaiement = statutPaiement; }
    public Boolean getReservationUniquement() { return reservationUniquement; }
    public void setReservationUniquement(Boolean reservationUniquement) { this.reservationUniquement = reservationUniquement; }
    public LocalDateTime getDateInscription() { return dateInscription; }
    public void setDateInscription(LocalDateTime dateInscription) { this.dateInscription = dateInscription; }
    public String getNumeroRecu() { return numeroRecu; }
    public void setNumeroRecu(String numeroRecu) { this.numeroRecu = numeroRecu; }
    public String getOperateurPaiement() { return operateurPaiement; }
    public void setOperateurPaiement(String operateurPaiement) { this.operateurPaiement = operateurPaiement; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
