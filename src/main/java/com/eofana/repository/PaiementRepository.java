package com.eofana.repository;

import com.eofana.entity.Paiement;
import com.eofana.enums.StatutPaiement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PaiementRepository extends JpaRepository<Paiement, Long> {

    /**
     * Tous les paiements  liés à une inscription donnée.
     */
    List<Paiement> findByInscription_IdInscriptionOrderByCreatedAtAsc(Long idInscription);

    /**
     * Paiement confirmé d'une inscription (un seul attendu).
     */
    Optional<Paiement> findFirstByInscription_IdInscriptionAndStatut(
            Long idInscription, StatutPaiement statut);

    // ─── Détection des doublons ────────────────────────────────────────────────

    /**
     * Vérifie si un numéro de transaction Mobile Money a déjà été enregistré.
     */
    boolean existsByNumeroTransaction(String numeroTransaction);

    /**
     * Retrouve un paiement par son numéro de transaction.
     */
    Optional<Paiement> findByNumeroTransaction(String numeroTransaction);

    // ─── Administration ────────────────────────────────────────────────────────
    /**
     * Tous les paiements d'un statut donné, pour le suivi administrateur.
     */
    List<Paiement> findByStatutOrderByCreatedAtAsc(StatutPaiement statut);

    /**
     * Compte les paiements confirmés d'une inscription.
     */
    long countByInscription_IdInscriptionAndStatut(Long idInscription, StatutPaiement statut);
}
