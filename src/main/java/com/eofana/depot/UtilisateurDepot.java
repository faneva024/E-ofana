package com.eofana.depot;

import com.eofana.entite.Utilisateur;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

/**
 * Dépôt Utilisateur — remplace ApprenantDepot.
 * Aligné sur la table "utilisateurs" de eofanaDb v3.0.
 * Couvre tous les rôles (apprenant, formateur, admin…).
 */
@Repository
public interface UtilisateurDepot extends JpaRepository<Utilisateur, Long> {

    /** Rechercher un utilisateur par son email (identifiant de connexion). */
    Optional<Utilisateur> findByEmail(String email);

    /** Vérifier qu'un email n'est pas déjà utilisé (contrôle d'unicité). */
    boolean existsByEmail(String email);

    /** Compter les utilisateurs actifs par rôle. */
    long countByRole(Utilisateur.Role role);

    /** Compter les apprenants actifs. */
    long countByRoleAndEstActifTrue(Utilisateur.Role role);
}
