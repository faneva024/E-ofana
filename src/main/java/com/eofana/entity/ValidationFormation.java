package com.eofana.entity;

import com.eofana.enums.StatutFormation;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;

/**
 * Trace chaque décision de modération prise sur une formation
 * (approbation, rejet, demande de correction) — audit trail demandé
 * par T-B-101. Complémentaire à Formation.statut/motifRejet, qui ne
 * garde que l'état courant.
 */
@Entity
@Table(name = "\"validationFormations\"")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ValidationFormation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "\"idValidation\"")
    private Long idValidation;

    @NotNull(message = "La validation doit être rattachée à une formation")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "\"idFormation\"", nullable = false)
    private Formation formation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "\"idModerateur\"")
    private Utilisateur moderateur;

    @NotNull
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "\"decision\"", nullable = false, columnDefinition = "\"statutFormation\"")
    private StatutFormation decision;

    @Column(name = "\"commentaire\"", columnDefinition = "TEXT")
    private String commentaire;

    @Column(name = "\"createdAt\"", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @PrePersist
    public void prePersist() {
        if (this.createdAt == null) {
            this.createdAt = OffsetDateTime.now();
        }
    }
}
