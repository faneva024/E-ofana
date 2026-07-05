package com.eofana.service;

import com.eofana.entity.Centre;
import com.eofana.entity.Formation;
import com.eofana.entity.SessionFormation;
import com.eofana.entity.Virement;
import com.eofana.enums.StatutVirement;
import com.eofana.repository.CentreRepository;
import com.eofana.repository.FormationRepository;
import com.eofana.repository.SessionFormationRepository;
import com.eofana.repository.VirementRepository;

import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfWriter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

/**
 * ServiceVirementAutomatique — T-B-113
 * Branche : Back → part-Rolph  |  Semaine 2
 *
 * Génère automatiquement les virements vers les centres de formation
 * selon la fréquence configurée dans leur profil.
 *
 * Aligné sur V9 :
 *   - FK idCentre (pas idFormateur)
 *   - "commission" (pas commissionTotale)
 *   - BIGINT en Ariary (pas BigDecimal)
 *   - OperateurMm depuis Centre.mobileMoneyOperateur
 *
 * Réutilise OpenPDF (déjà dans pom.xml) pour les justificatifs PDF.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ServiceVirementAutomatique {

    private final CentreRepository           centreRepository;
    private final FormationRepository         formationRepository;
    private final SessionFormationRepository  sessionFormationRepository;
    private final VirementRepository          virementRepository;

    private static final String DOSSIER_JUSTIFICATIFS = "uploads/justificatifs-virements";

    // ════════════════════════════════════════════════════════════════════════
    // Chaque lundi à 06h00 — centres en fréquence hebdomadaire
    // ════════════════════════════════════════════════════════════════════════
    @Scheduled(cron = "0 0 6 * * MON")
    @Transactional
    public void genererVirementsHebdomadaires() {
        log.info("[VirementAuto] Début traitement hebdomadaire — {}", LocalDate.now());

        LocalDate aujourdHui   = LocalDate.now();
        LocalDate periodeDebut = aujourdHui.minusDays(7);
        LocalDate periodeFin   = aujourdHui.minusDays(1);

        List<Centre> centres = centreRepository.findAll().stream()
            .filter(c -> c.getFrequenceReversement() != null
                      && "hebdomadaire".equals(c.getFrequenceReversement().name()))
            .toList();

        for (Centre centre : centres) {
            try {
                genererVirementPourCentre(centre, periodeDebut, periodeFin);
            } catch (Exception e) {
                log.error("[VirementAuto] Erreur centre {} : {}", centre.getIdCentre(), e.getMessage());
            }
        }

        log.info("[VirementAuto] Fin traitement hebdomadaire — {} centres traités", centres.size());
    }

    // ════════════════════════════════════════════════════════════════════════
    // Le 1er de chaque mois à 06h00 — centres en fréquence mensuelle
    // ════════════════════════════════════════════════════════════════════════
    @Scheduled(cron = "0 0 6 1 * *")
    @Transactional
    public void genererVirementsMensuels() {
        log.info("[VirementAuto] Début traitement mensuel — {}", LocalDate.now());

        LocalDate aujourd = LocalDate.now();
        LocalDate moisPrecedent = aujourd.minusMonths(1);
        LocalDate periodeDebut  = moisPrecedent.withDayOfMonth(1);
        LocalDate periodeFin    = moisPrecedent.withDayOfMonth(moisPrecedent.lengthOfMonth());

        List<Centre> centres = centreRepository.findAll().stream()
            .filter(c -> c.getFrequenceReversement() != null
                      && "mensuel".equals(c.getFrequenceReversement().name()))
            .toList();

        for (Centre centre : centres) {
            try {
                genererVirementPourCentre(centre, periodeDebut, periodeFin);
            } catch (Exception e) {
                log.error("[VirementAuto] Erreur centre {} : {}", centre.getIdCentre(), e.getMessage());
            }
        }

        log.info("[VirementAuto] Fin traitement mensuel — {} centres traités", centres.size());
    }

    // ── Logique commune : calculer et créer le virement ──────────────────────
    private void genererVirementPourCentre(Centre centre,
                                            LocalDate periodeDebut,
                                            LocalDate periodeFin) {
        // Éviter les doublons sur la même période
        if (virementRepository.existeVirementSurPeriode(
                centre.getIdCentre(), periodeDebut, periodeFin)) {
            log.info("[VirementAuto] Virement déjà effectué pour centre {} sur cette période.", centre.getIdCentre());
            return;
        }

        // Calcul du montant brut = somme des prix des sessions avec inscrits sur la période
        List<Formation> formations = formationRepository
            .findByCentre_IdCentre(centre.getIdCentre());

        long montantBrutAr = 0L;
        for (Formation f : formations) {
            List<SessionFormation> sessions = sessionFormationRepository
                .findByFormation_IdFormation(f.getIdFormation());
            for (SessionFormation s : sessions) {
                // Séances dont le début est dans la période
                if (s.getDateDebut() != null
                    && !s.getDateDebut().isBefore(periodeDebut)
                    && !s.getDateDebut().isAfter(periodeFin)) {
                    int inscrits = s.getPlacesTotal() - s.getPlacesRestantes();
                    if (inscrits > 0) {
                        long prixUnitaire = f.getPrixRemise() != null
                            ? f.getPrixRemise() : f.getPrix();
                        montantBrutAr += prixUnitaire * inscrits;
                    }
                }
            }
        }

        if (montantBrutAr <= 0) {
            log.info("[VirementAuto] Aucun revenu pour centre {} sur la période.", centre.getIdCentre());
            return;
        }

        // Commission = tauxCommission% du montant brut
        BigDecimal taux = centre.getTauxCommission()
            .divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);
        long commissionAr = BigDecimal.valueOf(montantBrutAr)
            .multiply(taux).setScale(0, RoundingMode.HALF_UP).longValue();
        long montantNetAr = montantBrutAr - commissionAr;

        // Créer le virement
        Virement virement = Virement.builder()
            .centre(centre)
            .montantBrut(montantBrutAr)
            .commission(commissionAr)
            .montantNet(montantNetAr)
            .operateur(centre.getMobileMoneyOperateur())
            .referenceVirement(genererReference())
            .statut(StatutVirement.planifie)
            .periodeDebut(periodeDebut)
            .periodeFin(periodeFin)
            .dateVirement(OffsetDateTime.now())
            .build();

        virement = virementRepository.save(virement);

        // Générer le justificatif PDF
        try {
            String cheminPdf = genererJustificatifVirement(virement, centre);
            log.info("[VirementAuto] Justificatif généré : {}", cheminPdf);
        } catch (Exception e) {
            log.warn("[VirementAuto] Erreur génération PDF : {}", e.getMessage());
        }

        // Passer en effectué
        virement.setStatut(StatutVirement.effectue);
        virementRepository.save(virement);

        log.info("[VirementAuto] Virement {} effectué — centre {} — net : {} Ar",
            virement.getReferenceVirement(), centre.getNom(), montantNetAr);

        // TODO : déclencher envoi email justificatif (service Node.js)
    }

    // ── Générer une référence unique ──────────────────────────────────────────
    private String genererReference() {
        String date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        return "VIR-" + date + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    // ════════════════════════════════════════════════════════════════════════
    // Générer le justificatif PDF avec OpenPDF (déjà dans pom.xml)
    // ════════════════════════════════════════════════════════════════════════
    public String genererJustificatifVirement(Virement virement, Centre centre)
            throws DocumentException, IOException {

        Document document = new Document(PageSize.A4, 50, 50, 60, 60);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter.getInstance(document, baos);
        document.open();

        Font fontTitre  = new Font(Font.HELVETICA, 18, Font.BOLD);
        Font fontLabel  = new Font(Font.HELVETICA, 11, Font.BOLD);
        Font fontValeur = new Font(Font.HELVETICA, 11, Font.NORMAL);

        // Titre
        Paragraph titre = new Paragraph("E-OFANA — Justificatif de Virement", fontTitre);
        titre.setAlignment(Element.ALIGN_CENTER);
        titre.setSpacingAfter(24);
        document.add(titre);

        document.add(ligne("Référence",       virement.getReferenceVirement(), fontLabel, fontValeur));
        document.add(ligne("Centre",          centre.getNom(), fontLabel, fontValeur));
        document.add(ligne("Formateur",       centre.getUtilisateur().getPrenom()
                                              + " " + centre.getUtilisateur().getNom(), fontLabel, fontValeur));
        document.add(ligne("Période",         virement.getPeriodeDebut() + " → " + virement.getPeriodeFin(),
                                              fontLabel, fontValeur));
        document.add(ligne("Montant brut",    virement.getMontantBrut() + " Ar", fontLabel, fontValeur));
        document.add(ligne("Commission E-OFANA", virement.getCommission() + " Ar", fontLabel, fontValeur));
        document.add(ligne("Montant net viré",virement.getMontantNet() + " Ar", fontLabel, fontValeur));
        document.add(ligne("Opérateur",       virement.getOperateur() != null
                                              ? virement.getOperateur().name() : "—", fontLabel, fontValeur));
        document.add(ligne("Date virement",   virement.getDateVirement() != null
                                              ? virement.getDateVirement().toLocalDate().toString() : "—",
                                              fontLabel, fontValeur));

        document.close();

        Path dossier = Paths.get(DOSSIER_JUSTIFICATIFS);
        Files.createDirectories(dossier);
        String nomFichier = "justificatif-" + virement.getReferenceVirement() + ".pdf";
        Path chemin = dossier.resolve(nomFichier);
        Files.write(chemin, baos.toByteArray());

        return chemin.toString();
    }

    private Paragraph ligne(String label, String valeur, Font fontLabel, Font fontValeur) {
        Paragraph p = new Paragraph();
        p.add(new Chunk(label + " : ", fontLabel));
        p.add(new Chunk(valeur != null ? valeur : "—", fontValeur));
        p.setSpacingAfter(8);
        return p;
    }
}
