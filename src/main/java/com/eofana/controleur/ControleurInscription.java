package com.eofana.controleur;

import com.eofana.dto.requete.RequeteInscription;
import com.eofana.dto.requete.RequeteReservation;
import com.eofana.dto.requete.RequeteConfirmationPaiement;
import com.eofana.dto.reponse.ReponseInscription;
import com.eofana.dto.reponse.ReponsePaiement;
import com.eofana.entite.Apprenant;
import com.eofana.entite.Formation;
import com.eofana.entite.Inscription;
import com.eofana.entite.Paiement;
import com.eofana.depot.ApprenantDepot;
import com.eofana.depot.FormationDepot;
import com.eofana.depot.InscriptionDepot;
import com.eofana.depot.PaiementDepot;
import com.eofana.service.ServicePaiement;
import com.eofana.service.ServiceGenerationRecu;

import jakarta.validation.Valid;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/inscriptions")
public class ControleurInscription {

    private final InscriptionDepot inscriptionDepot;
    private final FormationDepot formationDepot;
    private final ApprenantDepot apprenantDepot;
    private final PaiementDepot paiementDepot;
    private final ServicePaiement servicePaiement;
    private final ServiceGenerationRecu serviceGenerationRecu;
    private final ApplicationEventPublisher eventPublisher;

    public ControleurInscription(
            InscriptionDepot inscriptionDepot,
            FormationDepot formationDepot,
            ApprenantDepot apprenantDepot,
            PaiementDepot paiementDepot,
            ServicePaiement servicePaiement,
            ServiceGenerationRecu serviceGenerationRecu,
            ApplicationEventPublisher eventPublisher) {
        this.inscriptionDepot      = inscriptionDepot;
        this.formationDepot        = formationDepot;
        this.apprenantDepot        = apprenantDepot;
        this.paiementDepot         = paiementDepot;
        this.servicePaiement       = servicePaiement;
        this.serviceGenerationRecu = serviceGenerationRecu;
        this.eventPublisher        = eventPublisher;
    }

