package com.eofana.repository;

import com.eofana.entity.Formation;
import com.eofana.enums.StatutFormation;
import com.eofana.repository.projection.StatFormationParCentre;
import com.eofana.repository.projection.StatInscriptionParSession;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;


public interface FormationRepository extends JpaRepository<Formation, Long> {


    List<Formation> findByStatut(StatutFormation statut);

    /**
     * Formations approuvées qui ont AU MOINS une session encore ouverte
     */
    @Query("""
            SELECT DISTINCT f FROM Formation f
            JOIN SessionFormation s ON s.formation = f
            WHERE f.statut = :statut
              AND s.dateLimiteInscription >= CURRENT_DATE
            """)
    List<Formation> trouverFormationsActivesAvecSessionOuverte(@Param("statut") StatutFormation statut);

    default List<Formation> trouverFormationsActives() {
        return findByStatut(StatutFormation.approuve);
    }

    /** Raccourci : formations approuvées avec session encore ouverte (recommandé). */
    default List<Formation> trouverFormationsActivesEtNonExpirees() {
        return trouverFormationsActivesAvecSessionOuverte(StatutFormation.approuve);
    }

    /**
     * Formations approuvées d'une catégorie donnée.
     */
    @Query("""
            SELECT f FROM Formation f
            WHERE f.statut = :statut
              AND f.categorie.idCategorie = :idCategorie
            """)
    List<Formation> trouverParCategorie(@Param("idCategorie") Long idCategorie,
                                        @Param("statut") StatutFormation statut);

    /** Raccourci : formations approuvées d'une catégorie. */
    default List<Formation> trouverParCategorie(Long idCategorie) {
        return trouverParCategorie(idCategorie, StatutFormation.approuve);
    }

    /**
     * Recherche plein texte sur titre + description en français.
     */
    @Query(value = """
            SELECT * FROM eofana.formations f
            WHERE f."statut" = 'approuve'
              AND to_tsvector('french', f."titre" || ' ' || COALESCE(f."description", ''))
                  @@ to_tsquery('french', :motsCles)
            """, nativeQuery = true)
    List<Formation> rechercherParMotsCles(@Param("motsCles") String motsCles);

    Page<Formation> findByStatutOrderByCreatedAtDesc(StatutFormation statut, Pageable pageable);

    /**
     * Toutes les formations d'un centre, TOUS statuts confondus.
     */
    List<Formation> findByCentre_IdCentreOrderByCreatedAtDesc(Long idCentre);

    /**
     * Formations d'un centre filtrées par un statut précis.
     */
    List<Formation> findByCentre_IdCentreAndStatutOrderByUpdatedAtDesc(
            Long idCentre, StatutFormation statut);

    /**
     * Formations d'un centre filtrées par plusieurs statuts simultanément.
     */
    List<Formation> findByCentre_IdCentreAndStatutInOrderByUpdatedAtDesc(
            Long idCentre, Collection<StatutFormation> statuts);

    /**
     * Raccourci : formations d'un centre qui nécessitent une action du formateur * (rejetées ou en correction demandée).
     */
    default List<Formation> trouverFormationsACorrecter(Long idCentre) {
        return findByCentre_IdCentreAndStatutInOrderByUpdatedAtDesc(
                idCentre,
                List.of(StatutFormation.rejete, StatutFormation.correctionDemandee)
        );
    }

    /**
     * Nombre de formations d'un centre par statut — pour les badges du tableau de bord formateur (ex : "3 en attente de validation").
     */
    @Query("""
            SELECT f.statut, COUNT(f)
            FROM Formation f
            WHERE f.centre.idCentre = :idCentre
            GROUP BY f.statut
            """)
    List<Object[]> countByStatutPourCentre(@Param("idCentre") Long idCentre);

    /**
     * Vérifie si un centre possède déjà une formation avec un titre donné.
     */
    boolean existsByCentre_IdCentreAndTitreIgnoreCase(Long idCentre, String titre);

