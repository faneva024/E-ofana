package com.eofana.dto;

/**
 * Réponse JSON commune à /auth/inscription et /auth/connexion (T-B-009).
 * Le frontend stocke `token` et le renvoie dans l'en-tête
 * Authorization: Bearer {token} pour les routes protégées.
 */
public record AuthResponse(
        String token,
        Long idUser,
        String nom,
        String prenom,
        String email,
        String role
) {
}
