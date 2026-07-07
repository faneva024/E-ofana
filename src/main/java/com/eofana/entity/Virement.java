package com.eofana.entity;

import com.eofana.enums.OperateurMm;
import com.eofana.enums.StatutVirement;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
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
 * Virement Mobile Money effectué (ou planifié) vers un centre de
 * formation, correspondant à une période de reversement (cf.
 * T-B-105 / T-B-113).
 *
 * Contrainte applicative ET base (chkVirementMontantNet) :
 * montantNet = montantBrut - commission.
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

    @NotNull
    @Column(name = "\"montantBrut\"", nullable = false)
    private Long montantBrut;

    @NotNull
    @Column(name = "\"commission\"", nullable = false)
    private Long commission;

    @NotNull
    @Column(name = "\"montantNet\"", nullable = false)
    private Long montantNet;

    @NotNull
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "\"operateur\"", nullable = false, columnDefinition = "\"operateurMm\"")
    private OperateurMm operateur;

    @Column(name = "\"referenceVirement\"", unique = true, length = 100)
    private String referenceVirement;

    @NotNull
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "\"statut\"", nullable = false, columnDefinition = "\"statutVirement\"")
    @Builder.Default
    private StatutVirement statut = StatutVirement.planifie;

    @NotNull
    @Column(name = "\"periodeDebut\"", nullable = false)
    private LocalDate periodeDebut;

    @NotNull
    @Column(name = "\"periodeFin\"", nullable = false)
    private LocalDate periodeFin;

    @Column(name = "\"dateVirement\"")
    private OffsetDateTime dateVirement;

    @Column(name = "\"createdAt\"", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @PrePersist
    public void prePersist() {
        if (this.createdAt == null) {
            this.createdAt = OffsetDateTime.now();
        }
        // Garde-fou applicatif en plus de la contrainte SQL chkVirementMontantNet
        if (this.montantBrut != null && this.commission != null) {
            this.montantNet = this.montantBrut - this.commission;
        }
    }
}
