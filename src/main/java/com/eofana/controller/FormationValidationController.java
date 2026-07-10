package com.eofana.controller;

import com.eofana.entity.Formation;
import com.eofana.entity.Utilisateur;
import com.eofana.enums.StatutFormation;
import com.eofana.repository.FormationRepository;
import com.eofana.security.AdminContextResolver;
import com.eofana.service.CourseValidationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * T-B-205 : modération des formations soumises par les centres —
 * approbation, rejet (motif obligatoire), demande de correction
 * (commentaire obligatoire). La logique métier vit dans
 * CourseValidationService (T-B-214) ; ce contrôleur ne fait que
 * résoudre l'admin courant, charger la formation, et déléguer.
 */
@RestController
@RequestMapping("/api/v1/admin/formations")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:5174"})
public class FormationValidationController {

    private final FormationRepository formationRepository;
    private final CourseValidationService courseValidationService;
    private final AdminContextResolver adminContextResolver;

    /**
     * Liste paginée des formations en attente de validation. Le
     * ticket dit "en attente" (statut = enAttente) ; un paramètre
     * optionnel "statut" permet de réutiliser la même route pour les
     * formations en correctionDemandee, cas d'usage voisin dans le
     * même écran admin.
     */
    @GetMapping("/en-attente")
    @Transactional(readOnly = true)
    public ResponseEntity<?> getPendingCourses(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestParam(defaultValue = "enAttente") StatutFormation statut,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int taille) {

        try {
            adminContextResolver.resoudreAdmin(authorization);
        } catch (AdminContextResolver.AccesNonAutoriseException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", e.getMessage()));
        }

        Page<Formation> formations = formationRepository.findByStatut(statut, PageRequest.of(page, taille));
        return ResponseEntity.ok(formations.map(this::toSummary));
    }

    @PutMapping("/{id}/approuver")
    @Transactional
    public ResponseEntity<?> approveCourse(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @PathVariable Long id) {

        return traiterDecision(authorization, id, (formation, admin) ->
                courseValidationService.approveCourse(formation, admin));
    }

    @PutMapping("/{id}/rejeter")
    @Transactional
    public ResponseEntity<?> rejectCourse(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @PathVariable Long id,
            @RequestBody DecisionRequest request) {

        if (request.motif() == null || request.motif().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Le motif de rejet est obligatoire"));
        }

        return traiterDecision(authorization, id, (formation, admin) ->
                courseValidationService.rejectCourse(formation, admin, request.motif()));
    }

    @PutMapping("/{id}/demander-correction")
    @Transactional
    public ResponseEntity<?> requestCorrection(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @PathVariable Long id,
            @RequestBody DecisionRequest request) {

        if (request.motif() == null || request.motif().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Le commentaire de correction est obligatoire"));
        }

        return traiterDecision(authorization, id, (formation, admin) ->
                courseValidationService.requestCorrection(formation, admin, request.motif()));
    }

    private ResponseEntity<?> traiterDecision(String authorization, Long id, DecisionFn decision) {
        Utilisateur admin;
        try {
            admin = adminContextResolver.resoudreAdmin(authorization);
        } catch (AdminContextResolver.AccesNonAutoriseException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", e.getMessage()));
        }

        Formation formation = formationRepository.findById(id).orElse(null);
        if (formation == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "Formation introuvable"));
        }

        try {
            Formation resultat = decision.appliquer(formation, admin);
            return ResponseEntity.ok(Map.of(
                    "message", "Décision enregistrée",
                    "formation", toSummary(resultat)
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    private Map<String, Object> toSummary(Formation formation) {
        Map<String, Object> map = new java.util.HashMap<>();
        map.put("idFormation", formation.getIdFormation());
        map.put("titre", formation.getTitre());
        map.put("statut", formation.getStatut());
        map.put("motifRejet", formation.getMotifRejet());
        map.put("idCentre", formation.getCentre() != null ? formation.getCentre().getIdCentre() : null);
        map.put("centreNom", formation.getCentre() != null ? formation.getCentre().getNom() : null);
        return map;
    }

    @FunctionalInterface
    private interface DecisionFn {
        Formation appliquer(Formation formation, Utilisateur admin);
    }

    public record DecisionRequest(String motif) {
    }
}
