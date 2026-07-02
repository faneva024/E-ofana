package com.eofana.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/inscriptions")
@CrossOrigin(origins = {
        "http://localhost:5173",
        "http://localhost:5174"
})
public class InscriptionController {

    private final JdbcTemplate jdbcTemplate;

    public InscriptionController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @PostMapping("/inscrire")
    @Transactional
    public ResponseEntity<?> inscrire(@RequestBody InscriptionRequest request) {
        if (request.getIdUser() == null) {
            return ResponseEntity.badRequest().body(Map.of(
                    "message", "L'utilisateur est obligatoire"
            ));
        }

        if (request.getIdFormation() == null) {
            return ResponseEntity.badRequest().body(Map.of(
                    "message", "La formation est obligatoire"
            ));
        }

        Long idUser = request.getIdUser();
        Long idFormation = request.getIdFormation();

        Integer userExiste = jdbcTemplate.queryForObject(
                """
                SELECT COUNT(*)
                FROM eofana.utilisateurs
                WHERE "idUser" = ?
                """,
                Integer.class,
                idUser
        );

        if (userExiste == null || userExiste == 0) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "message", "Utilisateur introuvable"
            ));
        }

        Integer formationExiste = jdbcTemplate.queryForObject(
                """
                SELECT COUNT(*)
                FROM eofana.formations
                WHERE "idFormation" = ?
                """,
                Integer.class,
                idFormation
        );

        if (formationExiste == null || formationExiste == 0) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "message", "Formation introuvable"
            ));
        }

        Long idSession = trouverOuCreerSession(idFormation);

        Integer dejaInscrit = jdbcTemplate.queryForObject(
                """
                SELECT COUNT(*)
                FROM eofana.inscriptions
                WHERE "idUser" = ?
                AND "idSession" = ?
                """,
                Integer.class,
                idUser,
                idSession
        );

        if (dejaInscrit != null && dejaInscrit > 0) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(
                    "message", "Vous êtes déjà inscrit à cette formation"
            ));
        }

        Integer placesRestantes = jdbcTemplate.queryForObject(
                """
                SELECT "placesRestantes"
                FROM eofana."sessionsFormation"
                WHERE "idSession" = ?
                """,
                Integer.class,
                idSession
        );

        if (placesRestantes == null || placesRestantes <= 0) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(
                    "message", "Il n'y a plus de places disponibles"
            ));
        }

        String numeroRecu = "RECU-" + System.currentTimeMillis();

        Long idInscription = jdbcTemplate.queryForObject(
                """
                INSERT INTO eofana.inscriptions (
                    "idUser",
                    "idSession",
                    "numeroRecu"
                )
                VALUES (?, ?, ?)
                RETURNING "idInscription"
                """,
                Long.class,
                idUser,
                idSession,
                numeroRecu
        );

        jdbcTemplate.update(
                """
                UPDATE eofana."sessionsFormation"
                SET "placesRestantes" = "placesRestantes" - 1
                WHERE "idSession" = ?
                AND "placesRestantes" > 0
                """,
                idSession
        );

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Inscription réussie");
        response.put("idInscription", idInscription);
        response.put("idUser", idUser);
        response.put("idFormation", idFormation);
        response.put("idSession", idSession);
        response.put("numeroRecu", numeroRecu);

        return ResponseEntity.ok(response);
    }

    private Long trouverOuCreerSession(Long idFormation) {
        List<Map<String, Object>> sessions = jdbcTemplate.queryForList(
                """
                SELECT "idSession"
                FROM eofana."sessionsFormation"
                WHERE "idFormation" = ?
                AND statut = 'ouvert'
                AND "placesRestantes" > 0
                ORDER BY "dateDebut" ASC
                LIMIT 1
                """,
                idFormation
        );

        if (!sessions.isEmpty()) {
            return ((Number) sessions.get(0).get("idSession")).longValue();
        }

        return jdbcTemplate.queryForObject(
                """
                INSERT INTO eofana."sessionsFormation" (
                    "idFormation",
                    "dateDebut",
                    "dateFin",
                    "dateLimiteInscription",
                    "placesTotal",
                    "placesRestantes"
                )
                VALUES (
                    ?,
                    CURRENT_DATE + INTERVAL '7 days',
                    CURRENT_DATE + INTERVAL '37 days',
                    CURRENT_DATE + INTERVAL '6 days',
                    20,
                    20
                )
                RETURNING "idSession"
                """,
                Long.class,
                idFormation
        );
    }

    public static class InscriptionRequest {
        private Long idUser;
        private Long idFormation;

        public Long getIdUser() {
            return idUser;
        }

        public void setIdUser(Long idUser) {
            this.idUser = idUser;
        }

        public Long getIdFormation() {
            return idFormation;
        }

        public void setIdFormation(Long idFormation) {
            this.idFormation = idFormation;
        }
    }
}