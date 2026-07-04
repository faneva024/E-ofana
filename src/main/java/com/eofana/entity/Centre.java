package com.eofana.entity;

import com.eofana.enums.AbonnementType;
import com.eofana.enums.FrequenceReversement;
import com.eofana.enums.OperateurMm;
import com.eofana.enums.StatutCentre;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "\"centres\"")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Centre {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "\"idCentre\"")
    private Long idCentre;

    @NotNull(message = "Le centre doit être rattaché à un utilisateur")
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "\"idUser\"", nullable = false, unique = true)
    private Utilisateur utilisateur;

    @NotBlank(message = "Le nom du centre est obligatoire")
    @Size(max = 200, message = "Le nom ne doit pas dépasser 200 caractères")
    @Column(name = "\"nom\"", nullable = false, length = 200)
    private String nom;

    @Size(max = 255)
    @Column(name = "\"logo\"", length = 255)
    private String logo;

    @Column(name = "\"description\"", columnDefinition = "TEXT")
    private String description;

    @Column(name = "\"services\"", columnDefinition = "TEXT")
    private String services;

    @Size(max = 100)
    @Column(name = "\"ville\"", length = 100)
    private String ville;

    @Column(name = "\"adresse\"", columnDefinition = "TEXT")
    private String adresse;

    @Column(name = "\"fourchettePrixMin\"", nullable = false)
    @Builder.Default
    private Integer fourchettePrixMin = 0;

    @Column(name = "\"fourchettePrixMax\"", nullable = false)
    @Builder.Default
    private Integer fourchettePrixMax = 0;

    @Size(max = 20)
    @Column(name = "\"telephone\"", length = 20)
    private String telephone;

    @Size(max = 180)
    @Column(name = "\"email\"", length = 180)
    private String email;

    @Size(max = 255)
    @Column(name = "\"siteWeb\"", length = 255)
    private String siteWeb;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "\"reseauxSociaux\"", columnDefinition = "jsonb")
    private String reseauxSociaux;

    @NotNull
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "\"abonnement\"", nullable = false, columnDefinition = "\"abonnementType\"")
    @Builder.Default
    private AbonnementType abonnement = AbonnementType.basic;

    @NotNull
    // PAS D'ANNOTATION @Enumerated ici, c'est un BigDecimal !
    @Column(name = "\"tauxCommission\"", nullable = false, precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal tauxCommission = new BigDecimal("7.00");

    @NotNull
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "\"frequenceReversement\"", nullable = false, columnDefinition = "\"frequenceReversement\"")
    @Builder.Default
    private FrequenceReversement frequenceReversement = FrequenceReversement.mensuel;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "\"mobileMoneyOperateur\"", columnDefinition = "\"operateurMM\"")
    private OperateurMm mobileMoneyOperateur;

    @Size(max = 20)
    @Column(name = "\"mobileMoneyNumero\"", length = 20)
    private String mobileMoneyNumero;

    @NotNull
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "\"statut\"", nullable = false, columnDefinition = "\"statutCentre\"")
    @Builder.Default
    private StatutCentre statut = StatutCentre.enAttente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "\"idModerateur\"")
    private Utilisateur moderateur;

    @Column(name = "\"validatedAt\"")
    private LocalDateTime validatedAt;
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