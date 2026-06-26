package com.eofana.entite;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "inscriptions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Inscription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idInscription")
    private Long idInscription;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idUser", nullable = false)
    private Utilisateur utilisateur;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idSession", nullable = false)
    private SessionFormation sessionFormation;

    @Enumerated(EnumType.STRING)
    @Column(name = "typeInsc")
    private TypeInscription typeInscription;

    @Enumerated(EnumType.STRING)
    @Column(name = "statut")
    private StatutInscription statut;

    @Column(name = "montantPaye")
    private Long montantPaye;

    @Column(name = "remise")
    private Long remise;

    @Column(name = "commission")
    private Long commission;

    @Column(name = "montantFormateur")
    private Long montantFormateur;

    @Enumerated(EnumType.STRING)
    @Column(name = "operateur")
    private OperateurMm operateur;

    @Column(name = "transactionId")
    private String transactionId;

    @Column(name = "numeroRecu")
    private String numeroRecu;

    @Column(name = "recuPdf")
    private String recuPdf;

    @Column(name = "createdAt", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updatedAt")
    private LocalDateTime updatedAt;
}