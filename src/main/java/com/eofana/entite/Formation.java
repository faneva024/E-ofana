package com.eofana.entite;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entité Formation — table "formations" de eofanaDb v3.0
 */
@Entity
@Table(name = "\"formations\"")
public class Formation {

    public enum StatutFormation {
        brouillon, enAttente, approuve, rejete, correctionDemandee, archive
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "\"idFormation\"")
    private Long idFormation;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "\"idCentre\"", nullable = false)
    private Centre centre;

    @Column(name = "\"titre\"", nullable = false, length = 250)
    private String titre;

    @Column(name = "\"description\"", columnDefinition = "TEXT")
    private String description;

    @Column(name = "\"duree\"", length = 100)
    private String duree;

    @Column(name = "\"lieu\"", length = 200)
    private String lieu;

    @Column(name = "\"prix\"", nullable = false)
    private long prix;

    @Column(name = "\"prixRemise\"")
    private Long prixRemise;

    @Enumerated(EnumType.STRING)
    @Column(name = "\"statut\"", nullable = false)
    private StatutFormation statut = StatutFormation.brouillon;

    @Column(name = "\"noteMoyenne\"", precision = 3, scale = 2)
    private BigDecimal noteMoyenne = BigDecimal.ZERO;

    @Column(name = "\"nbAvis\"", nullable = false)
    private int nbAvis = 0;

    @Column(name = "\"createdAt\"", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "\"updatedAt\"", nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    // Getters
    public Long getIdFormation()          { return idFormation; }
    public Centre getCentre()             { return centre; }
    public void setCentre(Centre c)       { this.centre = c; }
    public String getTitre()              { return titre; }
    public void setTitre(String t)        { this.titre = t; }
    public String getDescription()        { return description; }
    public void setDescription(String d)  { this.description = d; }
    public String getDuree()              { return duree; }
    public void setDuree(String d)        { this.duree = d; }
    public String getLieu()               { return lieu; }
    public void setLieu(String l)         { this.lieu = l; }
    public long getPrix()                 { return prix; }
    public void setPrix(long p)           { this.prix = p; }
    public Long getPrixRemise()           { return prixRemise; }
    public void setPrixRemise(Long p)     { this.prixRemise = p; }
    public StatutFormation getStatut()    { return statut; }
    public void setStatut(StatutFormation s) { this.statut = s; }
    public BigDecimal getNoteMoyenne()    { return noteMoyenne; }
    public int getNbAvis()                { return nbAvis; }
    public LocalDateTime getCreatedAt()   { return createdAt; }
    public LocalDateTime getUpdatedAt()   { return updatedAt; }
}
