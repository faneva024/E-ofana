package com.eofana.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "formations")
@EntityListeners(AuditingEntityListener.class)
public class Formation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_formation")
    private Long idFormation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_formateur", nullable = false)
    private Utilisateur formateur;

    @NotBlank
    @Column(name = "titre", nullable = false)
    private String titre;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "categorie")
    private String categorie;

    @Column(name = "lieu")
    private String lieu;

    @Column(name = "date_debut")
    private LocalDateTime dateDebut;

    @Column(name = "date_limite_inscription")
    private LocalDateTime dateLimiteInscription;

    @NotNull
    @Column(name = "places_disponibles", nullable = false)
    private Integer placesDisponibles = 0;

    @NotNull
    @Column(name = "prix", nullable = false, precision = 10, scale = 2)
    private BigDecimal prix = BigDecimal.ZERO;

    @Column(name = "prix_remise", precision = 10, scale = 2)
    private BigDecimal prixRemise;

    @Column(name = "chemin_image")
    private String cheminImage;

    @Column(name = "statut", nullable = false)
    private String statut = "enAttente";

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public Formation() {}

    public Long getIdFormation() { return idFormation; }
    public void setIdFormation(Long idFormation) { this.idFormation = idFormation; }
    public Utilisateur getFormateur() { return formateur; }
    public void setFormateur(Utilisateur formateur) { this.formateur = formateur; }
    public String getTitre() { return titre; }
    public void setTitre(String titre) { this.titre = titre; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getCategorie() { return categorie; }
    public void setCategorie(String categorie) { this.categorie = categorie; }
    public String getLieu() { return lieu; }
    public void setLieu(String lieu) { this.lieu = lieu; }
    public LocalDateTime getDateDebut() { return dateDebut; }
    public void setDateDebut(LocalDateTime dateDebut) { this.dateDebut = dateDebut; }
    public LocalDateTime getDateLimiteInscription() { return dateLimiteInscription; }
    public void setDateLimiteInscription(LocalDateTime dateLimiteInscription) { this.dateLimiteInscription = dateLimiteInscription; }
    public Integer getPlacesDisponibles() { return placesDisponibles; }
    public void setPlacesDisponibles(Integer placesDisponibles) { this.placesDisponibles = placesDisponibles; }
    public BigDecimal getPrix() { return prix; }
    public void setPrix(BigDecimal prix) { this.prix = prix; }
    public BigDecimal getPrixRemise() { return prixRemise; }
    public void setPrixRemise(BigDecimal prixRemise) { this.prixRemise = prixRemise; }
    public String getCheminImage() { return cheminImage; }
    public void setCheminImage(String cheminImage) { this.cheminImage = cheminImage; }
    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public int calculerPlacesRestantes() {
        return this.placesDisponibles != null ? this.placesDisponibles : 0;
    }
}