    /**
     * Formations en attente de modération, toutes centres confondus.
     */
    List<Formation> findByStatutOrderByUpdatedAtAsc(StatutFormation statut);

    /**
     * Formations d'un centre précis filtrées par statut.
     */
    List<Formation> findByCentre_IdCentreAndStatut(Long idCentre, StatutFormation statut);

    /**
     * Statistiques d'inscriptions PAR SESSION pour une formation donnée.
     */
    @Query(value = """
            SELECT
                s."idSession"                AS idSession,
                s."dateDebut"                AS dateDebut,
                s."dateFin"                  AS dateFin,
                s."placesTotal"              AS placesTotal,
                s."placesRestantes"          AS placesRestantes,
                COUNT(CASE WHEN i."typeInsc" = 'inscription'
                           AND i."statut" NOT IN ('annule', 'rembourse')
                           THEN 1 END)       AS nbInscrits,
                COUNT(CASE WHEN i."typeInsc" = 'reservation'
                           AND i."statut" NOT IN ('annule', 'rembourse')
                           THEN 1 END)       AS nbReservations,
                COUNT(CASE WHEN i."statut" NOT IN ('annule', 'rembourse')
                           THEN 1 END)       AS nbTotal,
                COALESCE(
                    SUM(CASE WHEN i."statut" = 'valide'
                             THEN i."montantFormateur" ELSE 0 END),
                    0)                       AS montantFormateurValide
            FROM eofana."sessionsFormation" s
            LEFT JOIN eofana."inscriptions" i
                   ON i."idSession" = s."idSession"
            WHERE s."idFormation" = :idFormation
            GROUP BY s."idSession", s."dateDebut", s."dateFin",
                     s."placesTotal", s."placesRestantes"
            ORDER BY s."dateDebut" ASC
            """, nativeQuery = true)
    List<StatInscriptionParSession> compterInscriptionsParSession(
            @Param("idFormation") Long idFormation);

    /**
     * Statistiques d'inscriptions PAR FORMATION pour un centre donné.
     */
    @Query(value = """
            SELECT
                f."idFormation"                               AS idFormation,
                f."titre"                                     AS titre,
                CAST(f."statut" AS TEXT)                      AS statut,
                COUNT(DISTINCT s."idSession")                 AS nbSessions,
                COUNT(DISTINCT CASE WHEN s."statut" = 'ouvert'
                                    AND s."dateLimiteInscription" >= CURRENT_DATE
                               THEN s."idSession" END)        AS nbSessionsOuvertes,
                COUNT(CASE WHEN i."typeInsc" = 'inscription'
                           AND i."statut" NOT IN ('annule', 'rembourse')
                           THEN 1 END)                        AS nbInscrits,
                COUNT(CASE WHEN i."typeInsc" = 'reservation'
                           AND i."statut" NOT IN ('annule', 'rembourse')
                           THEN 1 END)                        AS nbReservations,
                COALESCE(
                    SUM(CASE WHEN i."statut" = 'valide'
                             THEN i."montantFormateur" ELSE 0 END),
                    0)                                        AS montantFormateurTotal,
                COALESCE(
                    SUM(CASE WHEN s."statut" = 'ouvert'
                             AND s."dateLimiteInscription" >= CURRENT_DATE
                             THEN s."placesRestantes" ELSE 0 END),
                    0)                                        AS placesRestantesTotales
            FROM eofana."formations" f
            LEFT JOIN eofana."sessionsFormation" s
                   ON s."idFormation" = f."idFormation"
            LEFT JOIN eofana."inscriptions" i
                   ON i."idSession" = s."idSession"
            WHERE f."idCentre" = :idCentre
            GROUP BY f."idFormation", f."titre", f."statut", f."createdAt"
            ORDER BY f."createdAt" DESC
            """, nativeQuery = true)
    List<StatFormationParCentre> compterInscriptionsParCentre(
            @Param("idCentre") Long idCentre);
}
