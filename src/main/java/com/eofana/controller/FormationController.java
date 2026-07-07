package com.eofana.controller;

import com.eofana.entity.Formation;
import com.eofana.repository.FormationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/formations")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:5174"})
public class FormationController {

    private final FormationRepository formationRepository;

    @GetMapping
    @Transactional(readOnly = true)
    public List<FormationResponse> getAllFormations(
            @RequestParam(required = false) String categorie,
            @RequestParam(required = false) String ville,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String q,
            @RequestParam(required = false) Long prixMax
    ) {
        String motCle = isNotBlank(search) ? search : q;

        return formationRepository.findAll()
                .stream()
                .map(this::toResponse)
                .filter(formation -> filtreCategorie(formation, categorie))
                .filter(formation -> filtreVille(formation, ville))
                .filter(formation -> filtreRecherche(formation, motCle))
                .filter(formation -> filtrePrixMax(formation, prixMax))
                .toList();
    }

    @GetMapping("/{id}")
    @Transactional(readOnly = true)
    public FormationResponse getFormationById(@PathVariable Long id) {
        Formation formation = formationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Formation introuvable"));

        return toResponse(formation);
    }

    // Correction module Formateur (T-B-108) : la création de formation via
    // POST /api/v1/formations a été déplacée vers
    // FormationFormateurController#publishCourse(), qui valide les champs
    // obligatoires du cahier des charges (§3.3) et rattache la formation au
    // centre du formateur authentifié plutôt qu'à un idCentre fourni en
    // clair par le client. Garder les deux aurait provoqué un conflit de
    // mapping Spring (deux POST /api/v1/formations dans deux contrôleurs).

    private boolean filtreCategorie(FormationResponse formation, String categorie) {
        if (!isNotBlank(categorie)) {
            return true;
        }

        if ("all".equalsIgnoreCase(categorie) || "Toutes les catégories".equalsIgnoreCase(categorie)) {
            return true;
        }

        return containsIgnoreCase(formation.categorie(), categorie);
    }

    private boolean filtreVille(FormationResponse formation, String ville) {
        if (!isNotBlank(ville)) {
            return true;
        }

        if ("all".equalsIgnoreCase(ville) || "Toutes les villes".equalsIgnoreCase(ville)) {
            return true;
        }

        return containsIgnoreCase(formation.ville(), ville)
                || containsIgnoreCase(formation.lieu(), ville);
    }

    private boolean filtreRecherche(FormationResponse formation, String motCle) {
        if (!isNotBlank(motCle)) {
            return true;
        }

        return containsIgnoreCase(formation.titre(), motCle)
                || containsIgnoreCase(formation.description(), motCle)
                || containsIgnoreCase(formation.categorie(), motCle)
                || containsIgnoreCase(formation.centre(), motCle)
                || containsIgnoreCase(formation.ville(), motCle)
                || containsIgnoreCase(formation.lieu(), motCle);
    }

    private boolean filtrePrixMax(FormationResponse formation, Long prixMax) {
        if (prixMax == null || prixMax <= 0) {
            return true;
        }

        Long prixFinal = formation.prixRemise() != null
                ? formation.prixRemise()
                : formation.prix();

        if (prixFinal == null) {
            return true;
        }

        return prixFinal <= prixMax;
    }

    private boolean containsIgnoreCase(String valeur, String recherche) {
        if (valeur == null || recherche == null) {
            return false;
        }

        return valeur.toLowerCase().contains(recherche.toLowerCase());
    }

    private boolean isNotBlank(String valeur) {
        return valeur != null && !valeur.trim().isEmpty();
    }

    private FormationResponse toResponse(Formation formation) {
        String categorie = formation.getCategorie() != null
                ? formation.getCategorie().getNom()
                : null;

        String centre = formation.getCentre() != null
                ? formation.getCentre().getNom()
                : null;

        String ville = formation.getCentre() != null
                ? formation.getCentre().getVille()
                : formation.getLieu();

        return new FormationResponse(
                formation.getIdFormation(),
                formation.getTitre(),
                formation.getDescription(),
                formation.getImage(),
                formation.getDuree(),
                formation.getLieu(),
                ville,
                formation.getPrix(),
                formation.getPrixRemise(),
                categorie,
                centre,
                formation.getNoteMoyenne(),
                formation.getNbAvis()
        );
    }

    public record FormationRequest(
            String titre,
            String description,
            String image,
            String duree,
            String lieu,
            Long prix,
            Long prixRemise,
            Long idCategorie,
            Long idCentre
    ) {
    }

    public record FormationResponse(
            Long idFormation,
            String titre,
            String description,
            String image,
            String duree,
            String lieu,
            String ville,
            Long prix,
            Long prixRemise,
            String categorie,
            String centre,
            BigDecimal noteMoyenne,
            Integer nbAvis
    ) {
    }
}