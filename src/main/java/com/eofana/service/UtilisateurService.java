package com.eofana.service;

import com.eofana.entity.Utilisateur;
import com.eofana.repository.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Service métier pour l'entité Utilisateur.
 *
 * Le repository (UtilisateurRepository) ne fait QUE de l'accès aux
 * données : pas de logique de hashage ni de règle métier dedans.
 * C'est ce service qui porte les deux méthodes utilitaires demandées
 * par le ticket T-B-002 : chercherParEmail() et verifierMotDePasse().
 */
@Service
@RequiredArgsConstructor
public class UtilisateurService {

    private final UtilisateurRepository utilisateurRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    /**
     * Recherche un utilisateur par son adresse email.
     *
     * @param email l'email saisi (typiquement sur l'écran de connexion)
     * @return l'utilisateur correspondant, ou Optional.empty() si
     *         aucun compte n'existe avec cet email
     */
    public Optional<Utilisateur> chercherParEmail(String email) {
        return utilisateurRepository.findByEmail(email);
    }

    /**
     * Vérifie qu'un mot de passe en clair correspond au hash BCrypt
     * stocké pour un utilisateur donné.
     *
     * IMPORTANT : on ne compare jamais deux chaînes en clair. BCrypt
     * recalcule le hash du mot de passe fourni avec le même sel que
     * celui déjà stocké, puis compare les deux hash — c'est la seule
     * façon sûre de vérifier un mot de passe.
     *
     * @param motDePasseEnClair le mot de passe tel que saisi par
     *                          l'utilisateur sur le formulaire de connexion
     * @param utilisateur       l'utilisateur dont on veut vérifier le mot de passe
     * @return true si le mot de passe est correct, false sinon
     */
    public boolean verifierMotDePasse(String motDePasseEnClair, Utilisateur utilisateur) {
        return passwordEncoder.matches(motDePasseEnClair, utilisateur.getMotDePasse());
    }

    /**
     * Hash un mot de passe en clair pour le stockage en base.
     * À utiliser à l'inscription, avant d'appeler save() sur le repository.
     * Ne fait jamais l'inverse (BCrypt n'est pas réversible).
     *
     * @param motDePasseEnClair le mot de passe choisi par l'utilisateur
     * @return le hash BCrypt à stocker dans Utilisateur.motDePasse
     */
    public String hacherMotDePasse(String motDePasseEnClair) {
        return passwordEncoder.encode(motDePasseEnClair);
    }

    /**
     * Vérifie si un email est déjà utilisé par un compte existant.
     * À appeler avant la création d'un nouvel utilisateur, pour
     * renvoyer un message d'erreur métier clair plutôt que de
     * laisser la contrainte UNIQUE de PostgreSQL lever une exception
     * technique peu compréhensible côté utilisateur final.
     *
     * @param email l'email à vérifier
     * @return true si un compte existe déjà avec cet email
     */
    public boolean emailDejaUtilise(String email) {
        return utilisateurRepository.existsByEmail(email);
    }
}
