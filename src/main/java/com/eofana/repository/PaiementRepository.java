package com.eofana.repository;

import com.eofana.entity.Paiement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Dépôt Spring Data JPA pour l'entité Paiement.
 *
 * Une Inscription peut avoir plusieurs Paiement (acompte + solde
 * pour une réservation) : findByInscription_IdInscription() renvoie
 * donc une liste, pas un Optional unique.
 */
public interface PaiementRepository extends JpaRepository<Paiement, Long> {

    List<Paiement> findByInscription_IdInscriptionOrderByCreatedAtAsc(Long idInscription);

    Optional<Paiement> findByNumeroTransaction(String numeroTransaction);
}
