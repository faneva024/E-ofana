package com.eofana.repository;

import com.eofana.entity.DemandeModificationCentre;
import com.eofana.enums.StatutModification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Dépôt Spring Data JPA pour DemandeModificationCentre (T-B-103).
 */
public interface DemandeModificationCentreRepository extends JpaRepository<DemandeModificationCentre, Long> {

    /** Demandes d'un centre filtrées par statut (ex. EN_ATTENTE côté admin). */
    List<DemandeModificationCentre> findByCentreIdCentreAndStatut(Long idCentre, StatutModification statut);

    /** Historique complet des demandes d'un centre, plus récentes en premier. */
    List<DemandeModificationCentre> findByCentreIdCentreOrderByCreatedAtDesc(Long idCentre);
}
