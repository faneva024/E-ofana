package com.eofana.service;

import com.eofana.entity.Inscription;
import com.eofana.entity.Paiement;
import com.eofana.entity.SessionFormation;
import com.eofana.repository.InscriptionRepository;
import com.eofana.repository.PaiementRepository;
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
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * Génère le reçu PDF d'une inscription (T-B-014).
 *
 * Extrait de RecuController, qui mélangeait jusqu'ici accès aux
 * données (SQL brut via JdbcTemplate) et génération du document —
 * cette responsabilité est désormais isolée dans un service dédié,
 * réutilisable depuis InscriptionController (route officielle
 * /api/v1/inscriptions/{id}/recu) et depuis RecuController (route
 * historique conservée pour compatibilité).
 *
 * Contenu conforme au cahier des charges §6.2 : n° de reçu, identité
 * de l'apprenant, formation + centre, date et lieu de la session,
 * prix normal / remise / montant total payé, opérateur Mobile Money
 * et n° de transaction, date de l'inscription.
 */
@Service
public class ReceiptGenerationService {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final InscriptionRepository inscriptionRepository;
    private final PaiementRepository paiementRepository;

    public ReceiptGenerationService(InscriptionRepository inscriptionRepository,
                                     PaiementRepository paiementRepository) {
        this.inscriptionRepository = inscriptionRepository;
        this.paiementRepository = paiementRepository;
    }

    public byte[] generateReceipt(Long idInscription) {
        Inscription inscription = inscriptionRepository.findById(idInscription)
                .orElseThrow(() -> new NoSuchElementException("Inscription introuvable : " + idInscription));

        List<Paiement> paiements = paiementRepository.findByInscription_IdInscriptionOrderByCreatedAtAsc(idInscription);

        try {
            return buildPdf(inscription, paiements);
        } catch (Exception e) {
            throw new IllegalStateException("Erreur lors de la génération du reçu PDF", e);
        }
    }

    private byte[] buildPdf(Inscription inscription, List<Paiement> paiements) throws Exception {
        SessionFormation session = inscription.getSession();

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4);
        PdfWriter.getInstance(document, outputStream);
        document.open();

        Font titreFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
        Font sousTitreFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);
        Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 11);

        Paragraph titre = new Paragraph("REÇU D'INSCRIPTION", titreFont);
        titre.setAlignment(Element.ALIGN_CENTER);
        titre.setSpacingAfter(10);
        document.add(titre);

        Paragraph plateforme = new Paragraph("E-OFANA - Plateforme de formations professionnelles", normalFont);
        plateforme.setAlignment(Element.ALIGN_CENTER);
        plateforme.setSpacingAfter(25);
        document.add(plateforme);

        document.add(section("Apprenant", sousTitreFont));
        PdfPTable tableApprenant = table();
        ajouterLigne(tableApprenant, "Nom", inscription.getUtilisateur().getNom());
        ajouterLigne(tableApprenant, "Prénom", inscription.getUtilisateur().getPrenom());
        ajouterLigne(tableApprenant, "Email", inscription.getUtilisateur().getEmail());
        document.add(tableApprenant);
        document.add(new Paragraph(" "));

        document.add(section("Formation", sousTitreFont));
        PdfPTable tableFormation = table();
        ajouterLigne(tableFormation, "Formation", session.getFormation().getTitre());
        ajouterLigne(tableFormation, "Centre", session.getFormation().getCentre().getNom());
        ajouterLigne(tableFormation, "Lieu", valeurOuTiret(session.getFormation().getLieu()));
        ajouterLigne(tableFormation, "Date de début", session.getDateDebut() != null ? session.getDateDebut().format(DATE_FORMAT) : "-");
        document.add(tableFormation);
        document.add(new Paragraph(" "));

        document.add(section("Détail du montant", sousTitreFont));
        PdfPTable tableMontant = table();
        long prixNormal = session.getFormation().getPrix() != null ? session.getFormation().getPrix() : 0L;
        ajouterLigne(tableMontant, "Prix normal", montant(prixNormal) + " Ar");
        ajouterLigne(tableMontant, "Remise E-OFANA", "-" + montant(inscription.getRemise()) + " Ar");
        ajouterLigne(tableMontant, "Type d'inscription", inscription.getTypeInsc().name());
        ajouterLigne(tableMontant, "Montant total dû", montant(inscription.getMontantPaye()) + " Ar");
        ajouterLigne(tableMontant, "Statut", inscription.getStatut().name());
        document.add(tableMontant);
        document.add(new Paragraph(" "));

        document.add(section("Paiement(s) Mobile Money", sousTitreFont));
        PdfPTable tablePaiements = table();
        ajouterLigne(tablePaiements, "N° reçu", valeurOuTiret(inscription.getNumeroRecu()));
        if (paiements.isEmpty()) {
            ajouterLigne(tablePaiements, "Paiement", "Aucun paiement enregistré");
        } else {
            for (int i = 0; i < paiements.size(); i++) {
                Paiement p = paiements.get(i);
                String libelle = paiements.size() > 1 ? "Versement " + (i + 1) : "Montant payé";
                ajouterLigne(tablePaiements, libelle, montant(p.getMontant()) + " Ar (" + p.getOperateur() + ", " + p.getNumeroTransaction() + ")");
            }
        }
        document.add(tablePaiements);

        Paragraph footer = new Paragraph(
                "\nMerci pour votre confiance.\nDocument généré automatiquement par E-OFANA.",
                normalFont
        );
        footer.setAlignment(Element.ALIGN_CENTER);
        footer.setSpacingBefore(30);
        document.add(footer);

        document.close();
        return outputStream.toByteArray();
    }

    private Paragraph section(String titre, Font font) {
        Paragraph p = new Paragraph(titre, font);
        p.setSpacingAfter(10);
        return p;
    }

    private PdfPTable table() {
        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{35, 65});
        return table;
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

    private String valeurOuTiret(String value) {
        return (value == null || value.isBlank()) ? "-" : value;
    }

    private String montant(Long value) {
        if (value == null) {
            return "0";
        }
        return String.format("%,d", value).replace(",", " ");
    }
}
