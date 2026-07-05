package com.eofana.entity;

import com.eofana.enums.AbonnementType;
import com.eofana.enums.FrequenceReversement;
import com.eofana.enums.OperateurMm;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

/**
 * Entité ProfilCentre — table "profilCentre" (V13)
 * Alignée sur eofanaDb v3.0 : camelCase + guillemets, Lombok, OffsetDateTime.
 * Réutilise les enums existants AbonnementType, FrequenceReversement, OperateurMm.
 * Tâche T-B-101 — ROLPH (Semaine 2)
 */
@Entity
@Table(name = "\"profilCentre\"")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProfilCentre {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "\"idCentre\"")
    private Long idCentre;

    @NotNull
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "\"idFormateur\"", nullable = false, unique = true)
    private Utilisateur formateur;

    @NotBlank
    @Column(name = "\"nomCentre\"", nullable = false, length = 200)
    private String nomCentre;

    @Column(name = "\"logo\"", length = 500)
    private String logo;

    @Column(name = "\"description\"", columnDefinition = "TEXT")
    private String description;

    @Column(name = "\"servicesProposes\"", columnDefinition = "TEXT")
    private String servicesProposes;

    @Column(name = "\"ville\"", length = 100)
    private String ville;

    @Column(name = "\"adresse\"", columnDefinition = "TEXT")
    private String adresse;

    @Column(name = "\"telephone\"", length = 20)
    private String telephone;

    @Column(name = "\"emailContact\"", length = 255)
    private String emailContact;

    @Column(name = "\"siteWeb\"", length = 300)
    private String siteWeb;

    @Column(name = "\"reseauxSociaux\"", length = 500)
    private String reseauxSociaux;

    @Column(name = "\"prixMin\"", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal prixMin = BigDecimal.ZERO;

    @Column(name = "\"prixMax\"", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal prixMax = BigDecimal.ZERO;

    @NotNull
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "\"typeAbonnement\"", nullable = false, columnDefinition = "\"abonnementType\"")
    @Builder.Default
    private AbonnementType typeAbonnement = AbonnementType.basic;

    @NotNull
    @Column(name = "\"tauxCommission\"", nullable = false, precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal tauxCommission = new BigDecimal("7.00");

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "\"operateurMobileMoney\"", columnDefinition = "\"operateurMm\"")
    private OperateurMm operateurMobileMoney;

    @Column(name = "\"numeroMobileMoney\"", length = 20)
    private String numeroMobileMoney;

    @NotNull
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "\"frequenceReversement\"", nullable = false,
            columnDefinition = "\"frequenceReversement\"")
    @Builder.Default
    private FrequenceReversement frequenceReversement = FrequenceReversement.mensuel;

    /** enAttente | approuve | rejete */
    @Column(name = "\"statutProfil\"", nullable = false, length = 20)
    @Builder.Default
    private String statutProfil = "enAttente";

    @Column(name = "\"motifRejet\"", columnDefinition = "TEXT")
    private String motifRejet;

    @Column(name = "\"validatedAt\"")
    private OffsetDateTime validatedAt;

    @Column(name = "\"createdAt\"", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "\"updatedAt\"", nullable = false)
    private OffsetDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        if (this.createdAt == null) this.createdAt = OffsetDateTime.now();
        if (this.updatedAt == null) this.updatedAt = OffsetDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = OffsetDateTime.now();
    }

    public boolean estApprouve() {
        return "approuve".equals(this.statutProfil);
    }

    /** Recalcule le taux selon l'abonnement : basic=7%, premium=15% */
    public void appliquerTauxCommission() {
        this.tauxCommission = AbonnementType.premium.equals(this.typeAbonnement)
            ? new BigDecimal("15.00")
            : new BigDecimal("7.00");
    }
}
