package com.eofana.repository;

import com.eofana.entity.Virement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Dépôt Spring Data JPA pour l'entité Virement.
 */
public interface VirementRepository extends JpaRepository<Virement, Long> {

    /**
     * Historique des virements d'un centre, du plus récent au plus ancien.
     */
    List<Virement> findByCentreIdCentreOrderByCreatedAtDesc(Long idCentre);
}