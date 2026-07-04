package com.eofana.repository;

import com.eofana.entity.Utilisateur;
import com.eofana.enums.RoleUtilisateur;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
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

   // ==========================================
    // Méthodes héritées de la Semaine 1 (Apprenant)
    // ==========================================
    Optional<Utilisateur> findByEmail(String email);
    Boolean existsByEmail(String email);
    Optional<Utilisateur> findByUuid(UUID uuid);

    // ==========================================
    // Nouvelles méthodes de la Semaine 2 (Formateur)
    // ==========================================
    
    /**
     * Recherche un utilisateur par son email et son rôle.
     * Utile pour la connexion exclusive sur l'espace Formateur.
     */
    Optional<Utilisateur> findByEmailAndRole(String email, RoleUtilisateur role);

    /**
     * Vérifie si un utilisateur existe avec cet email et ce rôle.
     */
    Boolean existsByEmailAndRole(String email, RoleUtilisateur role);
}
