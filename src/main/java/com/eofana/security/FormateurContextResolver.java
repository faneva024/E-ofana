package com.eofana.security;

import com.eofana.entity.Centre;
import com.eofana.entity.Utilisateur;
import com.eofana.enums.RoleUtilisateur;
import com.eofana.repository.CentreRepository;
import com.eofana.repository.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Résout le formateur (et son centre) actuellement connecté à partir
 * de l'en-tête "Authorization: Bearer ...".
 *
 * ⚠ Pont temporaire : ce projet n'a pas encore de JwtAuthenticationFilter
 * ni de JwtService (voir SecurityFilterConfig, qui laisse toutes les
 * routes ouvertes en attendant une vraie authentification JWT/session).
 * Le token émis par /api/v1/auth-formateur/connexion est un simple
 * "TOKEN-DEMO-FORMATEUR-{idUser}", cohérent avec le "TOKEN-DEMO-{idUser}"
 * déjà utilisé par /api/v1/auth/connexion.
 *
 * Ce composant centralise donc la même logique (extraire l'idUser du
 * token, vérifier le rôle et le statut du compte) pour tous les
 * contrôleurs formateur, plutôt que de la dupliquer dans chacun. Le
 * jour où un vrai JwtAuthenticationFilter alimentera le
 * SecurityContext Spring (T-B-115), seule l'implémentation de cette
 * classe aura besoin de changer — pas les contrôleurs qui l'utilisent.
 */
@Component
@RequiredArgsConstructor
public class FormateurContextResolver {

    private static final String PREFIXE_TOKEN = "TOKEN-DEMO-FORMATEUR-";

    private final UtilisateurRepository utilisateurRepository;
    private final CentreRepository centreRepository;

    /**
     * @param authorizationHeader la valeur brute de l'en-tête Authorization
     *                            (ex. "Bearer TOKEN-DEMO-FORMATEUR-42")
     * @return le formateur authentifié
     * @throws AccesNonAutoriseException si le token est absent, mal formé,
     *         ne correspond à aucun compte, correspond à un compte non
     *         FORMATEUR, ou à un compte suspendu (estActif = false)
     */
    public Utilisateur resoudreFormateur(String authorizationHeader) {
        Long idUser = extraireIdUser(authorizationHeader);

        Utilisateur utilisateur = utilisateurRepository.findById(idUser)
                .orElseThrow(() -> new AccesNonAutoriseException("Session invalide, merci de vous reconnecter"));

        if (utilisateur.getRole() != RoleUtilisateur.formateur) {
            throw new AccesNonAutoriseException("Accès réservé aux formateurs");
        }

        if (Boolean.FALSE.equals(utilisateur.getEstActif())) {
            throw new AccesNonAutoriseException("Ce compte a été suspendu");
        }

        return utilisateur;
    }

    /** Comme resoudreFormateur(), mais renvoie directement le Centre associé (usage le plus fréquent). */
    public Centre resoudreCentre(String authorizationHeader) {
        Utilisateur formateur = resoudreFormateur(authorizationHeader);

        return centreRepository.findByUtilisateur_IdUser(formateur.getIdUser())
                .orElseThrow(() -> new AccesNonAutoriseException("Aucun centre associé à ce compte formateur"));
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

    /** Exception 401/403 dédiée, attrapée par les contrôleurs formateur. */
    public static class AccesNonAutoriseException extends RuntimeException {
        public AccesNonAutoriseException(String message) {
            super(message);
        }
    }
}
