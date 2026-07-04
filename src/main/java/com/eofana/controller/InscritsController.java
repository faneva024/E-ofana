package com.eofana.controller;

// (T-B-111).


import com.eofana.entity.Formation;
import com.eofana.entity.Inscription;
import com.eofana.enums.StatutInscription;
import com.eofana.enums.TypeInscription;
import com.eofana.repository.FormationRepository;
import com.eofana.repository.InscriptionRepository;
import com.eofana.security.JwtAuthenticationFilter.AuthenticatedUser;
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
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayOutputStream;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 * Contrôleur formateur pour la consultation des inscrits à une formation 
 *
 * Toute requête à ce contrôleur doit être authentifiée et le formateur doit être le propriétaire de la formation.
 */
@RestController
@RequestMapping("/api/v1/formations/{id}/inscrits")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:5174"})
public class InscritsController {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final FormationRepository formationRepository;
    private final InscriptionRepository inscriptionRepository;

    @GetMapping
    @Transactional(readOnly = true)
    public ResponseEntity<?> getEnrollmentsByCourse(@PathVariable Long id,
                                                      @RequestParam(required = false) Long idSession,
                                                      @AuthenticationPrincipal AuthenticatedUser principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                    "message", "Authentification requise"
            ));
        }

        Formation formation = formationRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Formation introuvable"));

        if (!estProprietaire(formation, principal)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of(
                    "message", "Cette formation ne vous appartient pas"
            ));
        }

        List<InscritResponse> response = recupererInscriptions(id, idSession).stream()
                .map(this::toResponse)
                .toList();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/export-pdf")
    @Transactional(readOnly = true)
    public ResponseEntity<?> exportEnrollmentsToPdf(@PathVariable Long id,
                                                       @RequestParam(required = false) Long idSession,
                                                       @AuthenticationPrincipal AuthenticatedUser principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                    "message", "Authentification requise"
            ));
        }

        Formation formation = formationRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Formation introuvable"));

        if (!estProprietaire(formation, principal)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of(
                    "message", "Cette formation ne vous appartient pas"
            ));
        }

        List<Inscription> inscriptions = recupererInscriptions(id, idSession);

        try {
            byte[] pdf = genererPdf(formation, inscriptions);

            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_PDF)
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"inscrits-formation-" + id + ".pdf\"")
                    .body(pdf);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "message", "Erreur lors de la génération du PDF"
            ));
        }
    }

    private boolean estProprietaire(Formation formation, AuthenticatedUser principal) {
        return formation.getCentre() != null
                && formation.getCentre().getUtilisateur() != null
                && formation.getCentre().getUtilisateur().getIdUser().equals(principal.idUser());
    }

    private List<Inscription> recupererInscriptions(Long idFormation, Long idSession) {
        if (idSession != null) {
            return inscriptionRepository.findBySession_IdSessionAndSession_Formation_IdFormation(idSession, idFormation);
        }
        return inscriptionRepository.findBySession_Formation_IdFormationOrderByCreatedAtDesc(idFormation);
    }

    private InscritResponse toResponse(Inscription inscription) {
        return new InscritResponse(
                inscription.getIdInscription(),
                inscription.getUtilisateur().getNom(),
                inscription.getUtilisateur().getPrenom(),
                inscription.getSession().getIdSession(),
                inscription.getCreatedAt(),
                statutAffiche(inscription)
        );
    }

    /**
     * VALIDE = payé (statut = valide en base). EN_ATTENTE pour une
     * réservation (typeInsc = reservation) tant qu'elle n'est pas
     * validée — règle métier explicitement demandée par le ticket.
     * Les autres statuts (annule, rembourse, termine) sont renvoyés
     * tels quels, en majuscules, pour rester informatifs côté frontend.
     */
    private String statutAffiche(Inscription inscription) {
        if (inscription.getStatut() == StatutInscription.valide) {
            return "VALIDE";
        }
        if (inscription.getTypeInsc() == TypeInscription.reservation) {
            return "EN_ATTENTE";
        }
        return inscription.getStatut().name().toUpperCase();
    }

    private byte[] genererPdf(Formation formation, List<Inscription> inscriptions) throws Exception {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4.rotate());
        PdfWriter.getInstance(document, outputStream);
        document.open();

        Font titreFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16);
        Font sousTitreFont = FontFactory.getFont(FontFactory.HELVETICA, 11);
        Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10);
        Font cellFont = FontFactory.getFont(FontFactory.HELVETICA, 10);

        Paragraph titre = new Paragraph("Liste des inscrits", titreFont);
        titre.setAlignment(Element.ALIGN_CENTER);
        titre.setSpacingAfter(6);
        document.add(titre);

        Paragraph sousTitre = new Paragraph(
                formation.getTitre() + " — " + inscriptions.size() + " inscrit(s)",
                sousTitreFont
        );
        sousTitre.setAlignment(Element.ALIGN_CENTER);
        sousTitre.setSpacingAfter(20);
        document.add(sousTitre);

        PdfPTable table = new PdfPTable(4);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{30, 30, 20, 20});

        for (String entete : List.of("Nom", "Prénom", "Date d'inscription", "Statut")) {
            PdfPCell cell = new PdfPCell(new Phrase(entete, headerFont));
            cell.setPadding(8);
            table.addCell(cell);
        }

        for (Inscription inscription : inscriptions) {
            table.addCell(cellule(inscription.getUtilisateur().getNom(), cellFont));
            table.addCell(cellule(inscription.getUtilisateur().getPrenom(), cellFont));
            table.addCell(cellule(
                    inscription.getCreatedAt() != null ? inscription.getCreatedAt().format(DATE_FORMAT) : "-",
                    cellFont
            ));
            table.addCell(cellule(statutAffiche(inscription), cellFont));
        }

        if (inscriptions.isEmpty()) {
            PdfPCell vide = new PdfPCell(new Phrase("Aucun inscrit pour le moment", cellFont));
            vide.setColspan(4);
            vide.setPadding(10);
            table.addCell(vide);
        }

        document.add(table);
        document.close();

        return outputStream.toByteArray();
    }

    private PdfPCell cellule(String valeur, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(valeur != null ? valeur : "-", font));
        cell.setPadding(8);
        return cell;
    }

    public record InscritResponse(
            Long idInscription,
            String nom,
            String prenom,
            Long idSession,
            OffsetDateTime dateInscription,
            String statut
    ) {
    }
}