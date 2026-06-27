package com.eofana.entity;

import com.eofana.enums.StatutSession;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.OffsetDateTime;

@Entity
@Table(name = "\"sessionsFormation\"")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SessionFormation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "\"idSession\"")
    private Long idSession;

    @NotNull(message = "La session doit être rattachée à une formation")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "\"idFormation\"", nullable = false)
    private Formation formation;

    @NotNull(message = "La date de début est obligatoire")
    @Column(name = "\"dateDebut\"", nullable = false)
    private LocalDate dateDebut;

    @Column(name = "\"dateFin\"")
    private LocalDate dateFin;

    @NotNull(message = "La date limite d'inscription est obligatoire")
    @Column(name = "\"dateLimiteInscription\"", nullable = false)
    private LocalDate dateLimiteInscription;

    @NotNull
    @Min(value = 1, message = "Le nombre total de places doit être positif")
    @Column(name = "\"placesTotal\"", nullable = false)
    @Builder.Default
    private Integer placesTotal = 20;

    @NotNull
    @Min(value = 0, message = "Les places restantes ne peuvent pas être négatives")
    @Column(name = "\"placesRestantes\"", nullable = false)
    @Builder.Default
    private Integer placesRestantes = 20;

    @NotNull
    @Enumerated(EnumType.STRING) // AJOUT: Spécifie que c'est un Enum
    @Column(name = "\"statut\"", nullable = false, columnDefinition = "VARCHAR(50)") // AJOUT
    @Builder.Default
    private StatutSession statut = StatutSession.ouvert;

    @Column(name = "\"createdAt\"", nullable = false, updatable = false, insertable = false)
    private OffsetDateTime createdAt;
}