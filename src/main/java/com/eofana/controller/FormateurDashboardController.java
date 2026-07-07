package com.eofana.controller;

import com.eofana.entity.Centre;
import com.eofana.security.FormateurContextResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * T-B-109 : tableau de bord statistiques du formateur.
 *
 * ⚠ "Nombre total de visites" et "taux d'inscription" ne sont PAS
 * disponibles avec le schéma actuel (la table "visites" a été
 * retirée à la demande du client, cf. T-B-104). Plutôt que de les
 * renvoyer à zéro silencieusement — ce qui laisserait croire au
 * formateur qu'aucun apprenant n'a consulté sa formation — la réponse
 * documente explicitement leur indisponibilité.
 */
@RestController
@RequestMapping("/api/v1/formateurs/tableau-de-bord")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:5174"})
public class FormateurDashboardController {

    private final FormateurContextResolver formateurContextResolver;
    private final JdbcTemplate jdbcTemplate;

    @GetMapping
    @Transactional(readOnly = true)
    public ResponseEntity<?> getDashboardSummary(
            @RequestHeader(value = "Authorization", required = false) String authorization) {

        Centre centre;
        try {
            centre = formateurContextResolver.resoudreCentre(authorization);
        } catch (FormateurContextResolver.AccesNonAutoriseException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", e.getMessage()));
        }

        Long nbTotalInscrits = jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM eofana.inscriptions i
                JOIN eofana."sessionsFormation" s ON s."idSession" = i."idSession"
                JOIN eofana.formations f ON f."idFormation" = s."idFormation"
                WHERE f."idCentre" = ?
                  AND i.statut = 'valide'
                """, Long.class, centre.getIdCentre());

        Long chiffreAffairesNetMois = jdbcTemplate.queryForObject("""
                SELECT COALESCE(SUM(i."montantFormateur"), 0)
                FROM eofana.inscriptions i
                JOIN eofana."sessionsFormation" s ON s."idSession" = i."idSession"
                JOIN eofana.formations f ON f."idFormation" = s."idFormation"
                WHERE f."idCentre" = ?
                  AND i.statut = 'valide'
                  AND date_trunc('month', i."createdAt") = date_trunc('month', CURRENT_DATE)
                """, Long.class, centre.getIdCentre());

        Map<String, Object> reponse = new HashMap<>();
        reponse.put("nbTotalInscrits", nbTotalInscrits != null ? nbTotalInscrits : 0);
        reponse.put("chiffreAffairesNetMoisEnCours", chiffreAffairesNetMois != null ? chiffreAffairesNetMois : 0);
        reponse.put("nbTotalVisites", null);
        reponse.put("tauxInscription", null);
        reponse.put("indicateursIndisponibles", java.util.List.of("nbTotalVisites", "tauxInscription"));
        reponse.put("motifIndisponibilite",
                "La table de tracking des visites a été retirée du schéma de référence ; "
                        + "une décision produit est nécessaire avant de la réintroduire.");

        return ResponseEntity.ok(reponse);
    }

    /** Statistiques détaillées d'une formation du centre connecté. */
    @GetMapping("/formations/{id}")
    @Transactional(readOnly = true)
    public ResponseEntity<?> getCourseStats(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @PathVariable Long id) {

        Centre centre;
        try {
            centre = formateurContextResolver.resoudreCentre(authorization);
        } catch (FormateurContextResolver.AccesNonAutoriseException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", e.getMessage()));
        }

        Long idCentreProprietaire = jdbcTemplate.queryForObject(
                "SELECT \"idCentre\" FROM eofana.formations WHERE \"idFormation\" = ?",
                Long.class, id);

        if (idCentreProprietaire == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "Formation introuvable"));
        }

        if (!idCentreProprietaire.equals(centre.getIdCentre())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "Cette formation n'appartient pas à votre centre"));
        }

        Long nbInscrits = jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM eofana.inscriptions i
                JOIN eofana."sessionsFormation" s ON s."idSession" = i."idSession"
                WHERE s."idFormation" = ? AND i."typeInsc" = 'inscription'
                """, Long.class, id);

        Long nbReservations = jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM eofana.inscriptions i
                JOIN eofana."sessionsFormation" s ON s."idSession" = i."idSession"
                WHERE s."idFormation" = ? AND i."typeInsc" = 'reservation'
                """, Long.class, id);

        Integer placesRestantes = jdbcTemplate.queryForObject("""
                SELECT COALESCE(SUM(s."placesRestantes"), 0)
                FROM eofana."sessionsFormation" s
                WHERE s."idFormation" = ? AND s.statut = 'ouvert'
                """, Integer.class, id);

        Long revenusGeneres = jdbcTemplate.queryForObject("""
                SELECT COALESCE(SUM(i."montantPaye"), 0)
                FROM eofana.inscriptions i
                JOIN eofana."sessionsFormation" s ON s."idSession" = i."idSession"
                WHERE s."idFormation" = ? AND i.statut = 'valide'
                """, Long.class, id);

        Map<String, Object> reponse = new HashMap<>();
        reponse.put("idFormation", id);
        reponse.put("nbInscrits", nbInscrits != null ? nbInscrits : 0);
        reponse.put("nbReservations", nbReservations != null ? nbReservations : 0);
        reponse.put("placesRestantes", placesRestantes != null ? placesRestantes : 0);
        reponse.put("revenusGeneres", revenusGeneres != null ? revenusGeneres : 0);
        reponse.put("nbVisites", null);
        reponse.put("indicateursIndisponibles", java.util.List.of("nbVisites"));

        return ResponseEntity.ok(reponse);
    }
}
