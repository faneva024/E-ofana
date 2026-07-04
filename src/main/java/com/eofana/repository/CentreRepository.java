package com.eofana.repository;

import com.eofana.entity.Centre;
import com.eofana.enums.StatutCentre;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CentreRepository extends JpaRepository<Centre, Long> {

    /**
     * Retrouve le centre rattaché à un formateur à partir de son identifiant.
     */
    Optional<Centre> findByUtilisateur_IdUser(Long idUser);

    /**
     * Retrouve tous les centres ayant un statut donné.
     */
    List<Centre> findByStatutOrderByCreatedAtAsc(StatutCentre statut);

    /**
     * Retrouve tous les centres actifs d'une ville donnée (insensible à la casse).
     */
    List<Centre> findByStatutAndVilleIgnoreCase(StatutCentre statut, String ville);

    /**
     * Vérifie si un formateur possède déjà un centre.
     */
    boolean existsByUtilisateur_IdUser(Long idUser);

    @Query("SELECT c.statut, COUNT(c) FROM Centre c GROUP BY c.statut")
    List<Object[]> countByStatut();

    /**
     * Retrouve les centres dont le nom contient le mot-clé (insensible à la casse).
     * Utilisé par la barre de recherche administration.
     */
    @Query("SELECT c FROM Centre c WHERE LOWER(c.nom) LIKE LOWER(CONCAT('%', :motCle, '%'))")
    List<Centre> rechercherParNom(@Param("motCle") String motCle);
}