    @PostMapping("/inscrire")
    @Transactional
    public ResponseEntity<?> inscrire(
            @Valid @RequestBody RequeteInscription requete,
            @AuthenticationPrincipal UserDetails utilisateurConnecte) {

        // 1. Récupérer l'apprenant connecté
        Apprenant apprenant = apprenantDepot
                .chercherParEmail(utilisateurConnecte.getUsername())
                .orElseThrow(() -> new RuntimeException("Apprenant introuvable."));

        // 2. Récupérer la formation
        Formation formation = formationDepot
                .findById(requete.getFormationId())
                .orElseThrow(() -> new RuntimeException("Formation introuvable."));

        // 3. Vérifications métier
        if (!"approuve".equals(formation.getStatut())) {
            return ResponseEntity.badRequest()
                    .body(Map.of("erreur", "Cette formation n'est pas disponible à l'inscription."));
        }

        if (formation.getPlacesDisponibles() <= 0) {
            return ResponseEntity.badRequest()
                    .body(Map.of("erreur", "Plus de places disponibles pour cette formation."));
        }

        if (LocalDate.now().isAfter(formation.getDateLimiteInscription())) {
            return ResponseEntity.badRequest()
                    .body(Map.of("erreur", "La date limite d'inscription est dépassée."));
        }

        if (inscriptionDepot.existeDejaInscription(apprenant.getId(), formation.getId())) {
            return ResponseEntity.badRequest()
                    .body(Map.of("erreur", "Vous êtes déjà inscrit à cette formation."));
        }

        // 4. Valider l'opérateur Mobile Money
        if (!servicePaiement.validerOperateurPaiement(requete.getOperateurPaiement())) {
            return ResponseEntity.badRequest()
                    .body(Map.of("erreur", "Opérateur Mobile Money non reconnu. Utilisez : mvola, orange_money ou airtel_money."));
        }

        // 5. Calculer les montants
        Map<String, Long> montants = servicePaiement.calculerPrixInscription(
                formation.getPrix(),
                formation.getPrixRemise(),
                formation.getAbonnementFormateur()
        );

        // 6. Créer l'inscription en statut en_attente
        Inscription inscription = new Inscription();
        inscription.setApprenant(apprenant);
        inscription.setFormation(formation);
        inscription.setStatutPaiement("en_attente");
        inscription.setReservationUniquement(false);
        inscription.setOperateurPaiement(requete.getOperateurPaiement());

        String numeroTransaction = servicePaiement.genererNumeroRecu();
        inscription.setNumeroRecu(numeroTransaction);

        inscriptionDepot.save(inscription);

        // 7. Retourner la réponse avec les montants calculés
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "message",             "Inscription créée. En attente de confirmation du paiement.",
                "inscriptionId",       inscription.getId(),
                "numeroTransaction",   numeroTransaction,
                "prixAffiche",         montants.get("prixAffiche"),
                "apprenantPaie",       montants.get("apprenantPaie"),
                "remise",              montants.get("remise"),
                "commissionEofana",    montants.get("commissionEofana"),
                "formateurRecoit",     montants.get("formateurRecoit"),
                "operateur",           requete.getOperateurPaiement()
        ));
    }

    @PostMapping("/reserver")
    @Transactional
    public ResponseEntity<?> reserver(
            @Valid @RequestBody RequeteReservation requete,
            @AuthenticationPrincipal UserDetails utilisateurConnecte) {

        Apprenant apprenant = apprenantDepot
                .chercherParEmail(utilisateurConnecte.getUsername())
                .orElseThrow(() -> new RuntimeException("Apprenant introuvable."));

        Formation formation = formationDepot
                .findById(requete.getFormationId())
                .orElseThrow(() -> new RuntimeException("Formation introuvable."));

        // Vérifications identiques à l'inscription
        if (!"approuve".equals(formation.getStatut())) {
            return ResponseEntity.badRequest()
                    .body(Map.of("erreur", "Cette formation n'est pas disponible."));
        }

        if (formation.getPlacesDisponibles() <= 0) {
            return ResponseEntity.badRequest()
                    .body(Map.of("erreur", "Plus de places disponibles."));
        }

        if (LocalDate.now().isAfter(formation.getDateLimiteInscription())) {
            return ResponseEntity.badRequest()
                    .body(Map.of("erreur", "La date limite d'inscription est dépassée."));
        }

        if (inscriptionDepot.existeDejaInscription(apprenant.getId(), formation.getId())) {
            return ResponseEntity.badRequest()
                    .body(Map.of("erreur", "Vous avez déjà une inscription ou réservation pour cette formation."));
        }

        if (!servicePaiement.validerOperateurPaiement(requete.getOperateurPaiement())) {
            return ResponseEntity.badRequest()
                    .body(Map.of("erreur", "Opérateur Mobile Money non reconnu."));
        }

        // Calculer uniquement la commission pour la réservation
        long montantReservation = servicePaiement.calculerPrixReservation(
                formation.getPrix(),
                formation.getAbonnementFormateur()
        );

        Inscription reservation = new Inscription();
        reservation.setApprenant(apprenant);
        reservation.setFormation(formation);
        reservation.setStatutPaiement("en_attente");
        reservation.setReservationUniquement(true);
        reservation.setOperateurPaiement(requete.getOperateurPaiement());

        String numeroTransaction = servicePaiement.genererNumeroRecu();
        reservation.setNumeroRecu(numeroTransaction);

        inscriptionDepot.save(reservation);

        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "message",           "Réservation créée. En attente de confirmation du paiement.",
                "inscriptionId",     reservation.getId(),
                "numeroTransaction", numeroTransaction,
                "montantReservation",montantReservation,
                "operateur",         requete.getOperateurPaiement()
        ));
    }

    @GetMapping("/mes-inscriptions")
    public ResponseEntity<?> mesInscriptions(
            @AuthenticationPrincipal UserDetails utilisateurConnecte) {

        Apprenant apprenant = apprenantDepot
                .chercherParEmail(utilisateurConnecte.getUsername())
                .orElseThrow(() -> new RuntimeException("Apprenant introuvable."));

        List<Inscription> inscriptions = inscriptionDepot
                .trouverInscriptionsApprenant(apprenant.getId());

        List<Map<String, Object>> reponse = inscriptions.stream().map(insc -> Map.<String, Object>of(
                "inscriptionId",        insc.getId(),
                "formationTitre",       insc.getFormation().getTitre(),
                "formationLieu",        insc.getFormation().getLieu(),
                "dateDebut",            insc.getFormation().getDateDebut().toString(),
                "statut",               insc.getStatutPaiement(),
                "typeInscription",      insc.isReservationUniquement() ? "reservation" : "complete",
                "numeroRecu",           insc.getNumeroRecu() != null ? insc.getNumeroRecu() : "",
                "dateInscription",      insc.getDateInscription().toString()
        )).toList();

        return ResponseEntity.ok(Map.of(
                "total",        reponse.size(),
                "inscriptions", reponse
        ));
    }

    // ------------------------------------------------------------------
    // GET /api/v1/inscriptions/{inscriptionId}/recu
    // Télécharger le reçu PDF d'une inscription
    // ------------------------------------------------------------------
    @GetMapping("/{inscriptionId}/recu")
    public ResponseEntity<byte[]> telechargerRecu(
            @PathVariable Long inscriptionId,
            @AuthenticationPrincipal UserDetails utilisateurConnecte) {

        Apprenant apprenant = apprenantDepot
                .chercherParEmail(utilisateurConnecte.getUsername())
                .orElseThrow(() -> new RuntimeException("Apprenant introuvable."));

        Inscription inscription = inscriptionDepot.findById(inscriptionId)
                .orElseThrow(() -> new RuntimeException("Inscription introuvable."));

        // Vérifier que l'inscription appartient à l'apprenant connecté
        if (!inscription.getApprenant().getId().equals(apprenant.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        if (!"complete".equals(inscription.getStatutPaiement())
                && !"partiel".equals(inscription.getStatutPaiement())) {
            return ResponseEntity.badRequest().build();
        }

        byte[] pdfOctets = serviceGenerationRecu.genererRecu(inscriptionId);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData(
                "attachment",
                "recu-" + inscription.getNumeroRecu() + ".pdf"
        );

        return ResponseEntity.ok().headers(headers).body(pdfOctets);
    }

    @PostMapping("/{inscriptionId}/confirmer-paiement")
    @Transactional
    public ResponseEntity<?> confirmerPaiement(
            @PathVariable Long inscriptionId,
            @Valid @RequestBody RequeteConfirmationPaiement requete,
            @AuthenticationPrincipal UserDetails utilisateurConnecte) {

        Apprenant apprenant = apprenantDepot
                .chercherParEmail(utilisateurConnecte.getUsername())
                .orElseThrow(() -> new RuntimeException("Apprenant introuvable."));

        Inscription inscription = inscriptionDepot.findById(inscriptionId)
                .orElseThrow(() -> new RuntimeException("Inscription introuvable."));

        // Vérifier que l'inscription appartient à l'apprenant connecté
        if (!inscription.getApprenant().getId().equals(apprenant.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("erreur", "Accès non autorisé à cette inscription."));
        }

        if (!"en_attente".equals(inscription.getStatutPaiement())) {
            return ResponseEntity.badRequest()
                    .body(Map.of("erreur", "Cette inscription ne peut plus être confirmée (statut : "
                            + inscription.getStatutPaiement() + ")."));
        }

        // 1. Créer l'enregistrement Paiement
        Paiement paiement = new Paiement();
        paiement.setInscription(inscription);
        paiement.setMontant(requete.getMontant());
        paiement.setOperateur(inscription.getOperateurPaiement());
        paiement.setIdTransaction(requete.getIdTransaction());
        paiement.setStatut("confirme");
        paiementDepot.save(paiement);

        // 2. Mettre à jour le statut de l'inscription
        String nouveauStatut = inscription.isReservationUniquement() ? "partiel" : "complete";
        inscription.setStatutPaiement(nouveauStatut);
        inscriptionDepot.save(inscription);

        // 3. Décrémenter les places disponibles de la formation
        Formation formation = inscription.getFormation();
        formation.setPlacesDisponibles(formation.getPlacesDisponibles() - 1);
        formationDepot.save(formation);

        // 4. Publier un événement pour le service de notification SMS/email
        // Le ServiceNotification écoute cet événement (EventListener)
        eventPublisher.publishEvent(
                new EvenementInscriptionConfirmee(this, inscription.getId())
        );

        return ResponseEntity.ok(Map.of(
                "message",         "Paiement confirmé avec succès.",
                "inscriptionId",   inscription.getId(),
                "statut",          nouveauStatut,
                "numeroRecu",      inscription.getNumeroRecu(),
                "idTransaction",   requete.getIdTransaction(),
                "montantConfirme", requete.getMontant()
        ));
    }

    public static class EvenementInscriptionConfirmee
            extends org.springframework.context.ApplicationEvent {
        private final Long inscriptionId;

        public EvenementInscriptionConfirmee(Object source, Long inscriptionId) {
            super(source);
            this.inscriptionId = inscriptionId;
        }

        public Long getInscriptionId() {
            return inscriptionId;
        }
    }
}
