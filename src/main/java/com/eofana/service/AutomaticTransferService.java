package com.eofana.service;

import com.eofana.entity.Centre;
import com.eofana.entity.Virement;
import com.eofana.enums.FrequenceReversement;
import com.eofana.enums.StatutVirement;
import com.eofana.repository.CentreRepository;
import com.eofana.repository.VirementRepository;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.Map;

/**
 * T-B-113 : génération programmée des virements vers les centres.
 *
 * Le service Node.js d'envoi d'email (services/service-email) est un
 * microservice HTTP séparé — on l'appelle ici via RestTemplate plutôt
 * que de dupliquer sa logique côté Java, conformément à l'architecture
 * définie dans docker-compose.yml.
 */
@Service
@RequiredArgsConstructor
public class AutomaticTransferService {

    private static final Logger log = LoggerFactory.getLogger(AutomaticTransferService.class);

    private final CentreRepository centreRepository;
    private final VirementRepository virementRepository;
    private final FinanceCalculationService financeCalculationService;
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${eofana.service-email.url:http://localhost:3001}")
    private String serviceEmailUrl;

    /** Traitement programmé chaque lundi (centres en fréquence "hebdomadaire"). */
    @Scheduled(cron = "${eofana.virements.cron-hebdomadaire:0 0 6 * * MON}")
    public void generateWeeklyTransfers() {
        LocalDate periodeFin = LocalDate.now().minusDays(1);
        LocalDate periodeDebut = periodeFin.minusWeeks(1).plusDays(1);

        centreRepository.findAll().stream()
                .filter(centre -> centre.getFrequenceReversement() == FrequenceReversement.hebdomadaire)
                .forEach(centre -> genererVirementPourCentre(centre, periodeDebut, periodeFin));
    }

    /** Traitement programmé le 1er de chaque mois (centres en fréquence "mensuel"). */
    @Scheduled(cron = "${eofana.virements.cron-mensuel:0 0 6 1 * *}")
    public void generateMonthlyTransfers() {
        LocalDate periodeFin = LocalDate.now().minusDays(1);
        LocalDate periodeDebut = periodeFin.with(TemporalAdjusters.firstDayOfMonth());

        centreRepository.findAll().stream()
                .filter(centre -> centre.getFrequenceReversement() == FrequenceReversement.mensuel)
                .forEach(centre -> genererVirementPourCentre(centre, periodeDebut, periodeFin));
    }

    private void genererVirementPourCentre(Centre centre, LocalDate periodeDebut, LocalDate periodeFin) {
        FinanceCalculationService.ProchainVirement estimation =
                financeCalculationService.calculateNextTransfer(centre);

        if (estimation.montantEstime() <= 0) {
            return;
        }

        long montantBrut = financeCalculationService.calculateGrossRevenue(centre);
        long commission = financeCalculationService.calculateCommissionCollected(centre);
        long montantNet = estimation.montantEstime();

        Virement virement = Virement.builder()
                .centre(centre)
                .montantBrut(montantBrut)
                .commission(commission)
                .montantNet(montantNet)
                .operateur(centre.getMobileMoneyOperateur())
                .referenceVirement("VIR-" + centre.getIdCentre() + "-" + System.currentTimeMillis())
                .statut(StatutVirement.planifie)
                .periodeDebut(periodeDebut)
                .periodeFin(periodeFin)
                .build();

        Virement saved = virementRepository.save(virement);

        // Le passage à statut = effectue est du ressort de l'intégration Mobile
        // Money réelle (services/service-mobile-money) : hors périmètre ici, on
        // se contente de planifier le virement et de générer son justificatif.
        byte[] recu = generateTransferReceipt(saved);
        envoyerJustificatifParEmail(centre, saved, recu);
    }

    /** Crée le PDF justificatif d'un virement. */
    public byte[] generateTransferReceipt(Virement virement) {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            Document document = new Document(PageSize.A4);
            PdfWriter.getInstance(document, outputStream);
            document.open();

            Font titreFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16);
            Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 11);

            Paragraph titre = new Paragraph("JUSTIFICATIF DE VIREMENT", titreFont);
            titre.setAlignment(Element.ALIGN_CENTER);
            titre.setSpacingAfter(20);
            document.add(titre);

            PdfPTable table = new PdfPTable(2);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{40, 60});

            ajouterLigne(table, "Centre", virement.getCentre().getNom());
            ajouterLigne(table, "Référence", String.valueOf(virement.getReferenceVirement()));
            ajouterLigne(table, "Période", virement.getPeriodeDebut() + " au " + virement.getPeriodeFin());
            ajouterLigne(table, "Montant brut", virement.getMontantBrut() + " Ar");
            ajouterLigne(table, "Commission", virement.getCommission() + " Ar");
            ajouterLigne(table, "Montant net", virement.getMontantNet() + " Ar");
            ajouterLigne(table, "Opérateur", virement.getOperateur() != null ? virement.getOperateur().name() : "-");
            ajouterLigne(table, "Statut", virement.getStatut().name());

            document.add(table);
            document.close();

            return outputStream.toByteArray();
        } catch (Exception e) {
            log.error("Erreur lors de la génération du justificatif de virement {}", virement.getIdVirement(), e);
            return new byte[0];
        }
    }

    private void ajouterLigne(PdfPTable table, String label, String valeur) {
        Font labelFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10);
        Font valueFont = FontFactory.getFont(FontFactory.HELVETICA, 10);
        PdfPCell labelCell = new PdfPCell(new Phrase(label, labelFont));
        labelCell.setPadding(8);
        PdfPCell valueCell = new PdfPCell(new Phrase(valeur, valueFont));
        valueCell.setPadding(8);
        table.addCell(labelCell);
        table.addCell(valueCell);
    }

    /**
     * Déclenche l'envoi automatique du justificatif par email via le
     * service Node.js d'envoi d'email (table notifications,
     * typeNotif = VIREMENT).
     */
    private void envoyerJustificatifParEmail(Centre centre, Virement virement, byte[] justificatifPdf) {
        if (centre.getEmail() == null || centre.getEmail().isBlank()) {
            log.warn("Centre {} sans email : justificatif de virement {} non envoyé", centre.getIdCentre(), virement.getIdVirement());
            return;
        }

        try {
            Map<String, Object> payload = Map.of(
                    "destinataire", centre.getEmail(),
                    "typeNotif", "VIREMENT",
                    "idVirement", virement.getIdVirement(),
                    "montantNet", virement.getMontantNet()
                    // Le PDF lui-même serait envoyé en pièce jointe (base64) dans une
                    // implémentation complète ; simplifié ici, le service email n'étant
                    // pas dans le périmètre de ce module.
            );

            restTemplate.postForEntity(serviceEmailUrl + "/api/notifications/virement", payload, Void.class);
        } catch (RestClientException e) {
            log.error("Échec de l'envoi du justificatif de virement {} au service email", virement.getIdVirement(), e);
        }
    }
}
