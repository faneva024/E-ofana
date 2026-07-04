package com.eofana.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Corps de la requête POST /api/v1/auth/inscription (T-B-009).
 * Crée toujours un compte de rôle "apprenant" — l'auto-inscription
 * formateur n'existe pas (comptes créés par le commercial).
 */
public record RegisterRequest(

        @NotBlank(message = "Le nom est obligatoire")
        @Size(max = 100)
        String nom,

        @NotBlank(message = "Le prénom est obligatoire")
        @Size(max = 100)
        String prenom,

        @NotBlank(message = "L'email est obligatoire")
        @Email(message = "Le format de l'email est invalide")
        @Size(max = 180)
        String email,

        @NotBlank(message = "Le mot de passe est obligatoire")
        @Size(min = 8, message = "Le mot de passe doit contenir au moins 8 caractères")
        String motDePasse,

        @Size(max = 20)
        String telephone
) {
}
