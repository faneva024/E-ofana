package com.eofana.controller;

import com.eofana.entity.Centre;
import com.eofana.entity.DemandeModificationCentre;
import com.eofana.entity.Utilisateur;
import com.eofana.enums.StatutModification;
import com.eofana.enums.TypeNotification;
import com.eofana.repository.CentreRepository;
import com.eofana.repository.DemandeModificationCentreRepository;
import com.eofana.security.AdminContextResolver;
import com.eofana.service.NotificationService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * T-B-206 : approbation/rejet des demandes de modification de profil
 * centre soumises par les formateurs (T-B-103/T-B-107). Les
 * changements proposés (JSON libre dans "donneesProposees") ne sont
 * JAMAIS appliqués sur Centre avant une approbation explicite de
 * l'admin — critère d'acceptation du ticket.
 */
@RestController
@RequestMapping("/api/v1/admin/profils/demandes")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:5174"})
public class ProfileValidationController {

    /** Clés reconnues du JSON "donneesProposees" et appliquées sur Centre à l'approbation. */
    private static final List<String> CHAMPS_MODIFIABLES = List.of(
            "nom", "logo", "description", "services", "ville", "adresse",
            "telephone", "email", "siteWeb", "fourchettePrixMin", "fourchettePrixMax",
            "mobileMoneyNumero"
    );

    private final DemandeModificationCentreRepository demandeRepository;
    private final CentreRepository centreRepository;
    private final AdminContextResolver adminContextResolver;
    private final NotificationService notificationService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Liste des demandes en attente, incluant le diff avant (état
     * actuel du centre) / après (donneesProposees telles que soumises).
     */
    @GetMapping
    @Transactional(readOnly = true)
    public ResponseEntity<?> getPendingProfileRequests(
            @RequestHeader(value = "Authorization", required = false) String authorization) {

        try {
            adminContextResolver.resoudreAdmin(authorization);
        } catch (AdminContextResolver.AccesNonAutoriseException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", e.getMessage()));
        }

        List<DemandeModificationCentre> demandes =
                demandeRepository.findByStatutOrderByCreatedAtAsc(StatutModification.enAttente);

        List<Map<String, Object>> reponse = demandes.stream().map(this::toDiffSummary).toList();
        return ResponseEntity.ok(reponse);
    }

