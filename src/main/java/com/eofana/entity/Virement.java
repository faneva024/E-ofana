package com.eofana.entity;

import com.eofana.enums.OperateurMm;
import com.eofana.enums.StatutVirement;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDate;
import java.time.OffsetDateTime;

/**
 * Entité Virement — table "virements" (créée en V9)
 * Alignée EXACTEMENT sur V9 :
 *   - FK vers "centres" (idCentre), pas utilisateurs
 *   - "commission" (pas commissionTotale)
 *   - "statut" de type ENUM "statutVirement"
 *   - "montantBrut", "montantNet" en BIGINT (Ariary entier)
 * Tâche T-B-113 — ROLPH (Semaine 2)
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

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "\"idCentre\"", nullable = false)
    private Centre centre;

    @Column(name = "\"montantBrut\"", nullable = false)
    private Long montantBrut;

    @Column(name = "\"commission\"", nullable = false)
    private Long commission;

    @Column(name = "\"montantNet\"", nullable = false)
    private Long montantNet;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "\"operateur\"", columnDefinition = "\"operateurMm\"")
    private OperateurMm operateur;

    @Column(name = "\"referenceVirement\"", length = 100)
    private String referenceVirement;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "\"statut\"", nullable = false, columnDefinition = "\"statutVirement\"")
    @Builder.Default
    private StatutVirement statut = StatutVirement.planifie;

    @Column(name = "\"periodeDebut\"")
    private LocalDate periodeDebut;

    @Column(name = "\"periodeFin\"")
    private LocalDate periodeFin;

    @Column(name = "\"dateVirement\"")
    private OffsetDateTime dateVirement;

    @Column(name = "\"createdAt\"", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @PrePersist
    public void prePersist() {
        if (this.createdAt == null) this.createdAt = OffsetDateTime.now();
        if (this.dateVirement == null) this.dateVirement = OffsetDateTime.now();
    }
}
