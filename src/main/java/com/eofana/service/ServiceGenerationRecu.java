package com.eofana.service;

import com.eofana.entity.Inscription;
import com.eofana.repository.InscriptionRepository;
import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import org.springframework.stereotype.Service;
import java.awt.Color;
import java.io.ByteArrayOutputStream;

@Service
public class ServiceGenerationRecu {

    private final InscriptionRepository inscriptionRepository;

    public ServiceGenerationRecu(InscriptionRepository inscriptionRepository) {
        this.inscriptionRepository = inscriptionRepository;
    }

    public byte[] genererRecu(Long idInscription) {
        Inscription inscription = inscriptionRepository.findById(idInscription)
                .orElseThrow(() -> new RuntimeException("Inscription introuvable : " + idInscription));

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4);
            PdfWriter.getInstance(document, baos);
            document.open();

            // Titre
            Font fontTitre = new Font(Font.HELVETICA, 18, Font.BOLD, new Color(33, 37, 41));
            Paragraph titre = new Paragraph("REÇU D'INSCRIPTION - E-OFANA", fontTitre);
            titre.setAlignment(Element.ALIGN_CENTER);
            document.add(titre);
            document.add(Chunk.NEWLINE);

            Font fontLabel = new Font(Font.HELVETICA, 11, Font.BOLD);
            Font fontValeur = new Font(Font.HELVETICA, 11, Font.NORMAL);

            ajouterLigne(document, "N° Reçu :", inscription.getNumeroRecu() != null ? inscription.getNumeroRecu() : "N/A", fontLabel, fontValeur);
            ajouterLigne(document, "Apprenant :", inscription.getUtilisateur().getPrenom() + " " + inscription.getUtilisateur().getNom(), fontLabel, fontValeur);
            ajouterLigne(document, "Email :", inscription.getUtilisateur().getEmail(), fontLabel, fontValeur);
            ajouterLigne(document, "Formation :", inscription.getFormation().getTitre(), fontLabel, fontValeur);
            ajouterLigne(document, "Lieu :", inscription.getFormation().getLieu() != null ? inscription.getFormation().getLieu() : "N/A", fontLabel, fontValeur);
            ajouterLigne(document, "Prix :", inscription.getFormation().getPrix() + " Ar", fontLabel, fontValeur);
            ajouterLigne(document, "Opérateur paiement :", inscription.getOperateurPaiement() != null ? inscription.getOperateurPaiement() : "N/A", fontLabel, fontValeur);
            ajouterLigne(document, "Statut paiement :", inscription.getStatutPaiement(), fontLabel, fontValeur);
            ajouterLigne(document, "Date inscription :", inscription.getDateInscription() != null ? inscription.getDateInscription().toString() : "N/A", fontLabel, fontValeur);
            ajouterLigne(document, "Type :", Boolean.TRUE.equals(inscription.getReservationUniquement()) ? "Réservation" : "Inscription complète", fontLabel, fontValeur);

            document.add(Chunk.NEWLINE);
            Font fontNote = new Font(Font.HELVETICA, 9, Font.ITALIC, Color.GRAY);
            document.add(new Paragraph("Ce reçu est généré automatiquement par la plateforme E-Ofana.", fontNote));

            document.close();
            return baos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de la génération du reçu PDF", e);
        }
    }

    private void ajouterLigne(Document doc, String label, String valeur, Font fontLabel, Font fontValeur) throws DocumentException {
        Paragraph p = new Paragraph();
        p.add(new Chunk(label + " ", fontLabel));
        p.add(new Chunk(valeur, fontValeur));
        p.setSpacingAfter(4);
        doc.add(p);
    }
}
