package com.eofana.repository;

import com.eofana.entity.Virement;
import com.eofana.enums.StatutVirement;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * Dépôt Virement — table "virements" (créée en V9, FK idCentre)
 * Tâche T-B-113 — ROLPH (Semaine 2)
 */
@Repository
public interface VirementRepository extends JpaRepository<Virement, Long> {

    /** Historique trié par date décroissante pour un centre */
    Page<Virement> findByCentre_IdCentreOrderByDateVirementDesc(Long idCentre, Pageable pageable);

    /** Liste complète sans pagination */
    List<Virement> findByCentre_IdCentreOrderByDateVirementDesc(Long idCentre);

    /** Virements planifiés prêts à être exécutés */
    List<Virement> findByStatut(StatutVirement statut);

    /** Total reversé à un centre (virements effectués) */
    @Query("SELECT COALESCE(SUM(v.montantNet), 0) FROM Virement v " +
           "WHERE v.centre.idCentre = :idCentre AND v.statut = 'effectue'")
    Long totalReversePourCentre(@Param("idCentre") Long idCentre);

    /** Vérifier si un virement a déjà été effectué sur une période */
    @Query("SELECT COUNT(v) > 0 FROM Virement v " +
           "WHERE v.centre.idCentre = :idCentre " +
           "AND v.statut = com.eofana.enums.StatutVirement.effectue " +
           "AND v.periodeDebut <= :fin AND v.periodeFin >= :debut")
    boolean existeVirementSurPeriode(@Param("idCentre") Long idCentre,
                                      @Param("debut") LocalDate debut,
                                      @Param("fin") LocalDate fin);
}
