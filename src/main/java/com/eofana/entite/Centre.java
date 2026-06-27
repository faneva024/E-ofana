package com.eofana.entite;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entité Centre — table "centres" de eofanaDb v3.0
 */
@Entity
@Table(name = "\"centres\"")
public class Centre {

    public enum Abonnement { basic, premium }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "\"idCentre\"")
    private Long idCentre;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "\"idUser\"", nullable = false, unique = true)
    private Utilisateur utilisateur;

    @Column(name = "\"nom\"", nullable = false, length = 200)
    private String nom;

    @Enumerated(EnumType.STRING)
    @Column(name = "\"abonnement\"", nullable = false)
    private Abonnement abonnement = Abonnement.basic;

    @Column(name = "\"tauxCommission\"", nullable = false, precision = 5, scale = 2)
    private BigDecimal tauxCommission = new BigDecimal("7.00");

    @Column(name = "\"createdAt\"", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "\"updatedAt\"", nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    // Getters
    public Long getIdCentre()                   { return idCentre; }
    public Utilisateur getUtilisateur()         { return utilisateur; }
    public void setUtilisateur(Utilisateur u)   { this.utilisateur = u; }
    public String getNom()                      { return nom; }
    public void setNom(String n)                { this.nom = n; }
    public Abonnement getAbonnement()           { return abonnement; }
    public void setAbonnement(Abonnement a)     { this.abonnement = a; }
    public BigDecimal getTauxCommission()       { return tauxCommission; }
    public void setTauxCommission(BigDecimal t) { this.tauxCommission = t; }
    public LocalDateTime getCreatedAt()         { return createdAt; }
    public LocalDateTime getUpdatedAt()         { return updatedAt; }
}
