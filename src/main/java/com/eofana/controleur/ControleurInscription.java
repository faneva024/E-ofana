package com.eofana.controleur;

import com.eofana.depot.InscriptionDepot;
import com.eofana.depot.PaiementDepot;
import com.eofana.depot.SessionFormationDepot;
import com.eofana.depot.UtilisateurDepot;
import com.eofana.dto.reponse.ReponseInscription;
import com.eofana.dto.requete.RequeteConfirmationPaiement;
import com.eofana.dto.requete.RequeteInscription;
import com.eofana.dto.requete.RequeteReservation;
import com.eofana.entite.Inscription;
import com.eofana.entite.Paiement;
import com.eofana.entite.SessionFormation;
import com.eofana.entite.Utilisateur;
import com.eofana.service.ServiceGenerationRecu;

import jakarta.validation.Valid;
import org.springframework.context.ApplicationEvent;
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

/**
 * ControleurInscription — T-B-008
 * Branche : back → part-Rolph
 *
 * Gère les inscriptions et réservations des apprenants.
 * Référence BDD v3.0 :
 *   - table "inscriptions"  : idUser, idSession, typeInsc, statut, montants…
 *   - table "paiements"     : idInscription, montant, operateur, numeroTransaction
 *   - table "utilisateurs"  : idUser (remplace apprenant_id)
 *   - table "sessionsFormation" : idSession (remplace formation_id)
 *
 * Les montants (remise, commission, montantFormateur) sont calculés
 * automatiquement par le trigger PostgreSQL fnCalculerCommission.
 * Toutes les routes sont protégées par JWT sauf indication contraire.
 */
@RestController
@RequestMapping("/api/v1/inscriptions")
public class ControleurInscription {

    private final InscriptionDepot      inscriptionDepot;
    private final SessionFormationDepot sessionDepot;
    private final UtilisateurDepot      utilisateurDepot;
    private final PaiementDepot         paiementDepot;
    private final ServiceGenerationRecu serviceGenerationRecu;
    private final ApplicationEventPublisher eventPublisher;

    public ControleurInscription(InscriptionDepot inscriptionDepot,
                                  SessionFormationDepot sessionDepot,
                                  UtilisateurDepot utilisateurDepot,
                                  PaiementDepot paiementDepot,
                                  ServiceGenerationRecu serviceGenerationRecu,
                                  ApplicationEventPublisher eventPublisher) {
        this.inscriptionDepot      = inscriptionDepot;
        this.sessionDepot          = sessionDepot;
        this.utilisateurDepot      = utilisateurDepot;
        this.paiementDepot         = paiementDepot;
        this.serviceGenerationRecu = serviceGenerationRecu;
        this.eventPublisher        = eventPublisher;
    }

    // ── Utilitaire : récupérer l'utilisateur connecté ───────────────────────
    private Utilisateur getUtilisateurConnecte(UserDetails ud) {
        return utilisateurDepot.findByEmail(ud.getUsername())
            .orElseThrow(() -> new RuntimeException("Utilisateur introuvable."));
    }

    // ── Utilitaire : récupérer une session ou lever une erreur ───────────────
    private SessionFormation getSession(Long idSession) {
        return sessionDepot.findById(idSession)
            .orElseThrow(() -> new RuntimeException("Session introuvable."));
    }

    // ── Vérifications métier communes ────────────────────────────────────────
    private ResponseEntity<?> verifierSession(Utilisateur utilisateur,
                                               SessionFormation session) {
        if (!session.estDisponible()) {
            return ResponseEntity.badRequest()
                .body(Map.of("erreur", "Cette session n'est plus disponible à l'inscription."));
        }
        if (LocalDate.now().isAfter(session.getDateLimiteInscription())) {
            return ResponseEntity.badRequest()
                .body(Map.of("erreur", "La date limite d'inscription est dépassée."));
        }
        if (inscriptionDepot.existsByUtilisateur_IdUserAndSession_IdSessionAndStatutNot(
                utilisateur.getIdUser(),
                session.getIdSession(),
                Inscription.StatutInscription.annule)) {
            return ResponseEntity.badRequest()
                .body(Map.of("erreur", "Vous avez déjà une inscription ou réservation pour cette session."));
        }
        return null; // pas d'erreur
    }

