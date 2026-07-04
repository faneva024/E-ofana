package com.eofana.entity;

import com.eofana.enums.OperateurMm;
import com.eofana.enums.StatutVirement;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDate;
import java.time.OffsetDateTime;

/**
 * Entité JPA mappée sur la table "virements" (schéma eofana).
 *
 * Représente un virement (planifié ou effectué) reversant à un centre
 * ses gains, après déduction de la commission de la plateforme.
 * Contrainte applicative : montantNet = montantBrut - commission
 * (doublée d'un CHECK côté base de données, voir migration V8).
 */
@Entity
@Table(name = "\"virements\"")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Virement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "\"idVirement\"")
    private Long idVirement;

    @NotNull(message = "Le virement doit être rattaché à un centre")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "\"idCentre\"", nullable = false)
    private Centre centre;

    @NotNull(message = "Le montant brut est obligatoire")
    @Column(name = "\"montantBrut\"", nullable = false)
    private Long montantBrut;

    @NotNull(message = "La commission est obligatoire")
    @Column(name = "\"commission\"", nullable = false)
    private Long commission;

    /** Toujours recalculé automatiquement (voir prePersist/preUpdate) — ne pas fixer manuellement. */
    @Column(name = "\"montantNet\"", nullable = false)
    private Long montantNet;

    @NotNull
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "\"operateur\"", nullable = false, columnDefinition = "\"operateurMm\"")
    private OperateurMm operateur;

    @Size(max = 100)
    @Column(name = "\"referenceVirement\"", length = 100, unique = true)
    private String referenceVirement;

    @NotNull
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "\"statut\"", nullable = false, columnDefinition = "\"statutVirement\"")
    @Builder.Default
    private StatutVirement statut = StatutVirement.planifie;

    @NotNull(message = "Le début de la période couverte est obligatoire")
    @Column(name = "\"periodeDebut\"", nullable = false)
    private LocalDate periodeDebut;

    @NotNull(message = "La fin de la période couverte est obligatoire")
    @Column(name = "\"periodeFin\"", nullable = false)
    private LocalDate periodeFin;

    /** Renseigné une fois le virement réellement effectué (statut = effectue). */
    @Column(name = "\"dateVirement\"")
    private LocalDate dateVirement;

    @Column(name = "\"createdAt\"", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @PrePersist
    public void prePersist() {
        if (this.createdAt == null) {
            this.createdAt = OffsetDateTime.now();
        }
        recalculerMontantNet();
    }

    @PreUpdate
    public void preUpdate() {
        recalculerMontantNet();
    }

    private void recalculerMontantNet() {
        if (this.montantBrut != null && this.commission != null) {
            this.montantNet = this.montantBrut - this.commission;
        }
    }
}