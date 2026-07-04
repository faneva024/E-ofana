package com.eofana.controller;

import com.eofana.entity.Centre;
import com.eofana.entity.DemandeModificationCentre;
import com.eofana.entity.Utilisateur;
import com.eofana.enums.StatutCentre;
import com.eofana.enums.StatutModification;
import com.eofana.repository.CentreRepository;
import com.eofana.repository.DemandeModificationCentreRepository;
import com.eofana.repository.UtilisateurRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;


@RestController
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:5174"})
public class CentreController {

    private final CentreRepository centreRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final DemandeModificationCentreRepository demandeRepository;
    private final ObjectMapper objectMapper;

    /**
     * Retourne la liste de tous les centres actifs.
     * Route publique : pas d'authentification requise.
     */
    @GetMapping("/api/v1/centres")
    @Transactional(readOnly = true)
    public List<CentrePublicResponse> getAllCentres() {
        return centreRepository.findByStatutOrderByCreatedAtAsc(StatutCentre.actif)
                .stream()
                .map(this::toPublicResponse)
                .toList();
    }

    @PostMapping("/api/v1/centres")
    @Transactional
    public ResponseEntity<CentrePublicResponse> creerCentre(@RequestBody CentreCreationRequest request) {
        if (request.idUser() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Le champ idUser est obligatoire");
        }

        Utilisateur utilisateur = utilisateurRepository.findById(request.idUser())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Utilisateur introuvable : id=" + request.idUser()));

        if (centreRepository.existsByUtilisateur_IdUser(request.idUser())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Cet utilisateur possède déjà un centre. Un formateur ne peut avoir qu'un seul centre.");
        }

        Centre centre = Centre.builder()
                .utilisateur(utilisateur)
                .nom(validerNonVide(request.nom(), "nom"))
                .logo(request.logo())
                .description(request.description())
                .services(request.services())
                .ville(request.ville())
                .adresse(request.adresse())
                .fourchettePrixMin(request.fourchettePrixMin() != null ? request.fourchettePrixMin() : 0)
                .fourchettePrixMax(request.fourchettePrixMax() != null ? request.fourchettePrixMax() : 0)
                .telephone(request.telephone())
                .email(request.email())
                .siteWeb(request.siteWeb())
                .build();

        Centre saved = centreRepository.save(centre);
        return ResponseEntity.status(HttpStatus.CREATED).body(toPublicResponse(saved));
    }

