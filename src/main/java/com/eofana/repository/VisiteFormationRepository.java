package com.eofana.repository;

import com.eofana.entity.VisiteFormation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Dépôt VisiteFormation — table "visiteFormation" (V13)
 * Tâche T-B-104 (FANEVA) / T-B-109 (ROLPH, Semaine 2)
 */
@Repository
public interface VisiteFormationRepository extends JpaRepository<VisiteFormation, Long> {

    /** Nombre de visites pour une formation */
    long countByFormation_IdFormation(Long idFormation);

    /** Nombre total de visites pour toutes les formations d'un centre */
    @Query("SELECT COUNT(v) FROM VisiteFormation v " +
           "WHERE v.formation.centre.idCentre = :idCentre")
    long countTotalVisitesParCentre(@Param("idCentre") Long idCentre);
}
