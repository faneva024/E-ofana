package com.eofana.entite;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Entité SessionFormation — table "sessionsFormation" de eofanaDb v3.0
 */
@Entity
@Table(name = "\"sessionsFormation\"")
public class SessionFormation {

    public enum StatutSession { ouvert, complet, termine, annule }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "\"idSession\"")
    private Long idSession;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "\"idFormation\"", nullable = false)
    private Formation formation;

    @Column(name = "\"dateDebut\"", nullable = false)
    private LocalDate dateDebut;

    @Column(name = "\"dateFin\"")
    private LocalDate dateFin;

    @Column(name = "\"dateLimiteInscription\"", nullable = false)
    private LocalDate dateLimiteInscription;

    @Column(name = "\"placesTotal\"", nullable = false)
    private int placesTotal = 20;

    @Column(name = "\"placesRestantes\"", nullable = false)
    private int placesRestantes = 20;

    @Enumerated(EnumType.STRING)
    @Column(name = "\"statut\"", nullable = false)
    private StatutSession statut = StatutSession.ouvert;

    @Column(name = "\"createdAt\"", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    // ── Utilitaires ──────────────────────────────────────────────────────────
    public boolean estDisponible() {
        return StatutSession.ouvert.equals(this.statut)
            && this.placesRestantes > 0
            && !LocalDate.now().isAfter(this.dateLimiteInscription);
    }

    // ── Getters / Setters ────────────────────────────────────────────────────
    public Long getIdSession()                                   { return idSession; }
    public Formation getFormation()                              { return formation; }
    public void setFormation(Formation f)                        { this.formation = f; }
    public LocalDate getDateDebut()                              { return dateDebut; }
    public void setDateDebut(LocalDate d)                        { this.dateDebut = d; }
    public LocalDate getDateFin()                                { return dateFin; }
    public void setDateFin(LocalDate d)                          { this.dateFin = d; }
    public LocalDate getDateLimiteInscription()                  { return dateLimiteInscription; }
    public void setDateLimiteInscription(LocalDate d)            { this.dateLimiteInscription = d; }
    public int getPlacesTotal()                                  { return placesTotal; }
    public void setPlacesTotal(int p)                            { this.placesTotal = p; }
    public int getPlacesRestantes()                              { return placesRestantes; }
    public void setPlacesRestantes(int p)                        { this.placesRestantes = p; }
    public StatutSession getStatut()                             { return statut; }
    public void setStatut(StatutSession s)                       { this.statut = s; }
    public LocalDateTime getCreatedAt()                          { return createdAt; }
}
