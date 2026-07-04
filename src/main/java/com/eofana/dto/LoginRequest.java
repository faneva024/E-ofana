package com.eofana.dto;

import jakarta.validation.constraints.NotBlank;

/** Corps de la requête POST /api/v1/auth/connexion (T-B-009). */
public record LoginRequest(

        @NotBlank(message = "L'email est obligatoire")
        String email,

        @NotBlank(message = "Le mot de passe est obligatoire")
        String motDePasse
) {
}
