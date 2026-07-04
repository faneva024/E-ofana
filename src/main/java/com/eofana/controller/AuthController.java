package com.eofana.controller;

import com.eofana.dto.AuthResponse;
import com.eofana.dto.LoginRequest;
import com.eofana.dto.RegisterRequest;
import com.eofana.entity.Utilisateur;
import com.eofana.enums.RoleUtilisateur;
import com.eofana.repository.UtilisateurRepository;
import com.eofana.security.JwtService;
import com.eofana.service.UtilisateurService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

/**
 * Contrôleur d'authentification (T-B-009).
 *
 * CORRECTION DE SÉCURITÉ IMPORTANTE : la version précédente comparait
 * le mot de passe en clair avec `.equals()` alors qu'un
 * BCryptPasswordEncoder (via UtilisateurService.verifierMotDePasse)
 * existait déjà mais n'était jamais appelé, et renvoyait un faux
 * jeton "TOKEN-DEMO-{id}" sans aucune vérification côté serveur.
 * Les deux problèmes sont corrigés ici : vérification BCrypt réelle
 * + émission d'un vrai JWT signé (voir JwtService).
 *
 * Ajout de la route d'inscription (register), absente jusqu'ici.
 */
@RestController
@RequestMapping("/api/v1/auth")
@CrossOrigin(origins = {
        "http://localhost:5173",
        "http://localhost:5174"
})
public class AuthController {

    private final UtilisateurRepository utilisateurRepository;
    private final UtilisateurService utilisateurService;
    private final JwtService jwtService;

    public AuthController(UtilisateurRepository utilisateurRepository,
                           UtilisateurService utilisateurService,
                           JwtService jwtService) {
        this.utilisateurRepository = utilisateurRepository;
        this.utilisateurService = utilisateurService;
        this.jwtService = jwtService;
    }

    @PostMapping("/inscription")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {

        if (utilisateurService.emailDejaUtilise(request.email())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(
                    "message", "Un compte existe déjà avec cet email"
            ));
        }

        Utilisateur utilisateur = Utilisateur.builder()
                .nom(request.nom())
                .prenom(request.prenom())
                .email(request.email())
                .motDePasse(utilisateurService.hacherMotDePasse(request.motDePasse()))
                .telephone(request.telephone())
                .role(RoleUtilisateur.apprenant)
                .estActif(true)
                .build();

        Utilisateur saved = utilisateurRepository.save(utilisateur);

        String token = jwtService.generateToken(saved);

        return ResponseEntity.status(HttpStatus.CREATED).body(new AuthResponse(
                token,
                saved.getIdUser(),
                saved.getNom(),
                saved.getPrenom(),
                saved.getEmail(),
                saved.getRole().name()
        ));
    }

    @PostMapping("/connexion")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {

        Optional<Utilisateur> utilisateurOptional = utilisateurService.chercherParEmail(request.email());

        if (utilisateurOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                    "message", "Email ou mot de passe incorrect"
            ));
        }

        Utilisateur utilisateur = utilisateurOptional.get();

        // Vérification BCrypt réelle (plus de comparaison en clair)
        if (!utilisateurService.verifierMotDePasse(request.motDePasse(), utilisateur)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                    "message", "Email ou mot de passe incorrect"
            ));
        }

        if (Boolean.FALSE.equals(utilisateur.getEstActif())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of(
                    "message", "Ce compte est suspendu"
            ));
        }

        String token = jwtService.generateToken(utilisateur);

        return ResponseEntity.ok(new AuthResponse(
                token,
                utilisateur.getIdUser(),
                utilisateur.getNom(),
                utilisateur.getPrenom(),
                utilisateur.getEmail(),
                utilisateur.getRole().name()
        ));
    }

    /**
     * Déconnexion : dans un schéma JWT stateless, il n'y a rien à
     * invalider côté serveur (pas de session). Le frontend doit
     * simplement supprimer le token stocké côté client. Cette route
     * existe pour la symétrie de l'API et pour un futur mécanisme de
     * liste noire de tokens si le besoin apparaît.
     */
    @PostMapping("/deconnexion")
    public ResponseEntity<?> logout() {
        return ResponseEntity.ok(Map.of("message", "Déconnexion réussie"));
    }
}
