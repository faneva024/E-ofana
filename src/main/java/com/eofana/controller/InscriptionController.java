package com.eofana.controller;

import com.eofana.dto.ConfirmationPaymentRequest;
import com.eofana.dto.EnrollRequest;
import com.eofana.dto.EnrollResponse;
import com.eofana.dto.MyEnrollmentResponse;
import com.eofana.entity.Inscription;
import com.eofana.entity.Paiement;
import com.eofana.entity.SessionFormation;
import com.eofana.entity.Utilisateur;
import com.eofana.enums.OperateurMm;
import com.eofana.enums.StatutInscription;
import com.eofana.enums.StatutPaiement;
import com.eofana.enums.TypeInscription;
import com.eofana.repository.InscriptionRepository;
import com.eofana.repository.PaiementRepository;
import com.eofana.repository.SessionFormationRepository;
import com.eofana.repository.UtilisateurRepository;
import com.eofana.security.JwtAuthenticationFilter.AuthenticatedUser;
import com.eofana.service.PaymentService;
import com.eofana.service.ReceiptGenerationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 * Contrôleur d'inscription et de réservation (T-B-011).
 *
 * Réécrit entièrement en JPA : la version précédente accédait à la
 * base en SQL brut via JdbcTemplate, ne distinguait pas inscription
 * et réservation, et créait elle-même une SessionFormation "au
 * hasard" (dates codées en dur, +7 jours) si aucune session
 * n'existait pour la formation demandée — ce qui n'a pas de sens
 * métier : c'est au formateur de créer ses sessions (module Semaine
 * 2), l'apprenant choisit une session existante.
 *
 * L'identifiant de l'apprenant est désormais lu dans le JWT
 * (@AuthenticationPrincipal), jamais accepté depuis le corps de la
 * requête : un client ne doit pas pouvoir s'inscrire "à la place"
 * de quelqu'un d'autre en changeant un idUser dans le JSON envoyé.
 */
@RestController
@RequestMapping("/api/v1/inscriptions")
@CrossOrigin(origins = {
        "http://localhost:5173",
        "http://localhost:5174"
})
public class InscriptionController {

    private final InscriptionRepository inscriptionRepository;
    private final PaiementRepository paiementRepository;
    private final SessionFormationRepository sessionFormationRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final PaymentService paymentService;
    private final ReceiptGenerationService receiptGenerationService;

    public InscriptionController(InscriptionRepository inscriptionRepository,
                                  PaiementRepository paiementRepository,
                                  SessionFormationRepository sessionFormationRepository,
                                  UtilisateurRepository utilisateurRepository,
                                  PaymentService paymentService,
                                  ReceiptGenerationService receiptGenerationService) {
        this.inscriptionRepository = inscriptionRepository;
        this.paiementRepository = paiementRepository;
        this.sessionFormationRepository = sessionFormationRepository;
        this.utilisateurRepository = utilisateurRepository;
        this.paymentService = paymentService;
        this.receiptGenerationService = receiptGenerationService;
    }

    @PostMapping("/inscrire")
    @Transactional
    public ResponseEntity<?> enroll(@Valid @RequestBody EnrollRequest request,
                                     @AuthenticationPrincipal AuthenticatedUser principal) {
        return creerInscription(request, principal, TypeInscription.inscription);
    }

    @PostMapping("/reserver")
    @Transactional
    public ResponseEntity<?> reserve(@Valid @RequestBody EnrollRequest request,
                                      @AuthenticationPrincipal AuthenticatedUser principal) {
        return creerInscription(request, principal, TypeInscription.reservation);
    }

    private ResponseEntity<?> creerInscription(EnrollRequest request, AuthenticatedUser principal, TypeInscription typeInsc) {
        Utilisateur utilisateur = utilisateurRepository.findById(principal.idUser())
                .orElseThrow(() -> new NoSuchElementException("Utilisateur introuvable"));

        SessionFormation session = sessionFormationRepository.findById(request.idSession())
                .orElseThrow(() -> new NoSuchElementException("Session introuvable"));

        if (inscriptionRepository.existsByUtilisateur_IdUserAndSession_IdSession(utilisateur.getIdUser(), session.getIdSession())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(
                    "message", "Vous êtes déjà inscrit (ou avez déjà réservé) à cette session"
            ));
        }

