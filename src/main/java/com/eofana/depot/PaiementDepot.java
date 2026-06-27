package com.eofana.depot;

import com.eofana.entite.Paiement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

/**
 * Dépôt Paiement — table "paiements" de eofanaDb v3.0
 */
@Repository
public interface PaiementDepot extends JpaRepository<Paiement, Long> {

    /** Tous les paiements d'une inscription (peut être 2 pour une réservation). */
    List<Paiement> findByInscription_IdInscriptionOrderByCreatedAtAsc(Long idInscription);

    /** Vérifier l'unicité d'un numéro de transaction. */
    boolean existsByNumeroTransaction(String numeroTransaction);
}
