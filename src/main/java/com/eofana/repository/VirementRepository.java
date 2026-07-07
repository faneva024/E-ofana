package com.eofana.repository;

import com.eofana.entity.Virement;
import com.eofana.enums.StatutVirement;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Dépôt Spring Data JPA pour Virement (T-B-105).
 */
public interface VirementRepository extends JpaRepository<Virement, Long> {

    /** Historique des virements d'un centre, plus récent en premier (pagination T-B-110). */
    Page<Virement> findByCentreIdCentreOrderByCreatedAtDesc(Long idCentre, Pageable pageable);

    List<Virement> findByCentreIdCentreOrderByCreatedAtDesc(Long idCentre);

    /** Virements déjà effectués pour un centre — sert à ne pas recompter deux fois les inscriptions déjà virées. */
    List<Virement> findByCentreIdCentreAndStatut(Long idCentre, StatutVirement statut);
}
