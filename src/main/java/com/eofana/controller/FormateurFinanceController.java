package com.eofana.controller;

import com.eofana.entity.Centre;
import com.eofana.entity.Virement;
import com.eofana.repository.VirementRepository;
import com.eofana.security.FormateurContextResolver;
import com.eofana.service.FinanceCalculationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * T-B-110 : vue financière du formateur (revenus, commissions,
 * virements). Les calculs de commission réutilisent exactement la
 * même logique que le module apprenant (cf. FinanceCalculationService).
 */
@RestController
@RequestMapping("/api/v1/formateurs/finances")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:5174"})
public class FormateurFinanceController {

    private final FormateurContextResolver formateurContextResolver;
    private final FinanceCalculationService financeCalculationService;
    private final VirementRepository virementRepository;

    @GetMapping
    @Transactional(readOnly = true)
    public ResponseEntity<?> getFinanceSummary(
            @RequestHeader(value = "Authorization", required = false) String authorization) {

        Centre centre;
        try {
            centre = formateurContextResolver.resoudreCentre(authorization);
        } catch (FormateurContextResolver.AccesNonAutoriseException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", e.getMessage()));
        }

        long revenusBruts = financeCalculationService.calculateGrossRevenue(centre);
        long commission = financeCalculationService.calculateCommissionCollected(centre);
        long revenusNets = financeCalculationService.calculateNetRevenue(centre);
        FinanceCalculationService.ProchainVirement prochainVirement =
                financeCalculationService.calculateNextTransfer(centre);

        Map<String, Object> reponse = new HashMap<>();
        reponse.put("revenusBruts", revenusBruts);
        reponse.put("commissionPrelevee", commission);
        reponse.put("revenusNets", revenusNets);
        reponse.put("tauxCommission", centre.getTauxCommission());
        reponse.put("prochainVirementDate", prochainVirement.date());
        reponse.put("prochainVirementMontantEstime", prochainVirement.montantEstime());

        return ResponseEntity.ok(reponse);
    }

    /** Historique paginé des virements, trié par date décroissante. */
    @GetMapping("/virements")
    @Transactional(readOnly = true)
    public ResponseEntity<?> getTransferHistory(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int taille) {

        Centre centre;
        try {
            centre = formateurContextResolver.resoudreCentre(authorization);
        } catch (FormateurContextResolver.AccesNonAutoriseException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", e.getMessage()));
        }

        Page<Virement> virements = virementRepository.findByCentreIdCentreOrderByCreatedAtDesc(
                centre.getIdCentre(), PageRequest.of(page, taille));

        Page<Map<String, Object>> reponse = virements.map(v -> Map.<String, Object>of(
                "idVirement", v.getIdVirement(),
                "date", v.getDateVirement(),
                "montantBrut", v.getMontantBrut(),
                "commission", v.getCommission(),
                "montantNet", v.getMontantNet(),
                "operateur", v.getOperateur().name(),
                "statut", v.getStatut().name(),
                // Lien vers le justificatif PDF, généré par AutomaticTransferService#generateTransferReceipt
                "justificatifUrl", "/api/v1/formateurs/finances/virements/" + v.getIdVirement() + "/pdf"
        ));

        return ResponseEntity.ok(reponse);
    }
}
