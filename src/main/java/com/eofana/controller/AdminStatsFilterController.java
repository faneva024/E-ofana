package com.eofana.controller;

import com.eofana.enums.AbonnementType;
import com.eofana.security.AdminContextResolver;
import com.eofana.service.AdminStatsService;
import com.eofana.service.AdminStatsService.StatsFilterDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;

/**
 * T-B-210 : statistiques filtrables par centre, catégorie, période,
 * ville/région, type d'abonnement — tous les paramètres sont
 * optionnels et combinables (ex. catégorie + période), cf.
 * AdminStatsService#getFilteredStats.
 */
@RestController
@RequestMapping("/api/v1/admin/statistiques/filtrees")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:5174"})
public class AdminStatsFilterController {

    private final AdminStatsService adminStatsService;
    private final AdminContextResolver adminContextResolver;

    @GetMapping
    @Transactional(readOnly = true)
    public ResponseEntity<?> getFilteredStats(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestParam(required = false) Long idCentre,
            @RequestParam(required = false) Long idCategorie,
            @RequestParam(required = false) String ville,
            @RequestParam(required = false) AbonnementType abonnement,
            @RequestParam(required = false) LocalDate periodeDebut,
            @RequestParam(required = false) LocalDate periodeFin) {

        try {
            adminContextResolver.resoudreAdmin(authorization);
        } catch (AdminContextResolver.AccesNonAutoriseException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", e.getMessage()));
        }

        if (periodeDebut != null && periodeFin != null && periodeFin.isBefore(periodeDebut)) {
            return ResponseEntity.badRequest().body(Map.of("message", "periodeFin ne peut pas précéder periodeDebut"));
        }

        StatsFilterDto filtres = new StatsFilterDto(idCentre, idCategorie, ville, abonnement, periodeDebut, periodeFin);
        return ResponseEntity.ok(adminStatsService.getFilteredStats(filtres));
    }
}
