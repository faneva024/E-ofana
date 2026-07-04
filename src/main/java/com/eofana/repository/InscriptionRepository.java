package com.eofana.repository;

import com.eofana.entity.Inscription;
import com.eofana.enums.StatutInscription;
import com.eofana.enums.TypeInscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;


public interface InscriptionRepository extends JpaRepository<Inscription, Long> {

    // ════════════════════════════════════════════════════════════════════════════
    // 1. MODULE APPRENANT
    // ════════════════════════════════════════════════════════════════════════════
    /**
     * Toutes les inscriptions d'un apprenant, tous statuts confondus.
     */
    List<Inscription> findByUtilisateur_IdUserOrderByCreatedAtDesc(Long idUser);

    /**
     * Inscriptions d'un apprenant filtrées par statut.
     */
    List<Inscription> findByUtilisateur_IdUserAndStatutOrderByCreatedAtDesc(
            Long idUser, StatutInscription statut);

    /**
     * Inscriptions d'un apprenant filtrées par type (inscription ou réservation).
     */
    List<Inscription> findByUtilisateur_IdUserAndTypeInscOrderByCreatedAtDesc(
            Long idUser, TypeInscription typeInsc);

    /**
     * Inscription unique d'un apprenant sur une session donnée.
     */
    Optional<Inscription> findByUtilisateur_IdUserAndSession_IdSession(
            Long idUser, Long idSession);

    /**
     * Vérifie si un apprenant est déjà inscrit (ou réservé) à une session.
     */
    boolean existsByUtilisateur_IdUserAndSession_IdSession(Long idUser, Long idSession);

    // ════════════════════════════════════════════════════════════════════════════
    // 2. MODULE FORMATEUR — inscrits par session / formation
    // ════════════════════════════════════════════════════════════════════════════
    /**
     * Toutes les inscriptions d'une session.
     */
    List<Inscription> findBySession_IdSessionOrderByCreatedAtAsc(Long idSession);

    /**
     * Inscriptions d'une session filtrées par statut.
     */
    List<Inscription> findBySession_IdSessionAndStatut(Long idSession, StatutInscription statut);

    /**
     * Toutes les inscriptions de toutes les sessions d'une formation.
     */
    List<Inscription> findBySession_Formation_IdFormation(Long idFormation);

    /**
     * Nombre d'inscriptions actives (non annulées, non remboursées) sur une session.
     */
    @Query("""
            SELECT COUNT(i) FROM Inscription i
            WHERE i.session.idSession = :idSession
              AND i.statut NOT IN :statutsExclus
            """)
    long countActivesBySession(
            @Param("idSession")     Long idSession,
            @Param("statutsExclus") java.util.Collection<StatutInscription> statutsExclus);


    /**
     * C'est le cash réel encaissé par E-OFANA pour ce centre.
     */
    @Query(value = """
            SELECT COALESCE(SUM(i."montantPaye"), 0)
            FROM eofana."inscriptions" i
            JOIN eofana."sessionsFormation" s ON s."idSession"   = i."idSession"
            JOIN eofana."formations"        f ON f."idFormation" = s."idFormation"
            WHERE f."idCentre" = :idCentre
              AND i."statut"   = 'valide'
            """, nativeQuery = true)
    Long sumMontantPayeValideParCentre(@Param("idCentre") Long idCentre);

    /**
     * Commission totale retenue par E-OFANA sur les inscriptions VALIDE du centre.
     */
    @Query(value = """
            SELECT COALESCE(SUM(i."commission"), 0)
            FROM eofana."inscriptions" i
            JOIN eofana."sessionsFormation" s ON s."idSession"   = i."idSession"
            JOIN eofana."formations"        f ON f."idFormation" = s."idFormation"
            WHERE f."idCentre" = :idCentre
              AND i."statut"   = 'valide'
            """, nativeQuery = true)
    Long sumCommissionValideParCentre(@Param("idCentre") Long idCentre);

    /**
     * Revenu net total dû au formateur sur les inscriptions VALIDE.
     */
    @Query(value = """
            SELECT COALESCE(SUM(i."montantFormateur"), 0)
            FROM eofana."inscriptions" i
            JOIN eofana."sessionsFormation" s ON s."idSession"   = i."idSession"
            JOIN eofana."formations"        f ON f."idFormation" = s."idFormation"
            WHERE f."idCentre" = :idCentre
              AND i."statut"   = 'valide'
            """, nativeQuery = true)
    Long sumMontantFormateurValideParCentre(@Param("idCentre") Long idCentre);

    /**
     * Montant net non encore reversé au formateur.
     */
    @Query(value = """
            SELECT COALESCE(SUM(i."montantFormateur"), 0)
            FROM eofana."inscriptions" i
            JOIN eofana."sessionsFormation" s ON s."idSession"   = i."idSession"
            JOIN eofana."formations"        f ON f."idFormation" = s."idFormation"
            WHERE f."idCentre" = :idCentre
              AND i."statut"   = 'valide'
              AND NOT EXISTS (
                  SELECT 1
                  FROM eofana."virements" v
                  WHERE v."idCentre"    = :idCentre
                    AND v."statut"      = 'effectue'
                    AND v."periodeDebut" IS NOT NULL
                    AND v."periodeFin"   IS NOT NULL
                    AND i."createdAt"::DATE
                        BETWEEN v."periodeDebut" AND v."periodeFin"
              )
            """, nativeQuery = true)
    Long sumMontantNonVireParCentre(@Param("idCentre") Long idCentre);
}