    /**
     * Retourne le profil complet du centre du formateur connecté.
     */
    @GetMapping("/api/v1/formateurs/profil")
    @Transactional(readOnly = true)
    public ResponseEntity<ProfilFormateurResponse> getProfile(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        Long idUser = extraireIdUser(authHeader);

        Centre centre = centreRepository.findByUtilisateur_IdUser(idUser)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Aucun profil centre trouvé pour ce compte. "
                                + "Veuillez créer votre centre via POST /api/v1/centres."));

        // Vérifier si une demande de modification est en cours
        boolean demandeEnCours = demandeRepository
                .existsByCentre_IdCentreAndStatut(centre.getIdCentre(), StatutModification.enAttente);

        return ResponseEntity.ok(toProfilFormateur(centre, demandeEnCours));
    }

    /**
     * Soumet une demande de modification du profil centre.
     */
    @PutMapping("/api/v1/formateurs/profil")
    @Transactional
    public ResponseEntity<DemandeModificationResponse> requestProfileUpdate(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody ModificationProfilRequest request) {

        Long idUser = extraireIdUser(authHeader);

        Centre centre = centreRepository.findByUtilisateur_IdUser(idUser)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Aucun profil centre trouvé pour ce compte."));

        // Règle métier : une seule demande active à la fois
        if (demandeRepository.existsByCentre_IdCentreAndStatut(
                centre.getIdCentre(), StatutModification.enAttente)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Une demande de modification est déjà en cours d'examen. "
                            + "Veuillez attendre sa validation avant d'en soumettre une nouvelle.");
        }

        // Rejeter une demande vide (aucun champ renseigné)
        if (estDemandeVide(request)) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY,
                    "La demande de modification ne contient aucun champ. "
                            + "Renseignez au moins un champ à modifier.");
        }

        // Sérialiser les données proposées en JSON pour la colonne JSONB
        String donneesJson = serialiserDemande(request);

        DemandeModificationCentre demande = DemandeModificationCentre.builder()
                .centre(centre)
                .donneesProposees(donneesJson)
                .statut(StatutModification.enAttente)
                .build();

        DemandeModificationCentre saved = demandeRepository.save(demande);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new DemandeModificationResponse(
                        saved.getIdDemande(),
                        saved.getStatut().name(),
                        saved.getCreatedAt(),
                        "Votre demande de modification a été soumise. "
                                + "Elle sera examinée par notre équipe sous 48 heures."
                ));
    }

    /**
     * Retourne le profil public d'un centre, tel que vu par les apprenants.
     * Seuls les centres au statut {@code actif} sont accessibles.
     */
    @GetMapping("/api/v1/formateurs/profil/public/{id}")
    @Transactional(readOnly = true)
    public ResponseEntity<CentrePublicDetailResponse> getPublicProfile(@PathVariable("id") Long idCentre) {
        Centre centre = centreRepository.findById(idCentre)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Centre introuvable : id=" + idCentre));

        // Un centre enAttente, inactif ou suspendu est invisible pour les apprenants.
        if (centre.getStatut() != StatutCentre.actif) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "Ce profil centre n'est pas disponible.");
        }

        return ResponseEntity.ok(toPublicDetailResponse(centre));
    }


    /** Profil minimal pour la liste publique des centres actifs. */
    private CentrePublicResponse toPublicResponse(Centre c) {
        return new CentrePublicResponse(
                c.getIdCentre(),
                c.getNom(),
                c.getLogo(),
                c.getVille(),
                c.getStatut() != null ? c.getStatut().name() : null,
                c.getAbonnement() != null ? c.getAbonnement().name() : null,
                c.getTauxCommission()
        );
    }

    /**
     * Profil complet pour le formateur connecté.
     * Inclut toutes les informations y compris administratives.
     */
    private ProfilFormateurResponse toProfilFormateur(Centre c, boolean demandeEnCours) {
        return new ProfilFormateurResponse(
                c.getIdCentre(),
                // Informations publiques
                c.getNom(),
                c.getLogo(),
                c.getDescription(),
                c.getServices(),
                c.getVille(),
                c.getAdresse(),
                c.getFourchettePrixMin(),
                c.getFourchettePrixMax(),
                c.getTelephone(),
                c.getEmail(),
                c.getSiteWeb(),
                c.getReseauxSociaux(),
                // Informations administratives (formateur uniquement)
                c.getStatut() != null ? c.getStatut().name() : null,
                c.getAbonnement() != null ? c.getAbonnement().name() : null,
                c.getTauxCommission(),
                c.getFrequenceReversement() != null ? c.getFrequenceReversement().name() : null,
                c.getMobileMoneyOperateur() != null ? c.getMobileMoneyOperateur().name() : null,
                c.getMobileMoneyNumero(),
                c.getValidatedAt(),
                // Indicateur de demande en cours
                demandeEnCours
        );
    }

    /**
     * Profil public détaillé pour les apprenants.
     */
    private CentrePublicDetailResponse toPublicDetailResponse(Centre c) {
        return new CentrePublicDetailResponse(
                c.getIdCentre(),
                c.getNom(),
                c.getLogo(),
                c.getDescription(),
                c.getServices(),
                c.getVille(),
                c.getAdresse(),
                c.getFourchettePrixMin(),
                c.getFourchettePrixMax(),
                c.getTelephone(),
                c.getEmail(),
                c.getSiteWeb(),
                c.getReseauxSociaux()
        );
    }

    /**
     * Extrait l'identifiant de l'utilisateur depuis le header Authorization.
     */
    private Long extraireIdUser(String authHeader) {
        if (authHeader == null || authHeader.isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,
                    "Header Authorization manquant. "
                            + "Connectez-vous via POST /api/v1/auth/connexion et "
                            + "incluez le token retourné dans le header Authorization.");
        }

        // Support du format brut "TOKEN-DEMO-{id}" et du format "Bearer TOKEN-DEMO-{id}"
        String token = authHeader.startsWith("Bearer ")
                ? authHeader.substring(7).trim()
                : authHeader.trim();

        if (!token.startsWith("TOKEN-DEMO-")) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,
                    "Token invalide. Format attendu : TOKEN-DEMO-{idUser}");
        }

        try {
            return Long.parseLong(token.substring("TOKEN-DEMO-".length()));
        } catch (NumberFormatException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,
                    "Token malformé : l'identifiant utilisateur n'est pas un nombre valide.");
        }
    }

    /** Vérifie que tous les champs de la demande sont null (demande vide). */
    private boolean estDemandeVide(ModificationProfilRequest r) {
        return r.nom() == null
                && r.logo() == null
                && r.description() == null
                && r.services() == null
                && r.ville() == null
                && r.adresse() == null
                && r.fourchettePrixMin() == null
                && r.fourchettePrixMax() == null
                && r.telephone() == null
                && r.email() == null
                && r.siteWeb() == null
                && r.reseauxSociaux() == null
                && r.mobileMoneyOperateur() == null
                && r.mobileMoneyNumero() == null;
    }

    /**
     * Sérialise la demande de modification en JSON pour stockage en JSONB.
     * N'inclut pas les champs null (patch partiel).
     */
    private String serialiserDemande(ModificationProfilRequest request) {
        try {
            // Construire un Map ne contenant que les champs non null
            // → le modérateur voit clairement ce qui est demandé
            var champs = new java.util.LinkedHashMap<String, Object>();
            if (request.nom()               != null) champs.put("nom",               request.nom());
            if (request.logo()              != null) champs.put("logo",              request.logo());
            if (request.description()       != null) champs.put("description",       request.description());
            if (request.services()          != null) champs.put("services",          request.services());
            if (request.ville()             != null) champs.put("ville",             request.ville());
            if (request.adresse()           != null) champs.put("adresse",           request.adresse());
            if (request.fourchettePrixMin() != null) champs.put("fourchettePrixMin", request.fourchettePrixMin());
            if (request.fourchettePrixMax() != null) champs.put("fourchettePrixMax", request.fourchettePrixMax());
            if (request.telephone()         != null) champs.put("telephone",         request.telephone());
            if (request.email()             != null) champs.put("email",             request.email());
            if (request.siteWeb()           != null) champs.put("siteWeb",           request.siteWeb());
            if (request.reseauxSociaux()    != null) champs.put("reseauxSociaux",    request.reseauxSociaux());
            if (request.mobileMoneyOperateur() != null) champs.put("mobileMoneyOperateur", request.mobileMoneyOperateur());
            if (request.mobileMoneyNumero() != null) champs.put("mobileMoneyNumero", request.mobileMoneyNumero());

            return objectMapper.writeValueAsString(champs);
        } catch (JsonProcessingException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Erreur lors de la sérialisation de la demande.");
        }
    }

    /** Valide qu'un champ obligatoire est non vide et retourne sa valeur. */
    private String validerNonVide(String valeur, String nomChamp) {
        if (valeur == null || valeur.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Le champ '" + nomChamp + "' est obligatoire.");
        }
        return valeur.trim();
    }


    /**
     * Corps de la requête POST /api/v1/centres (création initiale du centre). * L'idUser identifie le formateur propriétaire.
     */
    public record CentreCreationRequest(
            Long    idUser,
            String  nom,
            String  logo,
            String  description,
            String  services,
            String  ville,
            String  adresse,
            Integer fourchettePrixMin,
            Integer fourchettePrixMax,
            String  telephone,
            String  email,
            String  siteWeb
    ) {}

    
    public record ModificationProfilRequest(
            String  nom,
            String  logo,
            String  description,
            String  services,
            String  ville,
            String  adresse,
            Integer fourchettePrixMin,
            Integer fourchettePrixMax,
            String  telephone,
            String  email,
            String  siteWeb,
            String  reseauxSociaux,
            String  mobileMoneyOperateur,
            String  mobileMoneyNumero
    ) {}


    /**
     * Réponse minimale pour la liste publique GET /api/v1/centres.
     */
    public record CentrePublicResponse(
            Long       idCentre,
            String     nom,
            String     logo,
            String     ville,
            String     statut,
            String     abonnement,
            BigDecimal tauxCommission
    ) {}

    /**
     * Réponse complète pour le formateur connecté (GET /api/v1/formateurs/profil).
     */
    public record ProfilFormateurResponse(
            Long           idCentre,
            // Informations publiques
            String         nom,
            String         logo,
            String         description,
            String         services,
            String         ville,
            String         adresse,
            Integer        fourchettePrixMin,
            Integer        fourchettePrixMax,
            String         telephone,
            String         email,
            String         siteWeb,
            String         reseauxSociaux,
            // Informations administratives (formateur uniquement)
            String         statut,
            String         abonnement,
            BigDecimal     tauxCommission,
            String         frequenceReversement,
            String         mobileMoneyOperateur,
            String         mobileMoneyNumero,
            OffsetDateTime validatedAt,
            // Indicateur UX
            boolean        demandeModificationEnCours
    ) {}

    /**
     * Réponse publique détaillée (GET /api/v1/formateurs/profil/public/{id}).
     * N'expose aucune donnée financière ou administrative.
     */
    public record CentrePublicDetailResponse(
            Long    idCentre,
            String  nom,
            String  logo,
            String  description,
            String  services,
            String  ville,
            String  adresse,
            Integer fourchettePrixMin,
            Integer fourchettePrixMax,
            String  telephone,
            String  email,
            String  siteWeb,
            String  reseauxSociaux
    ) {}

    /**
     * Réponse à la soumission d'une demande de modification
     * (PUT /api/v1/formateurs/profil).
     */
    public record DemandeModificationResponse(
            Long           idDemande,
            String         statut,
            OffsetDateTime createdAt,
            String         message
    ) {}
}
