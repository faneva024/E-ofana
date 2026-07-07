package com.eofana.controller;

import com.eofana.entity.Centre;
import com.eofana.entity.DemandeModificationCentre;
import com.eofana.entity.Utilisateur;
import com.eofana.enums.StatutModification;
import com.eofana.repository.CentreRepository;
import com.eofana.repository.DemandeModificationCentreRepository;
import com.eofana.repository.UtilisateurRepository;
import com.eofana.security.FormateurContextResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * Pas de @RequestMapping de classe : ce contrôleur sert à la fois les
 * routes catalogue "/api/v1/centres" (Semaine 1) et les routes profil
 * formateur "/api/v1/formateurs/profil..." (Semaine 2, T-B-107) — deux
 * préfixes différents ne peuvent pas partager un mapping de classe
 * commun, donc chaque méthode déclare son chemin complet.
 */
@RestController
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:5174"})
public class CentreController {

    private final CentreRepository centreRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final DemandeModificationCentreRepository demandeModificationCentreRepository;
    private final FormateurContextResolver formateurContextResolver;

    @GetMapping("/api/v1/centres")
    @Transactional(readOnly = true)
    public List<CentreResponse> getAllCentres() {
        return centreRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @PostMapping("/api/v1/centres")
    @Transactional
    public CentreResponse creerCentre(@RequestBody CentreRequest request) {
        Utilisateur utilisateur = utilisateurRepository.findById(request.idUser())
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));

        Centre centre = new Centre();

        centre.setUtilisateur(utilisateur);
        centre.setNom(request.nom());
        centre.setLogo(request.logo());
        centre.setDescription(request.description());
        centre.setServices(request.services());
        centre.setVille(request.ville());
        centre.setAdresse(request.adresse());
        centre.setFourchettePrixMin(request.fourchettePrixMin() != null ? request.fourchettePrixMin() : 0);
        centre.setFourchettePrixMax(request.fourchettePrixMax() != null ? request.fourchettePrixMax() : 0);
        centre.setTelephone(request.telephone());
        centre.setEmail(request.email());
        centre.setSiteWeb(request.siteWeb());

        Centre saved = centreRepository.save(centre);

        return toResponse(saved);
    }

    /**
     * T-B-107 : profil complet du centre du formateur connecté.
     * Authentification requise (résolue via FormateurContextResolver).
     */
    @GetMapping("/api/v1/formateurs/profil")
    @Transactional(readOnly = true)
    public ResponseEntity<?> getProfile(@RequestHeader(value = "Authorization", required = false) String authorization) {
        try {
            Centre centre = formateurContextResolver.resoudreCentre(authorization);
            return ResponseEntity.ok(toResponse(centre));
        } catch (FormateurContextResolver.AccesNonAutoriseException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", e.getMessage()));
        }
    }

    /**
     * T-B-107 : le formateur propose une modification de son profil.
     * On ne modifie JAMAIS "centres" directement ici : la demande est
     * enregistrée en EN_ATTENTE, à charge pour l'admin/modérateur de
     * l'approuver (cf. §3.2 du cahier des charges et CourseValidationService
     * pour le mécanisme équivalent côté formations).
     */
    @PutMapping("/api/v1/formateurs/profil")
    @Transactional
    public ResponseEntity<?> requestProfileUpdate(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody String donneesProposeesJson) {
        try {
            Centre centre = formateurContextResolver.resoudreCentre(authorization);

            DemandeModificationCentre demande = DemandeModificationCentre.builder()
                    .centre(centre)
                    .donneesProposees(donneesProposeesJson)
                    .statut(StatutModification.enAttente)
                    .build();

            DemandeModificationCentre saved = demandeModificationCentreRepository.save(demande);

            return ResponseEntity.ok(Map.of(
                    "message", "Votre demande de modification a été soumise et est en attente de validation",
                    "idDemande", saved.getIdDemande(),
                    "statut", saved.getStatut().name()
            ));
        } catch (FormateurContextResolver.AccesNonAutoriseException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", e.getMessage()));
        }
    }

    /**
     * T-B-107 : profil public d'un centre, consulté par les apprenants.
     * Pas d'authentification requise. Ne renvoie que les informations
     * déjà approuvées (celles réellement présentes sur "centres" —
     * une demande de modification EN_ATTENTE n'apparaît jamais ici).
     */
    @GetMapping("/api/v1/formateurs/profil/public/{id}")
    @Transactional(readOnly = true)
    public ResponseEntity<?> getPublicProfile(@PathVariable Long id) {
    return centreRepository.findById(id)
            .map(centre -> ResponseEntity.ok().body((Object) toResponse(centre)))
            .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "Centre introuvable")));
}

    private CentreResponse toResponse(Centre centre) {
        return new CentreResponse(
                centre.getIdCentre(),
                centre.getUtilisateur() != null ? centre.getUtilisateur().getIdUser() : null,
                centre.getNom(),
                centre.getDescription(),
                centre.getVille(),
                centre.getAdresse(),
                centre.getTelephone(),
                centre.getEmail(),
                centre.getStatut() != null ? centre.getStatut().name() : null,
                centre.getAbonnement() != null ? centre.getAbonnement().name() : null,
                centre.getTauxCommission()
        );
    }

    public record CentreRequest(
            Long idUser,
            String nom,
            String logo,
            String description,
            String services,
            String ville,
            String adresse,
            Integer fourchettePrixMin,
            Integer fourchettePrixMax,
            String telephone,
            String email,
            String siteWeb
    ) {
    }

    public record CentreResponse(
            Long idCentre,
            Long idUser,
            String nom,
            String description,
            String ville,
            String adresse,
            String telephone,
            String email,
            String statut,
            String abonnement,
            BigDecimal tauxCommission
    ) {
    }
}