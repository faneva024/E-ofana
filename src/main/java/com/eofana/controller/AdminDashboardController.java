package com.eofana.controller;

import com.eofana.security.AdminContextResolver;
import com.eofana.service.AdminStatsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * T-B-209 : tableau de bord global de l'admin. Toute l'agrégation vit
 * dans AdminStatsService (T-B-212) — ce contrôleur ne fait
 * qu'authentifier l'admin et exposer le résultat en JSON.
 */
@RestController
@RequestMapping("/api/v1/admin/tableau-de-bord")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:5174"})
public class AdminDashboardController {

    private final AdminStatsService adminStatsService;
    private final AdminContextResolver adminContextResolver;

    @GetMapping
    @Transactional(readOnly = true)
    public ResponseEntity<?> getGlobalStats(
            @RequestHeader(value = "Authorization", required = false) String authorization) {

        try {
            adminContextResolver.resoudreAdmin(authorization);
        } catch (AdminContextResolver.AccesNonAutoriseException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", e.getMessage()));
        }

        return ResponseEntity.ok(adminStatsService.getGlobalStats());
    }
}
