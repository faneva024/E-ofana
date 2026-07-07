package com.eofana.controller;

import com.eofana.entity.Centre;
import com.eofana.security.FormateurContextResolver;
import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.Map;

/**
 * T-B-111 : liste des inscrits d'une formation, et son export PDF.
 * Accessible uniquement par le formateur propriétaire de la formation.
 */
@RestController
@RequestMapping("/api/v1/formations")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:5174"})
public class InscritsController {

    private final FormateurContextResolver formateurContextResolver;
    private final JdbcTemplate jdbcTemplate;

    @GetMapping("/{id}/inscrits")
    @Transactional(readOnly = true)
    public ResponseEntity<?> getEnrollmentsByCourse(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @PathVariable Long id,
            @RequestParam(required = false) Long idSession) {

        try {
            verifierProprietaire(authorization, id);
        } catch (FormateurContextResolver.AccesNonAutoriseException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", e.getMessage()));
        } catch (AccesInterditException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", e.getMessage()));
        } catch (IntrouvableException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", e.getMessage()));
        }

        List<Map<String, Object>> inscrits = jdbcTemplate.queryForList(
                requeteInscrits(idSession != null),
                idSession != null ? new Object[]{id, idSession} : new Object[]{id}
        );

        List<Map<String, Object>> reponse = inscrits.stream()
                .map(row -> Map.<String, Object>of(
                        "nom", row.get("nom"),
                        "prenom", row.get("prenom"),
                        "dateInscription", row.get("dateInscription"),
                        // VALIDE = payé, EN_ATTENTE (typeInsc = reservation) = réservé
                        "statutPaiement", "valide".equals(row.get("statut")) ? "VALIDE" : "EN_ATTENTE"
                ))
                .toList();

        return ResponseEntity.ok(reponse);
    }

    @GetMapping("/{id}/inscrits/export-pdf")
    public ResponseEntity<?> exportEnrollmentsToPdf(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @PathVariable Long id) {

        String titreFormation;
        try {
            titreFormation = verifierProprietaire(authorization, id);
        } catch (FormateurContextResolver.AccesNonAutoriseException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", e.getMessage()));
        } catch (AccesInterditException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", e.getMessage()));
        } catch (IntrouvableException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", e.getMessage()));
        }

        List<Map<String, Object>> inscrits = jdbcTemplate.queryForList(requeteInscrits(false), id);

        try {
            byte[] pdf = genererPdf(titreFormation, inscrits);
            String nomFichier = "inscrits-formation-" + id + ".pdf";

            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_PDF)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + nomFichier + "\"")
                    .body(pdf);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Erreur lors de la génération du PDF"));
        }
    }

    private String requeteInscrits(boolean filtrerSession) {
        String base = """
                SELECT u.nom AS nom, u.prenom AS prenom,
                       i."createdAt" AS "dateInscription", i.statut AS statut
                FROM eofana.inscriptions i
                JOIN eofana.utilisateurs u ON u."idUser" = i."idUser"
                JOIN eofana."sessionsFormation" s ON s."idSession" = i."idSession"
                WHERE s."idFormation" = ?
                """;
        return filtrerSession ? base + " AND i.\"idSession\" = ? ORDER BY i.\"createdAt\" DESC"
                : base + " ORDER BY i.\"createdAt\" DESC";
    }

    /** @return le titre de la formation si tout est en règle */
    private String verifierProprietaire(String authorization, Long idFormation) {
        Centre centre = formateurContextResolver.resoudreCentre(authorization);

        List<Map<String, Object>> result = jdbcTemplate.queryForList(
                "SELECT \"idCentre\", titre FROM eofana.formations WHERE \"idFormation\" = ?",
                idFormation);

        if (result.isEmpty()) {
            throw new IntrouvableException("Formation introuvable");
        }

        Long idCentreProprietaire = ((Number) result.get(0).get("idCentre")).longValue();
        if (!idCentreProprietaire.equals(centre.getIdCentre())) {
            throw new AccesInterditException("Cette formation n'appartient pas à votre centre");
        }

        return (String) result.get(0).get("titre");
    }

    private byte[] genererPdf(String titreFormation, List<Map<String, Object>> inscrits) throws Exception {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4);
        PdfWriter.getInstance(document, outputStream);
        document.open();

        Font titreFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16);
        Font sousTitreFont = FontFactory.getFont(FontFactory.HELVETICA, 12);
        Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10);
        Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 10);

        Paragraph titre = new Paragraph("LISTE DES INSCRITS", titreFont);
        titre.setAlignment(Element.ALIGN_CENTER);
        titre.setSpacingAfter(5);
        document.add(titre);

        Paragraph sousTitre = new Paragraph(titreFormation != null ? titreFormation : "", sousTitreFont);
        sousTitre.setAlignment(Element.ALIGN_CENTER);
        sousTitre.setSpacingAfter(20);
        document.add(sousTitre);

        PdfPTable table = new PdfPTable(4);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{30, 30, 20, 20});

        for (String entete : new String[]{"Nom", "Prénom", "Date d'inscription", "Statut"}) {
            PdfPCell cell = new PdfPCell(new Phrase(entete, headerFont));
            cell.setPadding(8);
            table.addCell(cell);
        }

        for (Map<String, Object> inscrit : inscrits) {
            table.addCell(new PdfPCell(new Phrase(String.valueOf(inscrit.get("nom")), normalFont)));
            table.addCell(new PdfPCell(new Phrase(String.valueOf(inscrit.get("prenom")), normalFont)));
            table.addCell(new PdfPCell(new Phrase(String.valueOf(inscrit.get("dateInscription")), normalFont)));
            String statut = "valide".equals(inscrit.get("statut")) ? "VALIDE" : "EN_ATTENTE";
            table.addCell(new PdfPCell(new Phrase(statut, normalFont)));
        }

        document.add(table);

        Paragraph footer = new Paragraph(
                "\nTotal : " + inscrits.size() + " inscrit(s).\nDocument généré automatiquement par E-OFANA.",
                normalFont);
        footer.setSpacingBefore(20);
        document.add(footer);

        document.close();
        return outputStream.toByteArray();
    }

    private static class AccesInterditException extends RuntimeException {
        AccesInterditException(String message) {
            super(message);
        }
    }

    private static class IntrouvableException extends RuntimeException {
        IntrouvableException(String message) {
            super(message);
        }
    }
}
