package com.eofana.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDate;

/**
 * DTO — corps des requêtes POST et PUT /api/v1/formations (formateur)
 * Tâche T-B-108 — ROLPH (Semaine 2)
 */
public record FormationFormateurRequete(

    @NotBlank(message = "Le titre est obligatoire.")
    String titre,

    @NotBlank(message = "La description est obligatoire.")
    String description,

    String categorie,

    @NotBlank(message = "Le lieu est obligatoire.")
    String lieu,

    String duree,

    @NotNull(message = "La date de début est obligatoire.")
    LocalDate dateDebut,

    @NotNull(message = "La date limite d'inscription est obligatoire.")
    LocalDate dateLimiteInscription,

    @NotNull(message = "Le nombre de places est obligatoire.")
    @Positive(message = "Le nombre de places doit être positif.")
    Integer placesTotal,

    @NotNull(message = "Le prix est obligatoire.")
    @Positive(message = "Le prix doit être positif.")
    Long prix,

    Long prixRemise,

    String image
) {}
