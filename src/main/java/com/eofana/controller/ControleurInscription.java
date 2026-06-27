package com.eofana.controller;

import com.eofana.dto.ConfirmationPaiementRequete;
import com.eofana.dto.InscriptionRequete;
import com.eofana.dto.InscriptionReponse;
import com.eofana.entity.Formation;
import com.eofana.entity.Inscription;
import com.eofana.entity.Paiement;
import com.eofana.entity.Utilisateur;
import com.eofana.repository.FormationRepository;
import com.eofana.repository.InscriptionRepository;
import com.eofana.repository.PaiementRepository;
import com.eofana.repository.UtilisateurRepository;
import com.eofana.service.ServiceGenerationRecu;
import com.eofana.service.ServicePaiement;
import com.eofana.service.ServiceUtilisateur;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/inscriptions")
@CrossOrigin(origins = "*")
public class ControleurInscription {

    private final InscriptionRepository inscriptionRepository;
    private final FormationRepository formationRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final PaiementRepository paiementRepository;
    private final ServicePaiement servicePaiement;
    private final ServiceGenerationRecu serviceGenerationRecu;
    private final ServiceUtilisateur serviceUtilisateur;

    public ControleurInscription(InscriptionRepository inscriptionRepository,
                                  FormationRepository formationRepository,
                                  UtilisateurRepository utilisateurRepository,
                                  PaiementRepository paiementRepository,
                                  ServicePaiement servicePaiement,
                                  ServiceGenerationRecu serviceGenerationRecu,
                                  ServiceUtilisateur serviceUtilisateur) {
        this.inscriptionRepository = inscriptionRepository;
        this.formationRepository = formationRepository;
        this.utilisateurRepository = utilisateurRepository;
        this.paiementRepository = paiementRepository;
        this.servicePaiement = servicePaiement;
        this.serviceGenerationRecu = serviceGenerationRecu;
        this.serviceUtilisateur = serviceUtilisateur;
    }

    @PostMapping("/inscrire")
    @Transactional
    public ResponseEntity<?> inscrire(@Valid @RequestBody InscriptionRequete requete,
                                       @AuthenticationPrincipal UserDetails userDetails) {
        return creerInscriptionOuReservation(requete, userDetails, false);
    }

    @PostMapping("/reserver")
    @Transactional
    public ResponseEntity<?> reserver(@Valid @RequestBody InscriptionRequete requete,
                                       @AuthenticationPrincipal UserDetails userDetails) {
        return creerInscriptionOuReservation(requete, userDetails, true);
    }

    private ResponseEntity<?> creerInscriptionOuReservation(InscriptionRequete requete,
                                                              UserDetails userDetails,
                                                              boolean reservationUniquement) {
        if (!servicePaiement.validerOperateurPaiement(requete.getOperateurPaiement())) {
            return ResponseEntity.badRequest()
                    .body(Map.of("erreur", "Opérateur de paiement invalide. Utilisez : Mvola, OrangeMoney, AirtelMoney"));
        }

        Formation formation = formationRepository.findById(requete.getIdFormation())
                .orElse(null);
        if (formation == null || !"approuve".equals(formation.getStatut())) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("erreur", "Formation introuvable ou non disponible"));
        }

        if (formation.getPlacesDisponibles() <= 0) {
            return ResponseEntity.badRequest()
                    .body(Map.of("erreur", "Plus de places disponibles pour cette formation"));
        }

        if (formation.getDateLimiteInscription() != null &&
                LocalDateTime.now().isAfter(formation.getDateLimiteInscription())) {
            return ResponseEntity.badRequest()
                    .body(Map.of("erreur", "La date limite d'inscription est dépassée"));
        }

        Utilisateur utilisateur = serviceUtilisateur.chercherParEmail(userDetails.getUsername());

        Inscription inscription = new Inscription();
        inscription.setUtilisateur(utilisateur);
        inscription.setFormation(formation);
        inscription.setReservationUniquement(reservationUniquement);
        inscription.setOperateurPaiement(requete.getOperateurPaiement());
        inscription.setStatutPaiement("enAttente");
        inscription.setNumeroRecu(servicePaiement.genererNumeroRecu());

        Inscription saved = inscriptionRepository.save(inscription);

        BigDecimal montant;
        if (reservationUniquement) {
            montant = servicePaiement.calculerPrixReservation(formation.getPrix(), false);
        } else {
            montant = servicePaiement.calculerPrixInscription(formation.getPrix()).get("apprenantPaie");
        }

        InscriptionReponse reponse = new InscriptionReponse();
        reponse.setIdInscription(saved.getIdInscription());
        reponse.setMontantAPayer(montant);
        reponse.setIdTransactionProvisoire(servicePaiement.genererIdTransaction());
        reponse.setNumeroRecu(saved.getNumeroRecu());
        reponse.setMessage(reservationUniquement ? "Réservation créée avec succès" : "Inscription créée avec succès");

        return ResponseEntity.status(HttpStatus.CREATED).body(reponse);
    }

    @GetMapping("/mes-inscriptions")
    public ResponseEntity<List<Inscription>> mesInscriptions(@AuthenticationPrincipal UserDetails userDetails) {
        Utilisateur utilisateur = serviceUtilisateur.chercherParEmail(userDetails.getUsername());
        List<Inscription> inscriptions = inscriptionRepository.findByUtilisateurIdUser(utilisateur.getIdUser());
        return ResponseEntity.ok(inscriptions);
    }

    @GetMapping("/{idInscription}/recu")
    public ResponseEntity<byte[]> genererRecu(@PathVariable Long idInscription,
                                               @AuthenticationPrincipal UserDetails userDetails) {
        Utilisateur utilisateur = serviceUtilisateur.chercherParEmail(userDetails.getUsername());
        Inscription inscription = inscriptionRepository
                .findByIdInscriptionAndUtilisateurIdUser(idInscription, utilisateur.getIdUser())
                .orElse(null);

        if (inscription == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        byte[] pdf = serviceGenerationRecu.genererRecu(idInscription);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "recu-" + inscription.getNumeroRecu() + ".pdf");
        return ResponseEntity.ok().headers(headers).body(pdf);
    }

    @PostMapping("/{idInscription}/confirmer-paiement")
    @Transactional
    public ResponseEntity<?> confirmerPaiement(@PathVariable Long idInscription,
                                                @Valid @RequestBody ConfirmationPaiementRequete requete,
                                                @AuthenticationPrincipal UserDetails userDetails) {
        Utilisateur utilisateur = serviceUtilisateur.chercherParEmail(userDetails.getUsername());
        Inscription inscription = inscriptionRepository
                .findByIdInscriptionAndUtilisateurIdUser(idInscription, utilisateur.getIdUser())
                .orElse(null);

        if (inscription == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        // Créer paiement
        Paiement paiement = new Paiement();
        paiement.setInscription(inscription);
        paiement.setMontant(requete.getMontant());
        paiement.setOperateur(inscription.getOperateurPaiement());
        paiement.setIdTransaction(requete.getIdTransaction());
        paiement.setStatut("complete");
        paiementRepository.save(paiement);

        // Mettre à jour inscription
        inscription.setStatutPaiement("complete");
        inscriptionRepository.save(inscription);

        // Décrémenter places
        Formation formation = inscription.getFormation();
        formation.setPlacesDisponibles(formation.getPlacesDisponibles() - 1);
        formationRepository.save(formation);

        return ResponseEntity.ok(Map.of(
                "message", "Paiement confirmé avec succès",
                "idTransaction", requete.getIdTransaction(),
                "numeroRecu", inscription.getNumeroRecu()
        ));
    }
}
