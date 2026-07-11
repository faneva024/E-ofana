package com.eofana.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/apprenants")
@CrossOrigin(origins = {
        "http://localhost:5173",
        "http://localhost:5174"
})
public class ApprenantEspaceController {

    private final JdbcTemplate jdbcTemplate;

    public ApprenantEspaceController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @GetMapping("/{idUser}/inscriptions")
    public ResponseEntity<?> getInscriptions(@PathVariable Long idUser) {
        List<Map<String, Object>> inscriptions = jdbcTemplate.queryForList(
                """
                SELECT
                    i."idInscription" AS "idInscription",
                    i."idUser" AS "idUser",
                    i."idSession" AS "idSession",
                    i."typeInsc"::text AS "typeInsc",
                    i.statut::text AS "statut",
                    i."montantPaye" AS "montantPaye",
                    i.remise AS "remise",
                    i.commission AS "commission",
                    i."montantFormateur" AS "montantFormateur",
                    i.operateur::text AS "operateur",
                    i."transactionId" AS "transactionId",
                    i."numeroRecu" AS "numeroRecu",
                    i."createdAt" AS "createdAt",

                    s."idFormation" AS "idFormation",
                    s."dateDebut" AS "dateDebut",
                    s."dateFin" AS "dateFin",
                    s."placesRestantes" AS "placesRestantes",

                    f.titre AS "formation",
                    f.description AS "description",
                    f.duree AS "duree",
                    f.lieu AS "lieu",
                    COALESCE(f."prixRemise", f.prix, 0) AS "prix",

                    c.nom AS "centre",
                    c.ville AS "ville",

                    cat.nom AS "categorie"
                FROM eofana.inscriptions i
                JOIN eofana."sessionsFormation" s ON s."idSession" = i."idSession"
                JOIN eofana.formations f ON f."idFormation" = s."idFormation"
                LEFT JOIN eofana.centres c ON c."idCentre" = f."idCentre"
                LEFT JOIN eofana.categories cat ON cat."idCategorie" = f."idCategorie"
                WHERE i."idUser" = ?
                ORDER BY i."createdAt" DESC
                """,
                idUser
        );

        List<Map<String, Object>> response = inscriptions.stream().map(row -> {
            Map<String, Object> item = new HashMap<>();

            item.put("id", row.get("idInscription"));
            item.put("idInscription", row.get("idInscription"));
            item.put("idUser", row.get("idUser"));
            item.put("idSession", row.get("idSession"));
            item.put("idFormation", row.get("idFormation"));

            item.put("formation", row.get("formation"));
            item.put("titre", row.get("formation"));
            item.put("description", row.get("description"));
            item.put("categorie", row.get("categorie"));
            item.put("ecole", row.get("centre"));
            item.put("centre", row.get("centre"));
            item.put("ville", row.get("ville"));
            item.put("lieu", row.get("lieu"));
            item.put("duree", row.get("duree"));

            item.put("date", row.get("createdAt"));
            item.put("dateDebut", row.get("dateDebut"));
            item.put("dateFin", row.get("dateFin"));

            item.put("statut", normaliserStatutInscription(row.get("statut")));
            item.put("statutOriginal", row.get("statut"));
            item.put("typeInsc", row.get("typeInsc"));

            item.put("montant", row.get("montantPaye"));
            item.put("montantPaye", row.get("montantPaye"));
            item.put("prix", row.get("prix"));
            item.put("remise", row.get("remise"));
            item.put("commission", row.get("commission"));
            item.put("montantFormateur", row.get("montantFormateur"));

            item.put("operateur", row.get("operateur"));
            item.put("transactionId", row.get("transactionId"));
            item.put("numeroRecu", row.get("numeroRecu"));

            item.put("progression", 0);
            item.put("placesRestantes", row.get("placesRestantes"));

            return item;
        }).toList();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{idUser}/recus")
    public ResponseEntity<?> getRecus(@PathVariable Long idUser) {
        List<Map<String, Object>> recus = jdbcTemplate.queryForList(
                """
                SELECT
                    i."idInscription" AS "idInscription",
                    i."numeroRecu" AS "numeroRecu",
                    i."createdAt" AS "dateInscription",

                    p."idPaiement" AS "idPaiement",
                    p.montant AS "montant",
                    p.operateur::text AS "operateur",
                    p."numeroTransaction" AS "numeroTransaction",
                    p.statut::text AS "statutPaiement",
                    p."createdAt" AS "datePaiement",

                    f."idFormation" AS "idFormation",
                    f.titre AS "formation",

                    c.nom AS "centre"
                FROM eofana.inscriptions i
                JOIN eofana."sessionsFormation" s ON s."idSession" = i."idSession"
                JOIN eofana.formations f ON f."idFormation" = s."idFormation"
                LEFT JOIN eofana.centres c ON c."idCentre" = f."idCentre"
                LEFT JOIN eofana.paiements p ON p."idInscription" = i."idInscription"
                WHERE i."idUser" = ?
                ORDER BY COALESCE(p."createdAt", i."createdAt") DESC
                """,
                idUser
        );

        List<Map<String, Object>> response = recus.stream().map(row -> {
            Map<String, Object> item = new HashMap<>();

            item.put("id", row.get("idPaiement") != null ? row.get("idPaiement") : row.get("idInscription"));
            item.put("idPaiement", row.get("idPaiement"));
            item.put("idInscription", row.get("idInscription"));
            item.put("idFormation", row.get("idFormation"));

            item.put("reference", row.get("numeroRecu"));
            item.put("numeroRecu", row.get("numeroRecu"));
            item.put("numeroTransaction", row.get("numeroTransaction"));

            item.put("formation", row.get("formation"));
            item.put("centre", row.get("centre"));

            item.put("date", row.get("datePaiement") != null ? row.get("datePaiement") : row.get("dateInscription"));
            item.put("montant", row.get("montant"));
            item.put("methode", "Mobile Money");
            item.put("operateur", row.get("operateur"));

            item.put("statut", normaliserStatutPaiement(row.get("statutPaiement")));
            item.put("statutOriginal", row.get("statutPaiement"));

            item.put("urlPdf", "/api/v1/recus/" + row.get("idInscription") + "/pdf");

            return item;
        }).toList();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{idUser}/stats")
    public ResponseEntity<?> getStats(@PathVariable Long idUser) {
        Integer totalInscriptions = jdbcTemplate.queryForObject(
                """
                SELECT COUNT(*)
                FROM eofana.inscriptions
                WHERE "idUser" = ?
                """,
                Integer.class,
                idUser
        );

        Integer inscriptionsTerminees = jdbcTemplate.queryForObject(
                """
                SELECT COUNT(*)
                FROM eofana.inscriptions
                WHERE "idUser" = ?
                AND statut::text IN ('termine', 'TERMINE')
                """,
                Integer.class,
                idUser
        );

        Integer inscriptionsEnCours = jdbcTemplate.queryForObject(
                """
                SELECT COUNT(*)
                FROM eofana.inscriptions
                WHERE "idUser" = ?
                AND statut::text NOT IN ('termine', 'TERMINE', 'annule', 'ANNULE')
                """,
                Integer.class,
                idUser
        );

        Long totalPaye = jdbcTemplate.queryForObject(
                """
                SELECT COALESCE(SUM(p.montant), 0)
                FROM eofana.paiements p
                JOIN eofana.inscriptions i ON i."idInscription" = p."idInscription"
                WHERE i."idUser" = ?
                """,
                Long.class,
                idUser
        );

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalInscriptions", totalInscriptions != null ? totalInscriptions : 0);
        stats.put("formationsEnCours", inscriptionsEnCours != null ? inscriptionsEnCours : 0);
        stats.put("formationsTerminees", inscriptionsTerminees != null ? inscriptionsTerminees : 0);
        stats.put("certificats", 0);
        stats.put("tempsFormation", "0h");
        stats.put("totalPaye", totalPaye != null ? totalPaye : 0);

        return ResponseEntity.ok(stats);
    }

    @GetMapping("/{idUser}/certificats")
    public ResponseEntity<?> getCertificats(@PathVariable Long idUser) {
        return ResponseEntity.ok(List.of());
    }

    @GetMapping("/{idUser}/dashboard")
    public ResponseEntity<?> getDashboard(@PathVariable Long idUser) {
        Map<String, Object> dashboard = new HashMap<>();

        dashboard.put("inscriptions", getInscriptionsData(idUser));
        dashboard.put("recus", getRecusData(idUser));
        dashboard.put("stats", getStatsData(idUser));
        dashboard.put("certificats", List.of());

        return ResponseEntity.ok(dashboard);
    }

    private List<Map<String, Object>> getInscriptionsData(Long idUser) {
        ResponseEntity<?> response = getInscriptions(idUser);
        Object body = response.getBody();

        if (body instanceof List<?>) {
            return (List<Map<String, Object>>) body;
        }

        return List.of();
    }

    private List<Map<String, Object>> getRecusData(Long idUser) {
        ResponseEntity<?> response = getRecus(idUser);
        Object body = response.getBody();

        if (body instanceof List<?>) {
            return (List<Map<String, Object>>) body;
        }

        return List.of();
    }

    private Map<String, Object> getStatsData(Long idUser) {
        ResponseEntity<?> response = getStats(idUser);
        Object body = response.getBody();

        if (body instanceof Map<?, ?>) {
            return (Map<String, Object>) body;
        }

        return Map.of();
    }

    private String normaliserStatutInscription(Object statut) {
        if (statut == null) {
            return "enAttente";
        }

        String value = statut.toString().toLowerCase();

        if (value.contains("valide") || value.contains("inscrit")) {
            return "inscrit";
        }

        if (value.contains("termine")) {
            return "termine";
        }

        if (value.contains("annule")) {
            return "annule";
        }

        if (value.contains("reservation") || value.contains("reserve")) {
            return "reserve";
        }

        return "enAttente";
    }

    private String normaliserStatutPaiement(Object statut) {
        if (statut == null) {
            return "enAttente";
        }

        String value = statut.toString().toLowerCase();

        if (value.contains("confirme") || value.contains("paye") || value.contains("payé")) {
            return "paye";
        }

        if (value.contains("echec") || value.contains("échoué")) {
            return "echec";
        }

        return "enAttente";
    }
}