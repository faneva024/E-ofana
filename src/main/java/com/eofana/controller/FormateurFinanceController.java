package com.eofana.controller;

import com.eofana.dto.TrainerFinanceSummaryResponse;
import com.eofana.dto.TrainerTransferResponse;
import com.eofana.service.TrainerFinanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller handling financial views for connected trainers (T-B-110).
 * All endpoints require valid JWT authentication.
 */
@RestController
@RequestMapping("/api/v1/formateurs/finances")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:5174"})
public class FormateurFinanceController {

    private final TrainerFinanceService trainerFinanceService;

    /**
     * GET /api/v1/formateurs/finances
     * Returns the global financial statement (Gross, Commission, Net, Next Payout).
     */
    @GetMapping
    public ResponseEntity<TrainerFinanceSummaryResponse> getFinanceSummary(
            @AuthenticationPrincipal UserDetails userDetails) {
        
        // userDetails.getUsername() contient l'email extrait du token JWT
        TrainerFinanceSummaryResponse summary = trainerFinanceService.getFinanceSummaryByEmail(userDetails.getUsername());
        return ResponseEntity.ok(summary);
    }

    /**
     * GET /api/v1/formateurs/finances/virements
     * Returns a paginated list of mobile money transfers, sorted by date descending by default.
     */
    @GetMapping("/virements")
    public ResponseEntity<Page<TrainerTransferResponse>> getTransferHistory(
            @AuthenticationPrincipal UserDetails userDetails,
            @PageableDefault(size = 10, sort = "dateVirement", direction = Sort.Direction.DESC) Pageable pageable) {
        
        Page<TrainerTransferResponse> history = trainerFinanceService.getTransferHistoryByEmail(userDetails.getUsername(), pageable);
        return ResponseEntity.ok(history);
    }
}