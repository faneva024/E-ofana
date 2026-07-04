package com.eofana.entity;

import com.eofana.enums.StatutModification;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;

@Entity
@Table(
    name = "\"demandesModificationCentre\"",
    indexes = {
        @Index(name = "idxDemandesCentre", columnList = "\"idCentre\""),
        @Index(name = "idxDemandesStatut", columnList = "\"statut\"")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DemandeModificationCentre {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "\"idDemande\"")
    private Long idDemande;


    @NotNull(message = "La demande doit être rattachée à un centre")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "\"idCentre\"", nullable = false)
    private Centre centre;

    @NotNull(message = "Les données proposées sont obligatoires")
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "\"donneesProposees\"", nullable = false, columnDefinition = "jsonb")
    private String donneesProposees;

    @Column(name = "\"commentaireAdmin\"", columnDefinition = "TEXT")
    private String commentaireAdmin;

    @NotNull
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "\"statut\"", nullable = false, columnDefinition = "\"statutModification\"")
    @Builder.Default
    private StatutModification statut = StatutModification.enAttente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "\"traiteParId\"")
    private Utilisateur traitePar;

    @Column(name = "\"traiteAt\"")
    private OffsetDateTime traiteAt;

    @Column(name = "\"createdAt\"", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @PrePersist
    public void prePersist() {
        if (this.createdAt == null) {
            this.createdAt = OffsetDateTime.now();
        }
        // Sécurité : statut toujours enAttente à la création,
        if (this.statut == null) {
            this.statut = StatutModification.enAttente;
        }
    }
}
