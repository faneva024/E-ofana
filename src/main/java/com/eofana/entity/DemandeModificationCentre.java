package com.eofana.entity;

import com.eofana.enums.StatutModification;
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
 * Demande de modification du profil d'un centre, proposée par le
 * formateur mais jamais appliquée directement sur "centres" — un
 * modérateur/admin doit l'approuver ou la rejeter (cf. T-B-103 /
 * T-B-107, §3.2 du cahier des charges).
 */
@Entity
@Table(name = "\"demandesModificationCentre\"")
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

    /**
     * Contenu JSON libre proposé par le formateur (ex. { "ville":
     * "Antananarivo", "services": "..." }) — seuls les champs modifiés
     * sont présents. Appliqué sur "centres" uniquement en cas
     * d'approbation, par le service d'administration (hors périmètre
     * de ce module).
     */
    @NotNull
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
    }
}
