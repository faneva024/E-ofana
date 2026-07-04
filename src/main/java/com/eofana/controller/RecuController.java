package com.eofana.controller;

import com.eofana.service.ReceiptGenerationService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.NoSuchElementException;

/**
 * Route historique de téléchargement du reçu PDF, conservée pour
 * compatibilité avec le frontend existant (api/recu.js).
 *
 * La génération du PDF ne vit plus ici : elle est déléguée à
 * ReceiptGenerationService (T-B-014), partagée avec la route
 * officielle GET /api/v1/inscriptions/{id}/recu (InscriptionController).
 * Avant cette correction, le PDF était généré deux fois (logique
 * dupliquée) et via des requêtes SQL brutes.
 */
@RestController
@RequestMapping("/api/v1/recus")
@CrossOrigin(origins = {
        "http://localhost:5173",
        "http://localhost:5174"
})
public class RecuController {

    private final ReceiptGenerationService receiptGenerationService;

    public RecuController(ReceiptGenerationService receiptGenerationService) {
        this.receiptGenerationService = receiptGenerationService;
    }

    @GetMapping("/{idInscription}/pdf")
    public ResponseEntity<?> telechargerRecuPdf(@PathVariable Long idInscription) {
        try {
            byte[] pdf = receiptGenerationService.generateReceipt(idInscription);

            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_PDF)
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"recu-inscription-" + idInscription + ".pdf\"")
                    .body(pdf);
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "message", "Inscription introuvable"
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "message", "Erreur lors de la génération du reçu PDF"
            ));
        }
    }
}
