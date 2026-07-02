package com.eofana.repository;

import com.eofana.entity.SessionFormation;
import com.eofana.enums.StatutSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

/**
 * Dépôt Spring Data JPA pour l'entité SessionFormation.
 *
 * Porte calculerPlacesRestantes() — demandée par le ticket T-B-003
 * sur Formation, mais qui dépend en réalité des sessions (voir la
 * note de conception dans Formation.java).
 */
public interface SessionFormationRepository extends JpaRepository<SessionFormation, Long> {

    /** Toutes les sessions d'une formation donnée. */
    List<SessionFormation> findByFormation_IdFormation(Long idFormation);

    /**
     * Sessions encore ouvertes à l'inscription pour une formation
     * (statut = ouvert ET date limite non dépassée).
     */
    @Query("""
            SELECT s FROM SessionFormation s
            WHERE s.formation.idFormation = :idFormation
              AND s.statut = 'ouvert'
              AND s.dateLimiteInscription >= CURRENT_DATE
            ORDER BY s.dateDebut ASC
            """)
    List<SessionFormation> trouverSessionsOuvertes(@Param("idFormation") Long idFormation);

    /**
     * Total des places encore disponibles pour une formation, en
     * additionnant placesRestantes de TOUTES ses sessions actuellement
     * ouvertes. Renvoie 0 (jamais null) si la formation n'a aucune
     * session ouverte.
     *
     * C'est la méthode "calculerPlacesRestantes()" demandée par le
     * ticket T-B-003 — rattachée ici à SessionFormation plutôt qu'à
     * Formation, puisque c'est sur cette table que vivent réellement
     * les colonnes placesTotal / placesRestantes.
     *
     * @param idFormation l'identifiant de la formation concernée
     * @return la somme des places restantes sur les sessions ouvertes
     */
    @Query("""
            SELECT COALESCE(SUM(s.placesRestantes), 0) FROM SessionFormation s
            WHERE s.formation.idFormation = :idFormation
              AND s.statut = 'ouvert'
              AND s.dateLimiteInscription >= CURRENT_DATE
            """)
    Integer calculerPlacesRestantes(@Param("idFormation") Long idFormation);

    /**
     * Variante pour une session précise (pas agrégée sur toute la
     * formation) — utile sur la page d'inscription, où l'apprenant
     * choisit une session précise et doit voir SES places restantes,
     * pas le total toutes sessions confondues.
     */
    @Query("SELECT s.placesRestantes FROM SessionFormation s WHERE s.idSession = :idSession")
    Integer calculerPlacesRestantesPourSession(@Param("idSession") Long idSession);

    /**
     * Sessions dont la date limite d'inscription est dépassée mais
     * qui sont encore marquées "ouvert" — utile pour un futur job
     * planifié qui les basculerait automatiquement en "termine".
     */
    List<SessionFormation> findByStatutAndDateLimiteInscriptionBefore(
            StatutSession statut, LocalDate date);
}
