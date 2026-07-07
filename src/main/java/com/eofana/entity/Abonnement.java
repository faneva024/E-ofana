package com.eofana.entity;

import com.eofana.enums.AbonnementType;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

/**
 * Historique des abonnements souscrits par un centre.
 *
 * Ne remplace PAS centres.abonnement (la valeur "vivante" utilisée
 * par l'application), qui reste synchronisée vers
 * centres.tauxCommission par le trigger fnSyncTauxCommission. Cette
 * table sert uniquement de journal (facturation, audit, historique
 * affiché au formateur).
 */
@Entity
@Table(name = "\"abonnements\"")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Abonnement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "\"idAbonnement\"")
    private Long idAbonnement;

    @NotNull(message = "L'abonnement doit être rattaché à un centre")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "\"idCentre\"", nullable = false)
    private Centre centre;

    @NotNull
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "\"type\"", nullable = false, columnDefinition = "\"abonnementType\"")
    private AbonnementType type;

    @NotNull
    @Column(name = "\"tauxCommission\"", nullable = false, precision = 5, scale = 2)
    private BigDecimal tauxCommission;

    @NotNull
    @Column(name = "\"dateDebut\"", nullable = false)
    @Builder.Default
    private LocalDate dateDebut = LocalDate.now();

    @Column(name = "\"dateFin\"")
    private LocalDate dateFin;

    @Column(name = "\"createdAt\"", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @PrePersist
    public void prePersist() {
        if (this.createdAt == null) {
            this.createdAt = OffsetDateTime.now();
        }
    }
}
