package com.eofana.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;

/**
 * Entité VisiteFormation — table "visiteFormation" (V13)
 * Compteur de consultations par formation pour les analytics formateur.
 * Tâche T-B-104 (FANEVA) — créée ici pour T-B-109 (ROLPH, Semaine 2)
 */
@Entity
@Table(name = "\"visiteFormation\"")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VisiteFormation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "\"idVisite\"")
    private Long idVisite;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "\"idFormation\"", nullable = false)
    private Formation formation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "\"idUtilisateur\"")
    private Utilisateur utilisateur;

    @Column(name = "\"adresseIp\"", length = 45)
    private String adresseIp;

    @Column(name = "\"dateVisite\"", nullable = false)
    @Builder.Default
    private OffsetDateTime dateVisite = OffsetDateTime.now();
}