        if (session.getPlacesRestantes() == null || session.getPlacesRestantes() <= 0) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(
                    "message", "Il n'y a plus de places disponibles pour cette session"
            ));
        }

        long prixNormal = session.getFormation().getPrix();
        var taux = session.getFormation().getCentre().getTauxCommission();

        long prixApresRemise = paymentService.calculateDiscountedPrice(prixNormal);
        long remise = prixNormal - prixApresRemise;
        long commission = paymentService.calculateCommissionFee(prixApresRemise, taux);
        long montantFormateur = paymentService.calculateTrainerShare(prixApresRemise, commission);

        // Montant à encaisser MAINTENANT (informatif pour le frontend) :
        // le total pour une inscription complète, seulement la
        // commission (l'acompte) pour une réservation — le solde sera
        // réglé via confirmer-paiement, cf. cahier des charges §4.6.
        // Aucun Paiement n'est créé à ce stade : cette étape se limite
        // à réserver la place logiquement (statut enAttente) ; le
        // paiement Mobile Money est confirmé dans un second appel.
        long montantAPayerMaintenant = (typeInsc == TypeInscription.inscription) ? prixApresRemise : commission;

        Inscription inscription = Inscription.builder()
                .utilisateur(utilisateur)
                .session(session)
                .typeInsc(typeInsc)
                .statut(StatutInscription.enAttente)
                .montantPaye(prixApresRemise)
                .remise(remise)
                .commission(commission)
                .montantFormateur(montantFormateur)
                .numeroRecu(genererNumeroRecu())
                .build();

        Inscription saved = inscriptionRepository.save(inscription);

        return ResponseEntity.ok(new EnrollResponse(
                saved.getIdInscription(),
                saved.getTypeInsc().name(),
                saved.getStatut().name(),
                saved.getMontantPaye(),
                montantAPayerMaintenant,
                saved.getRemise(),
                saved.getCommission(),
                saved.getNumeroRecu()
        ));
    }

    @GetMapping("/mes-inscriptions")
    public ResponseEntity<List<MyEnrollmentResponse>> getMyEnrollments(@AuthenticationPrincipal AuthenticatedUser principal) {
        List<Inscription> inscriptions = inscriptionRepository
                .findByUtilisateur_IdUserOrderByCreatedAtDesc(principal.idUser());

        List<MyEnrollmentResponse> response = inscriptions.stream()
                .map(i -> new MyEnrollmentResponse(
                        i.getIdInscription(),
                        i.getSession().getFormation().getTitre(),
                        i.getSession().getFormation().getCentre().getNom(),
                        i.getTypeInsc().name(),
                        i.getStatut().name(),
                        i.getMontantPaye(),
                        i.getNumeroRecu(),
                        i.getCreatedAt()
                ))
                .toList();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{idInscription}/recu")
    public ResponseEntity<?> getReceipt(@PathVariable Long idInscription,
                                         @AuthenticationPrincipal AuthenticatedUser principal) {
        Inscription inscription = inscriptionRepository.findById(idInscription)
                .orElseThrow(() -> new NoSuchElementException("Inscription introuvable"));

        if (!inscription.getUtilisateur().getIdUser().equals(principal.idUser())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of(
                    "message", "Ce reçu ne vous appartient pas"
            ));
        }

        byte[] pdf = receiptGenerationService.generateReceipt(idInscription);

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"recu-" + idInscription + ".pdf\"")
                .body(pdf);
    }

    @PostMapping("/{idInscription}/confirmer-paiement")
    @Transactional
    public ResponseEntity<?> confirmPayment(@PathVariable Long idInscription,
                                             @Valid @RequestBody ConfirmationPaymentRequest request) {
        Inscription inscription = inscriptionRepository.findById(idInscription)
                .orElseThrow(() -> new NoSuchElementException("Inscription introuvable"));

        if (!paymentService.isValidOperator(request.operateur())) {
            return ResponseEntity.badRequest().body(Map.of(
                    "message", "Opérateur Mobile Money invalide"
            ));
        }

        // Une réservation peut avoir plusieurs paiements (acompte, puis
        // solde) : on ne bloque donc jamais un nouveau paiement tant
        // que le total encaissé n'atteint pas montantPaye.
        long dejaEncaisse = paiementRepository.findByInscription_IdInscriptionOrderByCreatedAtAsc(idInscription)
                .stream()
                .filter(p -> p.getStatut() == StatutPaiement.confirme)
                .mapToLong(Paiement::getMontant)
                .sum();

        if (dejaEncaisse >= inscription.getMontantPaye()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(
                    "message", "Cette inscription est déjà intégralement payée"
            ));
        }

        Paiement paiement = Paiement.builder()
                .inscription(inscription)
                .montant(request.montant())
                .operateur(OperateurMm.valueOf(request.operateur().trim().toLowerCase()))
                .numeroTransaction(numeroTransactionOuGenere(request.numeroTransaction()))
                .statut(StatutPaiement.confirme)
                .build();

        paiementRepository.save(paiement);

        long totalEncaisse = dejaEncaisse + request.montant();

        if (totalEncaisse >= inscription.getMontantPaye()) {
            inscription.setStatut(StatutInscription.valide);
            inscription.setOperateur(paiement.getOperateur());
            inscription.setTransactionId(paiement.getNumeroTransaction());

            // Le trigger fnGererPlaces n'existant pas dans les migrations
            // réellement déployées, la décrémentation des places se fait
            // ici, dans la même transaction que le passage à "valide".
            SessionFormation session = inscription.getSession();
            session.setPlacesRestantes(session.getPlacesRestantes() - 1);
            sessionFormationRepository.save(session);

            inscriptionRepository.save(inscription);
        }

        return ResponseEntity.ok(Map.of(
                "message", totalEncaisse >= inscription.getMontantPaye()
                        ? "Paiement confirmé, inscription validée"
                        : "Acompte confirmé, solde restant à régler",
                "idInscription", inscription.getIdInscription(),
                "statut", inscription.getStatut().name(),
                "totalEncaisse", totalEncaisse,
                "montantDu", inscription.getMontantPaye()
        ));
    }

    private String numeroTransactionOuGenere(String numeroTransaction) {
        if (numeroTransaction != null && !numeroTransaction.isBlank()) {
            return numeroTransaction.trim();
        }
        return "TXN-" + OffsetDateTime.now().toEpochSecond() + "-" + (int) (Math.random() * 90000 + 10000);
    }

    private String genererNumeroRecu() {
        return "RECU-" + OffsetDateTime.now().toEpochSecond() + "-" + (int) (Math.random() * 90000 + 10000);
    }
}