    // ════════════════════════════════════════════════════════════════════════
    // POST /api/v1/inscriptions/inscrire
    // Inscrire un apprenant à une session (paiement complet, remise 5%)
    // ════════════════════════════════════════════════════════════════════════
    @PostMapping("/inscrire")
    @Transactional
    public ResponseEntity<?> inscrire(
            @Valid @RequestBody RequeteInscription requete,
            @AuthenticationPrincipal UserDetails ud) {

        Utilisateur utilisateur = getUtilisateurConnecte(ud);
        SessionFormation session = getSession(requete.getIdSession());

        // Vérifications métier
        ResponseEntity<?> erreur = verifierSession(utilisateur, session);
        if (erreur != null) return erreur;

        // Créer l'inscription — les montants sont calculés par le trigger BDD
        Inscription inscription = new Inscription(
            utilisateur,
            session,
            Inscription.TypeInscription.inscription,
            Inscription.Operateur.valueOf(requete.getOperateur())
        );
        inscription.setStatut(Inscription.StatutInscription.enAttente);
        inscription = inscriptionDepot.save(inscription);

        return ResponseEntity.status(HttpStatus.CREATED).body(new ReponseInscription(
            inscription.getIdInscription(),
            "inscription",
            inscription.getStatut().name(),
            inscription.getNumeroRecu(),
            inscription.getMontantPaye(),
            inscription.getRemise(),
            inscription.getCommission(),
            inscription.getMontantFormateur(),
            requete.getOperateur(),
            "Inscription créée. En attente de confirmation du paiement Mobile Money."
        ));
    }

    // ════════════════════════════════════════════════════════════════════════
    // POST /api/v1/inscriptions/reserver
    // Réserver une place (paiement de la commission seule : 7% ou 15%)
    // ════════════════════════════════════════════════════════════════════════
    @PostMapping("/reserver")
    @Transactional
    public ResponseEntity<?> reserver(
            @Valid @RequestBody RequeteReservation requete,
            @AuthenticationPrincipal UserDetails ud) {

        Utilisateur utilisateur = getUtilisateurConnecte(ud);
        SessionFormation session = getSession(requete.getIdSession());

        ResponseEntity<?> erreur = verifierSession(utilisateur, session);
        if (erreur != null) return erreur;

        // Créer la réservation — typeInsc = 'reservation'
        Inscription reservation = new Inscription(
            utilisateur,
            session,
            Inscription.TypeInscription.reservation,
            Inscription.Operateur.valueOf(requete.getOperateur())
        );
        reservation.setStatut(Inscription.StatutInscription.enAttente);
        reservation = inscriptionDepot.save(reservation);

        return ResponseEntity.status(HttpStatus.CREATED).body(new ReponseInscription(
            reservation.getIdInscription(),
            "reservation",
            reservation.getStatut().name(),
            reservation.getNumeroRecu(),
            reservation.getMontantPaye(),
            reservation.getRemise(),
            reservation.getCommission(),
            reservation.getMontantFormateur(),
            requete.getOperateur(),
            "Réservation créée. Montant de la commission à payer pour sécuriser votre place."
        ));
    }

    // ════════════════════════════════════════════════════════════════════════
    // GET /api/v1/inscriptions/mes-inscriptions
    // Récupérer toutes les inscriptions de l'apprenant connecté
    // ════════════════════════════════════════════════════════════════════════
    @GetMapping("/mes-inscriptions")
    @Transactional(readOnly = true)
    public ResponseEntity<?> mesInscriptions(@AuthenticationPrincipal UserDetails ud) {

        Utilisateur utilisateur = getUtilisateurConnecte(ud);
        List<Inscription> inscriptions =
            inscriptionDepot.trouverInscriptionsApprenant(utilisateur.getIdUser());

        List<Map<String, Object>> liste = inscriptions.stream().map(i -> Map.<String, Object>of(
            "idInscription",   i.getIdInscription(),
            "typeInscription", i.getTypeInsc().name(),
            "statut",          i.getStatut().name(),
            "numeroRecu",      i.getNumeroRecu() != null ? i.getNumeroRecu() : "",
            "montantPaye",     i.getMontantPaye(),
            "remise",          i.getRemise(),
            "commission",      i.getCommission(),
            "operateur",       i.getOperateur() != null ? i.getOperateur().name() : "",
            "dateInscription", i.getCreatedAt().toString(),
            "session", Map.of(
                "idSession",   i.getSession().getIdSession(),
                "dateDebut",   i.getSession().getDateDebut().toString(),
                "dateFin",     i.getSession().getDateFin() != null
                                   ? i.getSession().getDateFin().toString() : "",
                "formation",   i.getSession().getFormation().getTitre(),
                "lieu",        i.getSession().getFormation().getLieu() != null
                                   ? i.getSession().getFormation().getLieu() : ""
            )
        )).toList();

        return ResponseEntity.ok(Map.of(
            "total",        liste.size(),
            "inscriptions", liste
        ));
    }

