package com.eofana.controller;

import com.eofana.security.AdminContextResolver;
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
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.Map;

/**
 * T-B-211 : liste de TOUS les inscrits, toutes formations/centres
 * confondus, avec exports PDF et Excel. Même approche technique que
 * InscritsController (T-B-111, module Formateur) — JdbcTemplate + le
 * même pattern OpenPDF, sans le filtre "un seul centre" puisque ce
 * contrôleur est admin — étendue avec la pagination et les filtres
 * centre/formation demandés par le ticket.
 */
@RestController
@RequestMapping("/api/v1/admin/inscrits")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:5174"})
public class AdminInscritsController {

    private final NamedParameterJdbcTemplate jdbc;
    private final AdminContextResolver adminContextResolver;

    @GetMapping
    @Transactional(readOnly = true)
    public ResponseEntity<?> getAllEnrollments(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestParam(required = false) Long idCentre,
            @RequestParam(required = false) Long idFormation,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int taille) {

        try {
            adminContextResolver.resoudreAdmin(authorization);
        } catch (AdminContextResolver.AccesNonAutoriseException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", e.getMessage()));
        }

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("idCentre", idCentre)
                .addValue("idFormation", idFormation)
                .addValue("taille", taille)
                .addValue("offset", page * taille);

        String requete = requeteInscrits(idCentre != null, idFormation != null)
                + " ORDER BY i.\"createdAt\" DESC LIMIT :taille OFFSET :offset";
        List<Map<String, Object>> inscrits = jdbc.queryForList(requete, params);

        Long total = jdbc.queryForObject(
                "SELECT COUNT(*) " + fromEtWhere(idCentre != null, idFormation != null),
                params, Long.class);

        return ResponseEntity.ok(Map.of(
                "contenu", inscrits,
                "page", page,
                "taille", taille,
                "total", total != null ? total : 0
        ));
    }

