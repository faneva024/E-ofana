package com.eofana.entity;

import com.eofana.enums.OperateurMm;
import com.eofana.enums.StatutPaiement;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;

/**
 * Entité JPA mappée sur la table "paiements" (schéma eofana).
 *
 * Journal de toutes les transactions Mobile Money liées à une
 * inscription. Une même Inscription peut avoir PLUSIEURS Paiement :
 * typiquement un acompte (montant = commission) puis un solde, dans
 * le cas d'une réservation (typeInsc = reservation).
 */
@Entity
@Table(name = "\"paiements\"")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Paiement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "\"idPaiement\"")
    private Long idPaiement;

    @NotNull(message = "Le paiement doit être rattaché à une inscription")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "\"idInscription\"", nullable = false)
    private Inscription inscription;

    @NotNull(message = "Le montant est obligatoire")
    @Positive(message = "Le montant doit être strictement positif")
    @Column(name = "\"montant\"", nullable = false)
    private Long montant;

    @NotNull(message = "L'opérateur Mobile Money est obligatoire")
    @Enumerated(EnumType.STRING)
    @Column(name = "\"operateur\"", nullable = false, columnDefinition = "\"operateurMm\"")
    private OperateurMm operateur;

    @NotBlank(message = "Le numéro de transaction est obligatoire")
    @Column(name = "\"numeroTransaction\"", nullable = false, length = 100)
    private String numeroTransaction;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "\"statut\"", nullable = false, columnDefinition = "\"statutPaiement\"")
    @Builder.Default
    private StatutPaiement statut = StatutPaiement.enAttente;

    @Column(name = "\"createdAt\"", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @PrePersist
    public void prePersist() {
        if (this.createdAt == null) {
            this.createdAt = OffsetDateTime.now();
        }
    }
}
