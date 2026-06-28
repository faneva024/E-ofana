package com.eofana.controller;

import com.eofana.entity.Utilisateur;
import com.eofana.enums.RoleUtilisateur;
import com.eofana.repository.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/utilisateurs")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:5174"})
public class UtilisateurController {

    private final UtilisateurRepository utilisateurRepository;

    @GetMapping
    @Transactional(readOnly = true)
    public List<UtilisateurResponse> getAllUtilisateurs() {
        return utilisateurRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @PostMapping
    @Transactional
    public UtilisateurResponse creerUtilisateur(@RequestBody UtilisateurRequest request) {
        Utilisateur utilisateur = new Utilisateur();

        utilisateur.setNom(request.nom());
        utilisateur.setPrenom(request.prenom());
        utilisateur.setEmail(request.email());
        utilisateur.setMotDePasse(request.motDePasse());
        utilisateur.setTelephone(request.telephone());
        utilisateur.setAvatar(request.avatar());
        utilisateur.setEstActif(true);

        if (request.role() != null && !request.role().isBlank()) {
            utilisateur.setRole(RoleUtilisateur.valueOf(request.role()));
        }

        Utilisateur saved = utilisateurRepository.save(utilisateur);

        return toResponse(saved);
    }

    private UtilisateurResponse toResponse(Utilisateur utilisateur) {
        return new UtilisateurResponse(
                utilisateur.getIdUser(),
                utilisateur.getNom(),
                utilisateur.getPrenom(),
                utilisateur.getEmail(),
                utilisateur.getRole() != null ? utilisateur.getRole().name() : null,
                utilisateur.getTelephone(),
                utilisateur.getEstActif()
        );
    }

    public record UtilisateurRequest(
            String nom,
            String prenom,
            String email,
            String motDePasse,
            String role,
            String telephone,
            String avatar
    ) {
    }

    public record UtilisateurResponse(
            Long idUser,
            String nom,
            String prenom,
            String email,
            String role,
            String telephone,
            Boolean estActif
    ) {
    }
}