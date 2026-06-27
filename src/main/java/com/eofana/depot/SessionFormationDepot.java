package com.eofana.depot;

import com.eofana.entite.SessionFormation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

/**
 * Dépôt SessionFormation — table "sessionsFormation" de eofanaDb v3.0
 */
@Repository
public interface SessionFormationDepot extends JpaRepository<SessionFormation, Long> {

    /**
     * Sessions ouvertes d'une formation, triées par date de début.
     */
    @Query("""
        SELECT s FROM SessionFormation s
        WHERE s.formation.idFormation = :idFormation
        AND s.statut = com.eofana.entite.SessionFormation.StatutSession.ouvert
        ORDER BY s.dateDebut ASC
    """)
    List<SessionFormation> trouverSessionsOuvertes(@Param("idFormation") Long idFormation);
}
