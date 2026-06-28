package com.eofana.controller;

import com.eofana.entity.Categorie;
import com.eofana.entity.Centre;
import com.eofana.entity.Formation;
import com.eofana.repository.CategorieRepository;
import com.eofana.repository.CentreRepository;
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
    private final CategorieRepository categorieRepository;
    private final CentreRepository centreRepository;

    @GetMapping
    @Transactional(readOnly = true)
    public List<FormationResponse> getAllFormations() {
        return formationRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @GetMapping("/{id}")
    @Transactional(readOnly = true)
    public FormationResponse getFormationById(@PathVariable Long id) {
        Formation formation = formationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Formation introuvable"));

        return toResponse(formation);
    }

    @PostMapping
    @Transactional
    public FormationResponse creerFormation(@RequestBody FormationRequest request) {
        Formation formation = new Formation();

        formation.setTitre(request.titre());
        formation.setDescription(request.description());
        formation.setImage(request.image());
        formation.setDuree(request.duree());
        formation.setLieu(request.lieu());
        formation.setPrix(request.prix());
        formation.setPrixRemise(request.prixRemise());

        if (request.idCategorie() != null) {
            Categorie categorie = categorieRepository.findById(request.idCategorie())
                    .orElseThrow(() -> new RuntimeException("Catégorie introuvable"));
            formation.setCategorie(categorie);
        }

        if (request.idCentre() != null) {
            Centre centre = centreRepository.findById(request.idCentre())
                    .orElseThrow(() -> new RuntimeException("Centre introuvable"));
            formation.setCentre(centre);
        }

        Formation saved = formationRepository.save(formation);

        return toResponse(saved);
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