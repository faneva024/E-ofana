package com.eofana.controller;

import com.eofana.entity.Categorie;
import com.eofana.entity.Centre;
import com.eofana.entity.Formation;
import com.eofana.entity.SessionFormation;
import com.eofana.enums.StatutFormation;
import com.eofana.repository.CategorieRepository;
import com.eofana.repository.FormationRepository;
import com.eofana.repository.SessionFormationRepository;
import com.eofana.security.FormateurContextResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * T-B-108 : publication et gestion des formations côté formateur.
 *
 * Toutes les routes exigent une authentification formateur, et
 * vérifient systématiquement que la formation appartient bien au
 * centre du formateur connecté avant toute lecture/modification —
 * jamais de confiance dans un idFormation fourni par le client seul.
 */
@RestController
@RequestMapping("/api/v1/formations")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:5174"})
public class FormationFormateurController {

    private final FormationRepository formationRepository;
    private final SessionFormationRepository sessionFormationRepository;
    private final CategorieRepository categorieRepository;
    private final FormateurContextResolver formateurContextResolver;

    /**
     * Publie une nouvelle formation. La création couvre à la fois la
     * Formation et sa première SessionFormation (champs obligatoires
     * du cahier des charges §3.3 : image, titre, description,
     * catégorie, durée, lieu, dateDebut, dateLimiteInscription, places,
     * prix ; prixRemise optionnel).
     */
    @PostMapping
    @Transactional
    public ResponseEntity<?> publishCourse(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody PublishCourseRequest request) {

        Centre centre;
        try {
            centre = formateurContextResolver.resoudreCentre(authorization);
        } catch (FormateurContextResolver.AccesNonAutoriseException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", e.getMessage()));
        }

        String erreur = validerChampsObligatoires(request);
        if (erreur != null) {
            return ResponseEntity.badRequest().body(Map.of("message", erreur));
        }

        Formation formation = new Formation();
        formation.setCentre(centre);
        formation.setTitre(request.titre());
        formation.setDescription(request.description());
        formation.setImage(request.image());
        formation.setDuree(request.duree());
        formation.setLieu(request.lieu());
        formation.setPrix(request.prix());
        formation.setPrixRemise(request.prixRemise());
        formation.setStatut(StatutFormation.enAttente);

        if (request.idCategorie() != null) {
            Categorie categorie = categorieRepository.findById(request.idCategorie())
                    .orElseThrow(() -> new RuntimeException("Catégorie introuvable"));
            formation.setCategorie(categorie);
        }

        Formation savedFormation = formationRepository.save(formation);

        SessionFormation session = SessionFormation.builder()
                .formation(savedFormation)
                .dateDebut(request.dateDebut())
                .dateLimiteInscription(request.dateLimiteInscription())
                .placesTotal(request.placesDisponibles())
                .placesRestantes(request.placesDisponibles())
                .build();

        sessionFormationRepository.save(session);

        return ResponseEntity.ok(Map.of(
                "message", "Formation créée et envoyée en modération",
                "idFormation", savedFormation.getIdFormation(),
                "statut", savedFormation.getStatut().name()
        ));
    }

