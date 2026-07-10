package com.eofana.security;

import com.eofana.entity.Utilisateur;
import com.eofana.enums.RoleUtilisateur;
import com.eofana.repository.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Résout l'admin actuellement connecté à partir de l'en-tête
 * "Authorization: Bearer ...", en miroir exact de
 * FormateurContextResolver (module Formateur).
 *
 * Le token émis par /api/v1/auth-admin/connexion (T-B-204) est un
 * "TOKEN-DEMO-ADMIN-{idUser}", cohérent avec "TOKEN-DEMO-{idUser}" et
 * "TOKEN-DEMO-FORMATEUR-{idUser}" déjà utilisés ailleurs dans le
 * projet — DemoTokenAuthenticationFilter (T-B-215) reconnaît ce
 * nouveau préfixe pour alimenter le SecurityContext (ROLE_ADMIN), et
 * ce composant centralise la même logique pour tous les contrôleurs
 * admin qui ont besoin de savoir QUI a pris une décision (audit trail
 * de T-B-205/T-B-206/T-B-214).
 */
@Component
@RequiredArgsConstructor
public class AdminContextResolver {

    private static final String PREFIXE_TOKEN = "TOKEN-DEMO-ADMIN-";

    private final UtilisateurRepository utilisateurRepository;

    /**
     * @param authorizationHeader la valeur brute de l'en-tête Authorization
     *                            (ex. "Bearer TOKEN-DEMO-ADMIN-1")
     * @return l'admin authentifié
     * @throws AccesNonAutoriseException si le token est absent, mal formé,
     *         ne correspond à aucun compte, correspond à un compte non
     *         ADMIN, ou à un compte suspendu (estActif = false)
     */
    public Utilisateur resoudreAdmin(String authorizationHeader) {
        Long idUser = extraireIdUser(authorizationHeader);

        Utilisateur utilisateur = utilisateurRepository.findById(idUser)
                .orElseThrow(() -> new AccesNonAutoriseException("Session invalide, merci de vous reconnecter"));

        if (utilisateur.getRole() != RoleUtilisateur.admin) {
            throw new AccesNonAutoriseException("Accès réservé aux administrateurs");
        }

        if (Boolean.FALSE.equals(utilisateur.getEstActif())) {
            throw new AccesNonAutoriseException("Ce compte a été suspendu");
        }

        return utilisateur;
    }

    private Long extraireIdUser(String authorizationHeader) {
        if (authorizationHeader == null || authorizationHeader.isBlank()) {
            throw new AccesNonAutoriseException("Authentification requise");
        }

        String token = authorizationHeader.startsWith("Bearer ")
                ? authorizationHeader.substring("Bearer ".length()).trim()
                : authorizationHeader.trim();

        if (!token.startsWith(PREFIXE_TOKEN)) {
            throw new AccesNonAutoriseException("Token invalide");
        }

        try {
            return Long.parseLong(token.substring(PREFIXE_TOKEN.length()));
        } catch (NumberFormatException e) {
            throw new AccesNonAutoriseException("Token invalide");
        }
    }

    /** Exception 401/403 dédiée, attrapée par les contrôleurs admin. */
    public static class AccesNonAutoriseException extends RuntimeException {
        public AccesNonAutoriseException(String message) {
            super(message);
        }
    }
}
