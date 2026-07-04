package com.eofana.entity;

import com.eofana.enums.StatutFormation;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

/**
 * Entité JPA mappée sur la table "formations" (schéma eofana — migration V4).
 */
@Entity
@Table(name = "\"formations\"")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Formation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "\"idFormation\"")
    private Long idFormation;


    @NotNull(message = "La formation doit être rattachée à un centre")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "\"idCentre\"", nullable = false)
    private Centre centre;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "\"idCategorie\"")
    private Categorie categorie;

    // ─── Informations de la formation ──────────────────────────────────────────
    @NotBlank(message = "Le titre est obligatoire")
    @Size(max = 250, message = "Le titre ne doit pas dépasser 250 caractères")
    @Column(name = "\"titre\"", nullable = false, length = 250)
    private String titre;

    @Column(name = "\"description\"", columnDefinition = "TEXT")
    private String description;

    /** Chemin ou URL de l'image de couverture. */
    @Size(max = 255)
    @Column(name = "\"image\"", length = 255)
    private String image;

    /**
     * Durée indicative de la formation (ex. "3 jours", "20 heures").
     */
    @Size(max = 100)
    @Column(name = "\"duree\"", length = 100)
    private String duree;

    /** Lieu de déroulement (ex. "Antananarivo – Analakely"). */
    @Size(max = 200)
    @Column(name = "\"lieu\"", length = 200)
    private String lieu;


    /**
     * Prix affiché de la formation, en Ariary.
     */
    @NotNull(message = "Le prix est obligatoire")
    @PositiveOrZero(message = "Le prix ne peut pas être négatif")
    @Column(name = "\"prix\"", nullable = false)
    private Long prix;

    /**
     * Prix remisé proposé par le centre (avant la remise E-OFANA de 5%).
     */
    @Column(name = "\"prixRemise\"")
    private Long prixRemise;

    /**
     * Statut courant dans le workflow de modération.
     */
    @NotNull(message = "Le statut est obligatoire")
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "\"statut\"", nullable = false, columnDefinition = "\"statutFormation\"")
    @Builder.Default
    private StatutFormation statut = StatutFormation.brouillon;

    /**
     * Motif de rejet ou de demande de correction, rédigé par le modérateur.
     */
    @Column(name = "\"motifRejet\"", columnDefinition = "TEXT")
    private String motifRejet;

    /**
     * Modérateur (admin) qui a pris la dernière décision sur cette formation.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "\"idModerateur\"")
    private Utilisateur moderateur;

    /**
     * Date/heure de la dernière décision de modération.
     */
    @Column(name = "\"validatedAt\"")
    private OffsetDateTime validatedAt;

    @Column(name = "\"noteMoyenne\"", nullable = false, precision = 3, scale = 2)
    @Builder.Default
    private BigDecimal noteMoyenne = BigDecimal.ZERO;

    @Column(name = "\"nbAvis\"", nullable = false)
    @Builder.Default
    private Integer nbAvis = 0;


    @Column(name = "\"createdAt\"", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "\"updatedAt\"", nullable = false)
    private OffsetDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        OffsetDateTime now = OffsetDateTime.now();
        if (this.createdAt == null) this.createdAt = now;
        if (this.updatedAt == null) this.updatedAt = now;
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = OffsetDateTime.now();
    }
}
