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
@RequestMapping("/api/v1/paiements")
@CrossOrigin(origins = {
        "http://localhost:5173",
        "http://localhost:5174"
})
public class PaiementController {

    private final JdbcTemplate jdbcTemplate;

    public PaiementController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @PostMapping("/payer")
    @Transactional
    public ResponseEntity<?> payer(@RequestBody PaiementRequest request) {
        if (request.getIdInscription() == null) {
            return ResponseEntity.badRequest().body(Map.of(
                    "message", "L'inscription est obligatoire"
            ));
        }

        Long idInscription = request.getIdInscription();

        List<Map<String, Object>> inscriptions = jdbcTemplate.queryForList(
                """
                SELECT
                    i."idInscription",
                    i."idUser",
                    i."idSession",
                    i."numeroRecu",
                    s."idFormation",
                    f.titre,
                    COALESCE(f."prixRemise", f.prix, 0) AS montant
                FROM eofana.inscriptions i
                JOIN eofana."sessionsFormation" s ON s."idSession" = i."idSession"
                JOIN eofana.formations f ON f."idFormation" = s."idFormation"
                WHERE i."idInscription" = ?
                """,
                idInscription
        );

        if (inscriptions.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "message", "Inscription introuvable"
            ));
        }

        Map<String, Object> inscription = inscriptions.get(0);

        Long montantFormation = ((Number) inscription.get("montant")).longValue();

        Long montantPaye = request.getMontant() != null && request.getMontant() > 0
                ? request.getMontant()
                : montantFormation;

        String operateur = convertirOperateur(request.getOperateur());

        String numeroTransaction = request.getNumeroTransaction() != null
                && !request.getNumeroTransaction().trim().isEmpty()
                ? request.getNumeroTransaction().trim()
                : "TXN-" + System.currentTimeMillis();

        Integer paiementExiste = jdbcTemplate.queryForObject(
                """
                SELECT COUNT(*)
                FROM eofana.paiements
                WHERE "idInscription" = ?
                """,
                Integer.class,
                idInscription
        );

        if (paiementExiste != null && paiementExiste > 0) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(
                    "message", "Cette inscription a déjà un paiement enregistré"
            ));
        }

        Long idPaiement = jdbcTemplate.queryForObject(
                """
                INSERT INTO eofana.paiements (
                    "idInscription",
                    montant,
                    operateur,
                    "numeroTransaction"
                )
                VALUES (
                    ?,
                    ?,
                    ?::eofana."operateurMm",
                    ?
                )
                RETURNING "idPaiement"
                """,
                Long.class,
                idInscription,
                montantPaye,
                operateur,
                numeroTransaction
        );

        jdbcTemplate.update(
                """
                UPDATE eofana.inscriptions
                SET
                    "montantPaye" = ?,
                    remise = 0,
                    commission = 0,
                    "montantFormateur" = ?,
                    operateur = ?::eofana."operateurMm",
                    "transactionId" = ?,
                    "updatedAt" = NOW()
                WHERE "idInscription" = ?
                """,
                montantPaye,
                montantPaye,
                operateur,
                numeroTransaction,
                idInscription
        );

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Paiement enregistré avec succès");
        response.put("idPaiement", idPaiement);
        response.put("idInscription", idInscription);
        response.put("montantPaye", montantPaye);
        response.put("operateur", operateur);
        response.put("numeroTransaction", numeroTransaction);

        return ResponseEntity.ok(response);
    }

    private String convertirOperateur(String operateur) {
        if (operateur == null || operateur.trim().isEmpty()) {
            return "mvola";
        }

        String valeur = operateur.trim().toLowerCase();

        if (valeur.contains("orange")) {
            return "orange";
        }

        if (valeur.contains("airtel")) {
            return "airtel";
        }

        if (valeur.contains("mvola")) {
            return "mvola";
        }

        return "mvola";
    }

    public static class PaiementRequest {
        private Long idInscription;
        private Long montant;
        private String operateur;
        private String numeroTransaction;

        public Long getIdInscription() {
            return idInscription;
        }

        public void setIdInscription(Long idInscription) {
            this.idInscription = idInscription;
        }

        public Long getMontant() {
            return montant;
        }

        public void setMontant(Long montant) {
            this.montant = montant;
        }

        public String getOperateur() {
            return operateur;
        }

        public void setOperateur(String operateur) {
            this.operateur = operateur;
        }

        public String getNumeroTransaction() {
            return numeroTransaction;
        }

        public void setNumeroTransaction(String numeroTransaction) {
            this.numeroTransaction = numeroTransaction;
        }
    }
}