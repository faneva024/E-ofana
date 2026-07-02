package com.eofana.repository;

import com.eofana.entity.Utilisateur;
import com.eofana.enums.RoleUtilisateur;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Dépôt (repository) Spring Data JPA pour l'entité Utilisateur.
 *
 * Remarque de nommage : le ticket T-B-002 mentionnait "ApprenantDepot",
 * mais comme la base utilise une table unique "utilisateurs" pour tous
 * les rôles (et non une table "apprenants" séparée), ce dépôt s'appelle
 * UtilisateurRepository — nom idiomatique Spring Data JPA, cohérent
 * avec l'entité Utilisateur qu'il manipule.
 *
 * Spring Data JPA génère automatiquement l'implémentation de toutes
 * les méthodes ci-dessous à partir de leur seule signature : il n'y a
 * aucun code SQL ou JPQL à écrire pour ces cas simples.
 */
public interface UtilisateurRepository extends JpaRepository<Utilisateur, Long> {

    /**
     * Recherche un utilisateur par son email.
     *
     * Utilisée pour l'authentification (login) : l'email étant la
     * seule colonne réellement indexée pour la connexion (idxUtilisateursEmail),
     * cette requête reste rapide même avec beaucoup d'utilisateurs.
     *
     * Renvoie un Optional vide si aucun compte ne correspond à cet
     * email, plutôt que null — c'est à l'appelant (le service) de
     * décider quoi faire dans ce cas (ex. lever une exception métier).
     */
    Optional<Utilisateur> findByEmail(String email);

    /**
     * Vérifie l'existence d'un email en base sans charger l'entité
     * entière. Utilisée à l'inscription pour valider l'unicité de
     * l'email avant de tenter un INSERT (message d'erreur clair côté
     * service plutôt qu'une exception de contrainte SQL bute sur
     * l'utilisateur final).
     */
    boolean existsByEmail(String email);

    /**
     * Liste tous les utilisateurs d'un rôle donné.
     * Exploite l'index idxUtilisateursRole défini en V7.
     * Utile par exemple pour un futur écran admin "Liste des formateurs".
     */
    List<Utilisateur> findByRole(RoleUtilisateur role);
}
