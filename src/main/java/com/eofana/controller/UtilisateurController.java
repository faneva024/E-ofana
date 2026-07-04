package com.eofana.controller;

import com.eofana.entity.Utilisateur;
import com.eofana.enums.RoleUtilisateur;
import com.eofana.repository.UtilisateurRepository;
import com.eofana.security.JwtAuthenticationFilter.AuthenticatedUser;
import com.eofana.service.UtilisateurService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.NoSuchElementException;

@RestController
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:5174"})
public class UtilisateurController {

    private final UtilisateurRepository utilisateurRepository;
    private final UtilisateurService utilisateurService;

    /**
     * Espace personnel apprenant (T-B-012). L'identifiant vient du
     * JWT, jamais d'un paramètre de requête : un apprenant ne doit
     * pouvoir consulter/modifier que SON propre profil.
     */
    @GetMapping("/api/v1/apprenants/profil")
    @Transactional(readOnly = true)
    public ResponseEntity<UserProfileResponse> getProfile(@AuthenticationPrincipal AuthenticatedUser principal) {
        Utilisateur utilisateur = utilisateurRepository.findById(principal.idUser())
                .orElseThrow(() -> new NoSuchElementException("Utilisateur introuvable"));

        return ResponseEntity.ok(toProfileResponse(utilisateur));
    }

    @PutMapping("/api/v1/apprenants/profil")
    @Transactional
    public ResponseEntity<UserProfileResponse> updateProfile(
            @AuthenticationPrincipal AuthenticatedUser principal,
            @RequestBody UpdateProfileRequest request
    ) {
        Utilisateur utilisateur = utilisateurRepository.findById(principal.idUser())
                .orElseThrow(() -> new NoSuchElementException("Utilisateur introuvable"));

        // L'email n'est volontairement pas modifiable ici : c'est
        // l'identifiant de connexion unique du compte.
        if (request.nom() != null) utilisateur.setNom(request.nom());
        if (request.prenom() != null) utilisateur.setPrenom(request.prenom());
        if (request.telephone() != null) utilisateur.setTelephone(request.telephone());
        if (request.avatar() != null) utilisateur.setAvatar(request.avatar());

        Utilisateur saved = utilisateurRepository.save(utilisateur);

        return ResponseEntity.ok(toProfileResponse(saved));
    }

    // --- Endpoints existants (création directe / listing), conservés
    //     pour compatibilité avec les écrans déjà en place. ---

    @GetMapping("/api/v1/utilisateurs")
    @Transactional(readOnly = true)
    public List<UtilisateurResponse> getAllUtilisateurs() {
        return utilisateurRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @PostMapping("/api/v1/utilisateurs")
    @Transactional
    public UtilisateurResponse creerUtilisateur(@RequestBody UtilisateurRequest request) {
        Utilisateur utilisateur = new Utilisateur();

        utilisateur.setNom(request.nom());
        utilisateur.setPrenom(request.prenom());
        utilisateur.setEmail(request.email());

        // CORRECTION DE SÉCURITÉ : le mot de passe était stocké en
        // clair ici (utilisateur.setMotDePasse(request.motDePasse())),
        // ce qui rendait la connexion impossible une fois AuthController
        // corrigé pour vérifier un hash BCrypt (bcrypt.matches(clair,
        // clair) échoue toujours). Le mot de passe est maintenant
        // haché avant persistance, comme le fait déjà
        // AuthController.register() pour /api/v1/auth/inscription.
        utilisateur.setMotDePasse(utilisateurService.hacherMotDePasse(request.motDePasse()));

        utilisateur.setTelephone(request.telephone());
        utilisateur.setAvatar(request.avatar());
        utilisateur.setEstActif(true);

        if (request.role() != null && !request.role().isBlank()) {
            utilisateur.setRole(RoleUtilisateur.valueOf(request.role()));
        }

        Utilisateur saved = utilisateurRepository.save(utilisateur);

        return toResponse(saved);
    }

    private UserProfileResponse toProfileResponse(Utilisateur utilisateur) {
        return new UserProfileResponse(
                utilisateur.getIdUser(),
                utilisateur.getNom(),
                utilisateur.getPrenom(),
                utilisateur.getEmail(),
                utilisateur.getTelephone(),
                utilisateur.getAvatar(),
                utilisateur.getRole().name()
        );
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

    public record UserProfileResponse(
            Long idUser,
            String nom,
            String prenom,
            String email,
            String telephone,
            String avatar,
            String role
    ) {
    }

    public record UpdateProfileRequest(
            String nom,
            String prenom,
            String telephone,
            String avatar
    ) {
    }
}
