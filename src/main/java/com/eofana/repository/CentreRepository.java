package com.eofana.repository;

import com.eofana.entity.Centre;
import com.eofana.enums.AbonnementType;
import com.eofana.enums.StatutCentre;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * Dépôt Spring Data JPA pour l'entité Centre.
 * Minimal pour ce sprint — sera enrichi quand le module Formateur
 * (gestion de profil centre, etc.) sera développé.
 */
public interface CentreRepository extends JpaRepository<Centre, Long> {

    /**
     * Retrouve le centre d'un utilisateur (relation OneToOne).
     * Utile pour aller de "l'utilisateur connecté" à "son centre"
     * une fois l'authentification en place.
     */
    Optional<Centre> findByUtilisateur_IdUser(Long idUser);

    /**
     * T-B-207 : liste paginée des centres/formateurs pour l'écran
     * admin "Gestion des formateurs", filtrable par nom, ville,
     * statut et type d'abonnement — chaque filtre est ignoré s'il
     * est null, pour que les paramètres restent tous optionnels et
     * combinables (mêmes principes que AdminStatsFilterController,
     * T-B-210).
     */
    @Query("""
            SELECT c FROM Centre c
            WHERE (:nom IS NULL OR LOWER(c.nom) LIKE LOWER(CONCAT('%', :nom, '%')))
              AND (:ville IS NULL OR LOWER(c.ville) LIKE LOWER(CONCAT('%', :ville, '%')))
              AND (:statut IS NULL OR c.statut = :statut)
              AND (:abonnement IS NULL OR c.abonnement = :abonnement)
            """)
    Page<Centre> rechercherFormateurs(@Param("nom") String nom,
                                       @Param("ville") String ville,
                                       @Param("statut") StatutCentre statut,
                                       @Param("abonnement") AbonnementType abonnement,
                                       Pageable pageable);

    /**
     * T-B-203 : nombre de centres par type d'abonnement, base de
     * AdminStatsService#getGlobalStats() (répartition Basic/Premium)
     * et des filtres AdminStatsFilterController (T-B-210).
     */
    long countByAbonnement(AbonnementType abonnement);

    /**
     * T-B-203 : recherche des centres par ville OU région (nom de
     * méthode imposé par le ticket). Le schéma actuel (V4) n'a
     * qu'une seule colonne "ville" (pas de colonne "region" séparée) :
     * une dérivation Spring Data "findByVilleOrRegion" échouerait au
     * démarrage (aucune propriété "region" sur Centre) — @Query
     * explicite donc la recherche sur ce seul champ, insensible à la
     * casse, en attendant qu'une colonne "region" soit ajoutée si
     * besoin.
     */
    @Query("SELECT c FROM Centre c WHERE LOWER(c.ville) = LOWER(:villeOuRegion)")
    List<Centre> findByVilleOrRegion(@Param("villeOuRegion") String villeOuRegion);
}
