package com.eofana.entity;

import com.eofana.enums.OperateurMm;
import com.eofana.enums.StatutPaiement;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;


@Entity
@Table(
    name = "\"paiements\"",
    indexes = {
        @Index(name = "idxPaiementsInscription", columnList = "\"idInscription\""),
        @Index(name = "idxPaiementsStatut",      columnList = "\"statut\"")
    }
)
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

    /**
     * Inscription pour laquelle ce paiement a été effectué.
     */
    @NotNull(message = "Le paiement doit être rattaché à une inscription")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "\"idInscription\"", nullable = false)
    private Inscription inscription;


    /**
     * Montant de la transaction, en Ariary.
     */
    @NotNull(message = "Le montant est obligatoire")
    @Positive(message = "Le montant doit être positif")
    @Column(name = "\"montant\"", nullable = false)
    private Long montant;

    /**
     * Opérateur Mobile Money utilisé pour ce paiement + Valeurs : mvola, orange, airtel.
     */
    @NotNull(message = "L'opérateur de paiement est obligatoire")
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "\"operateur\"", nullable = false, columnDefinition = "\"operateurMm\"")
    private OperateurMm operateur;

    /**
     * Référence unique de la transaction fournie par l'opérateur Mobile Money.
     * Exemples : "MVL202606001" (Mvola), "OM20260601001" (Orange Money).
     */
    @NotBlank(message = "Le numéro de transaction est obligatoire")
    @Column(name = "\"numeroTransaction\"", nullable = false, length = 100)
    private String numeroTransaction;


    /**
     * Statut courant de la transaction.
     */
    @NotNull
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "\"statut\"", nullable = false, columnDefinition = "\"statutPaiement\"")
    @Builder.Default
    private StatutPaiement statut = StatutPaiement.enAttente;

    /**
     * Date/heure de création de la transaction (DEFAULT NOW() en DB).
     */
    @Column(name = "\"createdAt\"", nullable = false, updatable = false, insertable = false)
    private OffsetDateTime createdAt;
}
