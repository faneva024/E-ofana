package com.eofana.entity;

import com.eofana.enums.OperateurMm;
import com.eofana.enums.StatutInscription;
import com.eofana.enums.TypeInscription;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;

/**
 * Entité JPA mappée sur la table "inscriptions" (schéma eofana).
 *
 * Correspond à la migration V5. Absente du code initial : les
 * contrôleurs InscriptionController / PaiementController manipulaient
 * la table directement en SQL brut via JdbcTemplate. Cette entité
 * rétablit la couche JPA prévue par T-B-007.
 *
 * Une inscription se rapporte toujours à une SESSION précise
 * (idSession), pas directement à une Formation : une même formation
 * peut avoir plusieurs sessions (dates différentes), chacune avec
 * son propre quota de places.
 */
@Entity
@Table(name = "\"inscriptions\"")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Inscription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "\"idInscription\"")
    private Long idInscription;

    @NotNull(message = "L'inscription doit être rattachée à un utilisateur")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "\"idUser\"", nullable = false)
    private Utilisateur utilisateur;

    @NotNull(message = "L'inscription doit être rattachée à une session")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "\"idSession\"", nullable = false)
    private SessionFormation session;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "\"typeInsc\"", nullable = false, columnDefinition = "\"typeInscription\"")
    @Builder.Default
    private TypeInscription typeInsc = TypeInscription.inscription;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "\"statut\"", nullable = false, columnDefinition = "\"statutInscription\"")
    @Builder.Default
    private StatutInscription statut = StatutInscription.enAttente;

    /**
     * Montant total dû une fois l'inscription finalisée (prix de la
     * formation moins la remise E-OFANA de 5%). Pour une réservation,
     * ce montant représente le total à terme, pas ce qui est
     * effectivement encaissé aujourd'hui (voir Paiement).
     */
    @NotNull
    @PositiveOrZero
    @Column(name = "\"montantPaye\"", nullable = false)
    @Builder.Default
    private Long montantPaye = 0L;

    @NotNull
    @PositiveOrZero
    @Column(name = "\"remise\"", nullable = false)
    @Builder.Default
    private Long remise = 0L;

    @NotNull
    @PositiveOrZero
    @Column(name = "\"commission\"", nullable = false)
    @Builder.Default
    private Long commission = 0L;

    @NotNull
    @PositiveOrZero
    @Column(name = "\"montantFormateur\"", nullable = false)
    @Builder.Default
    private Long montantFormateur = 0L;

    @Enumerated(EnumType.STRING)
    @Column(name = "\"operateur\"", columnDefinition = "\"operateurMm\"")
    private OperateurMm operateur;

    @Column(name = "\"transactionId\"", length = 100)
    private String transactionId;

    @Column(name = "\"numeroRecu\"", unique = true, length = 50)
    private String numeroRecu;

    @Column(name = "\"recuPdf\"", length = 255)
    private String recuPdf;

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