    /**
     * Applique les changements proposés sur Centre, statut → APPROUVE.
     * Les champs non reconnus dans "donneesProposees" sont ignorés
     * (log silencieux) plutôt que de faire échouer toute la demande.
     */
    @PutMapping("/{id}/approuver")
    @Transactional
    public ResponseEntity<?> approveProfileRequest(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @PathVariable Long id) {

        Utilisateur admin;
        try {
            admin = adminContextResolver.resoudreAdmin(authorization);
        } catch (AdminContextResolver.AccesNonAutoriseException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", e.getMessage()));
        }

        DemandeModificationCentre demande = demandeRepository.findById(id).orElse(null);
        if (demande == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "Demande introuvable"));
        }
        if (demande.getStatut() != StatutModification.enAttente) {
            return ResponseEntity.badRequest().body(Map.of("message", "Cette demande a déjà été traitée"));
        }

        Centre centre = demande.getCentre();
        try {
            appliquerChangements(centre, demande.getDonneesProposees());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", "donneesProposees invalide : " + e.getMessage()));
        }
        centreRepository.save(centre);

        demande.setStatut(StatutModification.approuve);
        demande.setTraitePar(admin);
        demande.setTraiteAt(OffsetDateTime.now());
        demandeRepository.save(demande);

        notifierFormateur(centre, TypeNotification.profilApprouve,
                "Votre demande de modification de profil a été approuvée",
                "Les changements proposés pour \"" + centre.getNom() + "\" ont été appliqués à votre profil.");

        return ResponseEntity.ok(Map.of("message", "Demande approuvée, profil mis à jour", "demande", toDiffSummary(demande)));
    }

    /**
     * Rejette la demande — statut REJETE, commentaire admin
     * obligatoire, AUCUNE modification appliquée sur Centre.
     */
    @PutMapping("/{id}/rejeter")
    @Transactional
    public ResponseEntity<?> rejectProfileRequest(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @PathVariable Long id,
            @RequestBody RejectRequest request) {

        if (request.commentaireAdmin() == null || request.commentaireAdmin().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Le commentaire admin est obligatoire en cas de rejet"));
        }

        Utilisateur admin;
        try {
            admin = adminContextResolver.resoudreAdmin(authorization);
        } catch (AdminContextResolver.AccesNonAutoriseException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", e.getMessage()));
        }

        DemandeModificationCentre demande = demandeRepository.findById(id).orElse(null);
        if (demande == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "Demande introuvable"));
        }
        if (demande.getStatut() != StatutModification.enAttente) {
            return ResponseEntity.badRequest().body(Map.of("message", "Cette demande a déjà été traitée"));
        }

        demande.setStatut(StatutModification.rejete);
        demande.setCommentaireAdmin(request.commentaireAdmin());
        demande.setTraitePar(admin);
        demande.setTraiteAt(OffsetDateTime.now());
        demandeRepository.save(demande);

        notifierFormateur(demande.getCentre(), TypeNotification.profilRejete,
                "Votre demande de modification de profil a été rejetée",
                "Votre demande pour \"" + demande.getCentre().getNom() + "\" a été rejetée. Motif : " + request.commentaireAdmin());

        return ResponseEntity.ok(Map.of("message", "Demande rejetée, aucune modification appliquée", "demande", toDiffSummary(demande)));
    }

    @SuppressWarnings("unchecked")
    private void appliquerChangements(Centre centre, String donneesProposeesJson) throws Exception {
        Map<String, Object> champs = objectMapper.readValue(donneesProposeesJson, new TypeReference<Map<String, Object>>() {
        });

        for (String cle : champs.keySet()) {
            if (!CHAMPS_MODIFIABLES.contains(cle)) {
                continue; // champ non reconnu : ignoré plutôt que de faire échouer toute la demande
            }
            Object valeur = champs.get(cle);
            switch (cle) {
                case "nom" -> centre.setNom((String) valeur);
                case "logo" -> centre.setLogo((String) valeur);
                case "description" -> centre.setDescription((String) valeur);
                case "services" -> centre.setServices((String) valeur);
                case "ville" -> centre.setVille((String) valeur);
                case "adresse" -> centre.setAdresse((String) valeur);
                case "telephone" -> centre.setTelephone((String) valeur);
                case "email" -> centre.setEmail((String) valeur);
                case "siteWeb" -> centre.setSiteWeb((String) valeur);
                case "mobileMoneyNumero" -> centre.setMobileMoneyNumero((String) valeur);
                case "fourchettePrixMin" -> centre.setFourchettePrixMin(((Number) valeur).intValue());
                case "fourchettePrixMax" -> centre.setFourchettePrixMax(((Number) valeur).intValue());
                default -> {
                }
            }
        }
    }

    private void notifierFormateur(Centre centre, TypeNotification type, String titre, String message) {
        Utilisateur formateur = centre.getUtilisateur();
        if (formateur == null) {
            return;
        }
        notificationService.creerEtEnvoyer(formateur, type, titre, message, Map.of(
                "destinataire", String.valueOf(formateur.getEmail()),
                "typeNotif", type.name(),
                "centreNom", String.valueOf(centre.getNom())
        ));
    }

    private Map<String, Object> toDiffSummary(DemandeModificationCentre demande) {
        Map<String, Object> map = new HashMap<>();
        map.put("idDemande", demande.getIdDemande());
        map.put("idCentre", demande.getCentre().getIdCentre());
        map.put("centreNom", demande.getCentre().getNom());
        map.put("statut", demande.getStatut());
        map.put("donneesProposees", parseJsonSilencieux(demande.getDonneesProposees()));
        map.put("commentaireAdmin", demande.getCommentaireAdmin());
        map.put("createdAt", demande.getCreatedAt());
        map.put("traiteAt", demande.getTraiteAt());
        return map;
    }

    private Object parseJsonSilencieux(String json) {
        try {
            return objectMapper.readValue(json, Map.class);
        } catch (Exception e) {
            return json;
        }
    }

    public record RejectRequest(String commentaireAdmin) {
    }
}
