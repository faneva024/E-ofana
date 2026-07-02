package com.eofana.controller;

import com.eofana.entity.Centre;
import com.eofana.entity.Utilisateur;
import com.eofana.repository.CentreRepository;
import com.eofana.repository.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/centres")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:5174"})
public class CentreController {

    private final CentreRepository centreRepository;
    private final UtilisateurRepository utilisateurRepository;

    @GetMapping
    @Transactional(readOnly = true)
    public List<CentreResponse> getAllCentres() {
        return centreRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @PostMapping
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