    /**
     * Modifie une formation existante. Si elle était déjà APPROUVE,
     * elle repasse en EN_ATTENTE : toute modification doit repasser
     * par la modération (cf. CourseValidationService).
     */
    @PutMapping("/{id}")
    @Transactional
    public ResponseEntity<?> updateCourse(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @PathVariable Long id,
            @RequestBody PublishCourseRequest request) {

        Centre centre;
        try {
            centre = formateurContextResolver.resoudreCentre(authorization);
        } catch (FormateurContextResolver.AccesNonAutoriseException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", e.getMessage()));
        }

        Formation formation = formationRepository.findById(id).orElse(null);
        if (formation == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "Formation introuvable"));
        }

        if (!appartientAuCentre(formation, centre)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "Cette formation n'appartient pas à votre centre"));
        }

        if (request.titre() != null) formation.setTitre(request.titre());
        if (request.description() != null) formation.setDescription(request.description());
        if (request.image() != null) formation.setImage(request.image());
        if (request.duree() != null) formation.setDuree(request.duree());
        if (request.lieu() != null) formation.setLieu(request.lieu());
        if (request.prix() != null) formation.setPrix(request.prix());
        if (request.prixRemise() != null) formation.setPrixRemise(request.prixRemise());

        if (request.idCategorie() != null) {
            Categorie categorie = categorieRepository.findById(request.idCategorie())
                    .orElseThrow(() -> new RuntimeException("Catégorie introuvable"));
            formation.setCategorie(categorie);
        }

        if (formation.getStatut() == StatutFormation.approuve) {
            formation.setStatut(StatutFormation.enAttente);
        }

        Formation saved = formationRepository.save(formation);

        return ResponseEntity.ok(Map.of(
                "message", "Formation mise à jour",
                "idFormation", saved.getIdFormation(),
                "statut", saved.getStatut().name()
        ));
    }

    /** Ajoute une nouvelle session (récurrence, cf. §3.4) à une formation existante du centre connecté. */
    @PostMapping("/{id}/sessions")
    @Transactional
    public ResponseEntity<?> addSession(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @PathVariable Long id,
            @RequestBody SessionRequest request) {

        Centre centre;
        try {
            centre = formateurContextResolver.resoudreCentre(authorization);
        } catch (FormateurContextResolver.AccesNonAutoriseException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", e.getMessage()));
        }

        Formation formation = formationRepository.findById(id).orElse(null);
        if (formation == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "Formation introuvable"));
        }

        if (!appartientAuCentre(formation, centre)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "Cette formation n'appartient pas à votre centre"));
        }

        SessionFormation session = SessionFormation.builder()
                .formation(formation)
                .dateDebut(request.dateDebut())
                .dateFin(request.dateFin())
                .dateLimiteInscription(request.dateLimiteInscription())
                .placesTotal(request.placesTotal())
                .placesRestantes(request.placesTotal())
                .build();

        SessionFormation saved = sessionFormationRepository.save(session);

        return ResponseEntity.ok(Map.of(
                "message", "Session ajoutée",
                "idSession", saved.getIdSession()
        ));
    }

    /**
     * Liste les formations du centre connecté, avec compteur d'inscrits
     * et places restantes. Le paramètre "statut" filtre optionnellement
     * (EN_ATTENTE, APPROUVE, REJETE, CORRECTION_DEMANDEE — ici en
     * minuscules, cf. StatutFormation, miroir de l'ENUM PostgreSQL).
     */
    @GetMapping("/mes-formations")
    @Transactional(readOnly = true)
    public ResponseEntity<?> getMyCourses(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestParam(required = false) String statut) {

        Centre centre;
        try {
            centre = formateurContextResolver.resoudreCentre(authorization);
        } catch (FormateurContextResolver.AccesNonAutoriseException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", e.getMessage()));
        }

        List<Formation> formations;
        if (statut != null && !statut.isBlank()) {
            StatutFormation statutFormation;
            try {
                statutFormation = StatutFormation.valueOf(statut);
            } catch (IllegalArgumentException e) {
                return ResponseEntity.badRequest().body(Map.of("message", "Statut inconnu : " + statut));
            }
            formations = formationRepository.findByCentre_IdCentreAndStatut(centre.getIdCentre(), statutFormation);
        } else {
            formations = formationRepository.findByCentre_IdCentre(centre.getIdCentre());
        }

        List<Map<String, Object>> reponse = formations.stream()
                .map(formation -> {
                    long nbInscrits = formationRepository.countInscriptionsBySession(formation.getIdFormation());
                    Integer placesRestantes = sessionFormationRepository
                            .calculerPlacesRestantes(formation.getIdFormation());

                    return Map.<String, Object>of(
                            "idFormation", formation.getIdFormation(),
                            "titre", formation.getTitre(),
                            "statut", formation.getStatut().name(),
                            "prix", formation.getPrix(),
                            "nbInscrits", nbInscrits,
                            "placesRestantes", placesRestantes != null ? placesRestantes : 0
                            // NB: "nombre de visites" volontairement absent — hors périmètre, cf. T-B-104
                    );
                })
                .toList();

        return ResponseEntity.ok(reponse);
    }

    /**
     * Supprime/dépublie une formation. Refuse s'il existe des
     * inscriptions actives (statut = VALIDE) — une formation payée par
     * des apprenants ne peut pas disparaître silencieusement.
     */
    @DeleteMapping("/{id}")
    @Transactional
    public ResponseEntity<?> deleteCourse(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @PathVariable Long id) {

        Centre centre;
        try {
            centre = formateurContextResolver.resoudreCentre(authorization);
        } catch (FormateurContextResolver.AccesNonAutoriseException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", e.getMessage()));
        }

        Formation formation = formationRepository.findById(id).orElse(null);
        if (formation == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "Formation introuvable"));
        }

        if (!appartientAuCentre(formation, centre)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "Cette formation n'appartient pas à votre centre"));
        }

        long nbInscriptionsActives = formationRepository.countInscriptionsBySession(formation.getIdFormation());
        if (nbInscriptionsActives > 0) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(
                    "message", "Impossible de supprimer : des apprenants sont déjà inscrits à cette formation"
            ));
        }

        formationRepository.delete(formation);

        return ResponseEntity.ok(Map.of("message", "Formation supprimée"));
    }

    private boolean appartientAuCentre(Formation formation, Centre centre) {
        return formation.getCentre() != null
                && formation.getCentre().getIdCentre() != null
                && formation.getCentre().getIdCentre().equals(centre.getIdCentre());
    }

    private String validerChampsObligatoires(PublishCourseRequest request) {
        if (isBlank(request.titre())) return "Le titre est obligatoire";
        if (isBlank(request.description())) return "La description est obligatoire";
        if (isBlank(request.image())) return "L'image est obligatoire";
        if (request.idCategorie() == null) return "La catégorie est obligatoire";
        if (isBlank(request.duree())) return "La durée est obligatoire";
        if (isBlank(request.lieu())) return "Le lieu est obligatoire";
        if (request.dateDebut() == null) return "La date de début est obligatoire";
        if (request.dateLimiteInscription() == null) return "La date limite d'inscription est obligatoire";
        if (request.placesDisponibles() == null || request.placesDisponibles() <= 0) return "Le nombre de places disponibles est obligatoire";
        if (request.prix() == null || request.prix() < 0) return "Le prix est obligatoire";
        if (request.prixRemise() != null && request.prixRemise() >= request.prix()) return "Le prix remisé doit être inférieur au prix normal";
        return null;
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    public record PublishCourseRequest(
            String titre,
            String description,
            String image,
            Long idCategorie,
            String duree,
            String lieu,
            LocalDate dateDebut,
            LocalDate dateLimiteInscription,
            Integer placesDisponibles,
            Long prix,
            Long prixRemise
    ) {
    }

    public record SessionRequest(
            LocalDate dateDebut,
            LocalDate dateFin,
            LocalDate dateLimiteInscription,
            Integer placesTotal
    ) {
    }
}
