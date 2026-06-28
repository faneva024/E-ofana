package com.eofana.entity;

import com.eofana.enums.StatutFormation;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

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

    @NotBlank(message = "Le titre est obligatoire")
    @Size(max = 250, message = "Le titre ne doit pas dépasser 250 caractères")
    @Column(name = "\"titre\"", nullable = false, length = 250)
    private String titre;

    @Column(name = "\"description\"", columnDefinition = "TEXT")
    private String description;

    @Size(max = 255)
    @Column(name = "\"image\"", length = 255)
    private String image;

    @Size(max = 100)
    @Column(name = "\"duree\"", length = 100)
    private String duree;

    @Size(max = 200)
    @Column(name = "\"lieu\"", length = 200)
    private String lieu;

    @NotNull(message = "Le prix est obligatoire")
    @PositiveOrZero(message = "Le prix ne peut pas être négatif")
    @Column(name = "\"prix\"", nullable = false)
    private Long prix;

    @Column(name = "\"prixRemise\"")
    private Long prixRemise;

    @NotNull(message = "Le statut est obligatoire")
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "\"statut\"", nullable = false, columnDefinition = "\"statutFormation\"")
    @Builder.Default
    private StatutFormation statut = StatutFormation.brouillon;

    @Column(name = "\"motifRejet\"", columnDefinition = "TEXT")
    private String motifRejet;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "\"idModerateur\"")
    private Utilisateur moderateur;

    @Column(name = "\"validatedAt\"")
    private LocalDateTime validatedAt;

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
        if (this.createdAt == null) {
            this.createdAt = OffsetDateTime.now();
        }

        if (this.updatedAt == null) {
            this.updatedAt = OffsetDateTime.now();
        }
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = OffsetDateTime.now();
    }
}