    // ════════════════════════════════════════════════════════════════════════
    // GET /api/v1/inscriptions/{idInscription}/recu
    // Télécharger le reçu PDF d'une inscription
    // ════════════════════════════════════════════════════════════════════════
    @GetMapping("/{idInscription}/recu")
    public ResponseEntity<byte[]> telechargerRecu(
            @PathVariable Long idInscription,
            @AuthenticationPrincipal UserDetails ud) {

        Utilisateur utilisateur = getUtilisateurConnecte(ud);
        Inscription inscription = inscriptionDepot.findById(idInscription)
            .orElseThrow(() -> new RuntimeException("Inscription introuvable."));

        // Vérifier que l'inscription appartient à l'utilisateur connecté
        if (!inscription.getUtilisateur().getIdUser().equals(utilisateur.getIdUser())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        if (inscription.getStatut() == Inscription.StatutInscription.enAttente
         || inscription.getStatut() == Inscription.StatutInscription.annule) {
            return ResponseEntity.badRequest().build();
        }

        byte[] pdfOctets = serviceGenerationRecu.genererRecu(idInscription);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData(
            "attachment",
            "recu-" + inscription.getNumeroRecu() + ".pdf"
        );

        return ResponseEntity.ok().headers(headers).body(pdfOctets);
    }

    // ════════════════════════════════════════════════════════════════════════
    // POST /api/v1/inscriptions/{idInscription}/confirmer-paiement
    // Confirmer la réception du paiement Mobile Money et valider l'inscription
    // ════════════════════════════════════════════════════════════════════════
    @PostMapping("/{idInscription}/confirmer-paiement")
    @Transactional
    public ResponseEntity<?> confirmerPaiement(
            @PathVariable Long idInscription,
            @Valid @RequestBody RequeteConfirmationPaiement requete,
            @AuthenticationPrincipal UserDetails ud) {

        Utilisateur utilisateur = getUtilisateurConnecte(ud);
        Inscription inscription = inscriptionDepot.findById(idInscription)
            .orElseThrow(() -> new RuntimeException("Inscription introuvable."));

        // Vérifier appartenance
        if (!inscription.getUtilisateur().getIdUser().equals(utilisateur.getIdUser())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(Map.of("erreur", "Accès non autorisé à cette inscription."));
        }

        // Vérifier que l'inscription est bien en attente
        if (inscription.getStatut() != Inscription.StatutInscription.enAttente) {
            return ResponseEntity.badRequest().body(Map.of(
                "erreur", "Cette inscription ne peut plus être confirmée (statut actuel : "
                          + inscription.getStatut().name() + ")."
            ));
        }

        // Vérifier unicité du numéro de transaction
        if (paiementDepot.existsByNumeroTransaction(requete.getNumeroTransaction())) {
            return ResponseEntity.badRequest()
                .body(Map.of("erreur", "Ce numéro de transaction a déjà été enregistré."));
        }

        // 1. Enregistrer le paiement dans la table "paiements"
        Paiement paiement = new Paiement(
            inscription,
            requete.getMontant(),
            inscription.getOperateur(),
            requete.getNumeroTransaction()
        );
        paiement.setStatut(Paiement.StatutPaiement.confirme);
        paiementDepot.save(paiement);

        // 2. Valider l'inscription — le trigger trgPlacesInscriptionUpdate
        //    décrémente automatiquement placesRestantes dans sessionsFormation
        inscription.setStatut(Inscription.StatutInscription.valide);
        inscription.setTransactionId(requete.getNumeroTransaction());
        inscriptionDepot.save(inscription);

        // 3. Publier un événement pour déclencher la notification SMS/email
        //    (ServiceNotification écoute cet événement via @EventListener)
        eventPublisher.publishEvent(
            new EvenementInscriptionConfirmee(this, inscription.getIdInscription())
        );

        return ResponseEntity.ok(Map.of(
            "message",           "Paiement confirmé. Votre inscription est validée.",
            "idInscription",     inscription.getIdInscription(),
            "statut",            inscription.getStatut().name(),
            "numeroRecu",        inscription.getNumeroRecu(),
            "numeroTransaction", requete.getNumeroTransaction(),
            "montantConfirme",   requete.getMontant()
        ));
    }

    // ── Événement interne publié après confirmation du paiement ─────────────
    public static class EvenementInscriptionConfirmee extends ApplicationEvent {
        private final Long idInscription;

        public EvenementInscriptionConfirmee(Object source, Long idInscription) {
            super(source);
            this.idInscription = idInscription;
        }

        public Long getIdInscription() { return idInscription; }
    }
}
