package com.eofana.controller;

import com.eofana.entity.Paiement;
import com.eofana.repository.PaiementRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Consultation de l'historique des paiements d'une inscription.
 *
 * La CRÉATION d'un paiement se fait désormais via
 * POST /api/v1/inscriptions/{id}/confirmer-paiement
 * (InscriptionController, T-B-011), pas ici. L'ancienne route
 * POST /api/v1/paiements/payer a été retirée : elle remettait
 * `remise`/`commission` à zéro sur l'inscription et interdisait tout
 * second paiement, ce qui rendait impossible le schéma
 * "acompte + solde" requis pour une réservation.
 */
@RestController
@RequestMapping("/api/v1/paiements")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:5174"})
public class PaiementController {

    private final PaiementRepository paiementRepository;

    @GetMapping("/inscription/{idInscription}")
    @Transactional(readOnly = true)
    public List<PaiementResponse> getPaiementsByInscription(@PathVariable Long idInscription) {
        return paiementRepository.findByInscription_IdInscriptionOrderByCreatedAtAsc(idInscription)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private PaiementResponse toResponse(Paiement paiement) {
        return new PaiementResponse(
                paiement.getIdPaiement(),
                paiement.getInscription().getIdInscription(),
                paiement.getMontant(),
                paiement.getOperateur().name(),
                paiement.getNumeroTransaction(),
                paiement.getStatut().name(),
                paiement.getCreatedAt()
        );
    }

    public record PaiementResponse(
            Long idPaiement,
            Long idInscription,
            Long montant,
            String operateur,
            String numeroTransaction,
            String statut,
            java.time.OffsetDateTime createdAt
    ) {
    }
}
