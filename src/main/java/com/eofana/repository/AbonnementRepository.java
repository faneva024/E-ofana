package com.eofana.repository;

import com.eofana.entity.Abonnement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Dépôt Spring Data JPA pour Abonnement (historique des souscriptions, T-B-101).
 */
public interface AbonnementRepository extends JpaRepository<Abonnement, Long> {

    List<Abonnement> findByCentreIdCentreOrderByDateDebutDesc(Long idCentre);

    /** Abonnement actuellement actif d'un centre (dateFin = null). */
    Optional<Abonnement> findByCentreIdCentreAndDateFinIsNull(Long idCentre);
}
