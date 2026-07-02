package com.eofana.controller;

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
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/recus")
@CrossOrigin(origins = {
        "http://localhost:5173",
        "http://localhost:5174"
})
public class RecuController {

    private final JdbcTemplate jdbcTemplate;

    public RecuController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @GetMapping("/{idInscription}/pdf")
    public ResponseEntity<?> telechargerRecuPdf(@PathVariable Long idInscription) {
        List<Map<String, Object>> result = jdbcTemplate.queryForList(
                """
                SELECT
                    i."idInscription" AS id_inscription,
                    i."numeroRecu" AS numero_recu,
                    i."montantPaye" AS montant_inscription,
                    i.operateur::text AS operateur_inscription,
                    i."transactionId" AS transaction_inscription,

                    u.nom AS nom,
                    u.prenom AS prenom,
                    u.email AS email,

                    f.titre AS titre_formation,
                    COALESCE(f."prixRemise", f.prix, 0) AS prix_formation,

                    p."idPaiement" AS id_paiement,
                    p.montant AS montant_paiement,
                    p.operateur::text AS operateur_paiement,
                    p."numeroTransaction" AS numero_transaction,
                    p.statut::text AS statut_paiement,
                    p."createdAt" AS date_paiement
                FROM eofana.inscriptions i
                JOIN eofana.utilisateurs u ON u."idUser" = i."idUser"
                JOIN eofana."sessionsFormation" s ON s."idSession" = i."idSession"
                JOIN eofana.formations f ON f."idFormation" = s."idFormation"
                LEFT JOIN eofana.paiements p ON p."idInscription" = i."idInscription"
                WHERE i."idInscription" = ?
                ORDER BY p."createdAt" DESC NULLS LAST
                LIMIT 1
                """,
                idInscription
        );

        if (result.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "message", "Inscription introuvable"
            ));
        }

        Map<String, Object> recu = result.get(0);

        if (recu.get("id_paiement") == null) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(
                    "message", "Aucun paiement trouvé pour cette inscription"
            ));
        }

        try {
            byte[] pdf = genererPdf(recu);

            String nomFichier = "recu-inscription-" + idInscription + ".pdf";

            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_PDF)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + nomFichier + "\"")
                    .body(pdf);

        } catch (Exception e) {
            e.printStackTrace();

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "message", "Erreur lors de la génération du reçu PDF"
            ));
        }
    }

    private byte[] genererPdf(Map<String, Object> recu) throws Exception {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        Document document = new Document(PageSize.A4);
        PdfWriter.getInstance(document, outputStream);

        document.open();

        Font titreFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
        Font sousTitreFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);
        Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 11);

        Paragraph titre = new Paragraph("REÇU DE PAIEMENT", titreFont);
        titre.setAlignment(Element.ALIGN_CENTER);
        titre.setSpacingAfter(10);
        document.add(titre);

        Paragraph plateforme = new Paragraph("E-OFANA - Plateforme de formations professionnelles", normalFont);
        plateforme.setAlignment(Element.ALIGN_CENTER);
        plateforme.setSpacingAfter(25);
        document.add(plateforme);

        Paragraph infosPaiement = new Paragraph("Informations du paiement", sousTitreFont);
        infosPaiement.setSpacingAfter(10);
        document.add(infosPaiement);

        PdfPTable tablePaiement = new PdfPTable(2);
        tablePaiement.setWidthPercentage(100);
        tablePaiement.setWidths(new float[]{35, 65});

        ajouterLigne(tablePaiement, "N° reçu", valeur(recu.get("numero_recu")));
        ajouterLigne(tablePaiement, "N° transaction", valeur(recu.get("numero_transaction")));
        ajouterLigne(tablePaiement, "Statut paiement", valeur(recu.get("statut_paiement")));
        ajouterLigne(tablePaiement, "Opérateur", valeur(recu.get("operateur_paiement")));
        ajouterLigne(tablePaiement, "Montant payé", montant(recu.get("montant_paiement")) + " Ar");
        ajouterLigne(tablePaiement, "Date paiement", valeur(recu.get("date_paiement")));

        document.add(tablePaiement);

        document.add(new Paragraph(" "));

        Paragraph infosUtilisateur = new Paragraph("Informations de l'apprenant", sousTitreFont);
        infosUtilisateur.setSpacingAfter(10);
        document.add(infosUtilisateur);

        PdfPTable tableUtilisateur = new PdfPTable(2);
        tableUtilisateur.setWidthPercentage(100);
        tableUtilisateur.setWidths(new float[]{35, 65});

        ajouterLigne(tableUtilisateur, "Nom", valeur(recu.get("nom")));
        ajouterLigne(tableUtilisateur, "Prénom", valeur(recu.get("prenom")));
        ajouterLigne(tableUtilisateur, "Email", valeur(recu.get("email")));

        document.add(tableUtilisateur);

        document.add(new Paragraph(" "));

        Paragraph infosFormation = new Paragraph("Formation", sousTitreFont);
        infosFormation.setSpacingAfter(10);
        document.add(infosFormation);

        PdfPTable tableFormation = new PdfPTable(2);
        tableFormation.setWidthPercentage(100);
        tableFormation.setWidths(new float[]{35, 65});

        ajouterLigne(tableFormation, "Formation", valeur(recu.get("titre_formation")));
        ajouterLigne(tableFormation, "Prix formation", montant(recu.get("prix_formation")) + " Ar");

        document.add(tableFormation);

        Paragraph footer = new Paragraph(
                "\nMerci pour votre paiement.\nDocument généré automatiquement par E-OFANA.",
                normalFont
        );
        footer.setAlignment(Element.ALIGN_CENTER);
        footer.setSpacingBefore(30);
        document.add(footer);

        document.close();

        return outputStream.toByteArray();
    }

    private void ajouterLigne(PdfPTable table, String label, String value) {
        Font labelFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10);
        Font valueFont = FontFactory.getFont(FontFactory.HELVETICA, 10);

        PdfPCell labelCell = new PdfPCell(new Phrase(label, labelFont));
        labelCell.setPadding(8);

        PdfPCell valueCell = new PdfPCell(new Phrase(value, valueFont));
        valueCell.setPadding(8);

        table.addCell(labelCell);
        table.addCell(valueCell);
    }

    private String valeur(Object value) {
        if (value == null) {
            return "-";
        }

        return value.toString();
    }

    private String montant(Object value) {
        if (value == null) {
            return "0";
        }

        if (value instanceof Number number) {
            return String.format("%,d", number.longValue()).replace(",", " ");
        }

        return value.toString();
    }
}