    @GetMapping("/export-pdf")
    @Transactional(readOnly = true)
    public ResponseEntity<?> exportAllEnrollmentsToPdf(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestParam(required = false) Long idCentre,
            @RequestParam(required = false) Long idFormation) {

        try {
            adminContextResolver.resoudreAdmin(authorization);
        } catch (AdminContextResolver.AccesNonAutoriseException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", e.getMessage()));
        }

        List<Map<String, Object>> inscrits = chargerInscrits(idCentre, idFormation);

        try {
            byte[] pdf = genererPdf(inscrits);
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_PDF)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"inscrits-eofana.pdf\"")
                    .body(pdf);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Erreur lors de la génération du PDF"));
        }
    }

    @GetMapping("/export-excel")
    @Transactional(readOnly = true)
    public ResponseEntity<?> exportAllEnrollmentsToExcel(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestParam(required = false) Long idCentre,
            @RequestParam(required = false) Long idFormation) {

        try {
            adminContextResolver.resoudreAdmin(authorization);
        } catch (AdminContextResolver.AccesNonAutoriseException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", e.getMessage()));
        }

        List<Map<String, Object>> inscrits = chargerInscrits(idCentre, idFormation);

        try {
            byte[] excel = genererExcel(inscrits);
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"inscrits-eofana.xlsx\"")
                    .body(excel);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Erreur lors de la génération du fichier Excel"));
        }
    }

    private List<Map<String, Object>> chargerInscrits(Long idCentre, Long idFormation) {
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("idCentre", idCentre)
                .addValue("idFormation", idFormation);
        String requete = requeteInscrits(idCentre != null, idFormation != null) + " ORDER BY i.\"createdAt\" DESC";
        return jdbc.queryForList(requete, params);
    }

    private String fromEtWhere(boolean filtrerCentre, boolean filtrerFormation) {
        StringBuilder sql = new StringBuilder("""
                FROM eofana.inscriptions i
                JOIN eofana.utilisateurs u ON u."idUser" = i."idUser"
                JOIN eofana."sessionsFormation" s ON s."idSession" = i."idSession"
                JOIN eofana.formations f ON f."idFormation" = s."idFormation"
                JOIN eofana.centres c ON c."idCentre" = f."idCentre"
                WHERE 1 = 1
                """);
        if (filtrerCentre) sql.append(" AND c.\"idCentre\" = :idCentre");
        if (filtrerFormation) sql.append(" AND f.\"idFormation\" = :idFormation");
        return sql.toString();
    }

    private String requeteInscrits(boolean filtrerCentre, boolean filtrerFormation) {
        return """
                SELECT u.nom AS "apprenantNom", u.prenom AS "apprenantPrenom", u.email AS "apprenantEmail",
                       f.titre AS "formationTitre", c.nom AS "centreNom",
                       i."createdAt" AS "dateInscription", i.statut AS statut,
                       i."montantPaye" AS "montantPaye", i."numeroRecu" AS "numeroRecu"
                """ + fromEtWhere(filtrerCentre, filtrerFormation);
    }

    private byte[] genererPdf(List<Map<String, Object>> inscrits) throws Exception {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4.rotate());
        PdfWriter.getInstance(document, outputStream);
        document.open();

        Font titreFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16);
        Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9);
        Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 9);

        Paragraph titre = new Paragraph("LISTE DES INSCRITS — TOUTES FORMATIONS", titreFont);
        titre.setAlignment(Element.ALIGN_CENTER);
        titre.setSpacingAfter(15);
        document.add(titre);

        String[] entetes = {"Apprenant", "Email", "Formation", "Centre", "Date", "Statut", "Montant payé"};
        PdfPTable table = new PdfPTable(entetes.length);
        table.setWidthPercentage(100);

        for (String entete : entetes) {
            PdfPCell cell = new PdfPCell(new Phrase(entete, headerFont));
            cell.setPadding(6);
            table.addCell(cell);
        }

        for (Map<String, Object> inscrit : inscrits) {
            table.addCell(new PdfPCell(new Phrase(
                    inscrit.get("apprenantNom") + " " + inscrit.get("apprenantPrenom"), normalFont)));
            table.addCell(new PdfPCell(new Phrase(String.valueOf(inscrit.get("apprenantEmail")), normalFont)));
            table.addCell(new PdfPCell(new Phrase(String.valueOf(inscrit.get("formationTitre")), normalFont)));
            table.addCell(new PdfPCell(new Phrase(String.valueOf(inscrit.get("centreNom")), normalFont)));
            table.addCell(new PdfPCell(new Phrase(String.valueOf(inscrit.get("dateInscription")), normalFont)));
            String statut = "valide".equals(inscrit.get("statut")) ? "VALIDE" : "EN_ATTENTE";
            table.addCell(new PdfPCell(new Phrase(statut, normalFont)));
            table.addCell(new PdfPCell(new Phrase(String.valueOf(inscrit.get("montantPaye")), normalFont)));
        }

        document.add(table);

        Paragraph footer = new Paragraph(
                "\nTotal : " + inscrits.size() + " inscrit(s). Document généré automatiquement par E-OFANA.",
                normalFont);
        footer.setSpacingBefore(15);
        document.add(footer);

        document.close();
        return outputStream.toByteArray();
    }

    private byte[] genererExcel(List<Map<String, Object>> inscrits) throws Exception {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Inscrits");

            CellStyle enteteStyle = workbook.createCellStyle();
            org.apache.poi.ss.usermodel.Font enteteFont = workbook.createFont();
            enteteFont.setBold(true);
            enteteStyle.setFont(enteteFont);

            String[] entetes = {"Apprenant", "Prénom", "Email", "Formation", "Centre", "Date d'inscription", "Statut", "Montant payé (Ar)", "N° reçu"};
            Row ligneEntete = sheet.createRow(0);
            for (int i = 0; i < entetes.length; i++) {
                Cell cell = ligneEntete.createCell(i);
                cell.setCellValue(entetes[i]);
                cell.setCellStyle(enteteStyle);
            }

            int ligne = 1;
            for (Map<String, Object> inscrit : inscrits) {
                Row row = sheet.createRow(ligne++);
                row.createCell(0).setCellValue(String.valueOf(inscrit.get("apprenantNom")));
                row.createCell(1).setCellValue(String.valueOf(inscrit.get("apprenantPrenom")));
                row.createCell(2).setCellValue(String.valueOf(inscrit.get("apprenantEmail")));
                row.createCell(3).setCellValue(String.valueOf(inscrit.get("formationTitre")));
                row.createCell(4).setCellValue(String.valueOf(inscrit.get("centreNom")));
                row.createCell(5).setCellValue(String.valueOf(inscrit.get("dateInscription")));
                row.createCell(6).setCellValue("valide".equals(inscrit.get("statut")) ? "VALIDE" : "EN_ATTENTE");
                Object montant = inscrit.get("montantPaye");
                row.createCell(7).setCellValue(montant != null ? ((Number) montant).doubleValue() : 0d);
                row.createCell(8).setCellValue(String.valueOf(inscrit.get("numeroRecu")));
            }

            for (int i = 0; i < entetes.length; i++) {
                sheet.autoSizeColumn(i);
            }

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            return outputStream.toByteArray();
        }
    }
}
