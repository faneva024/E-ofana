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

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Contrôleur d'authentification unique (Semaine 2 - SANDA).
 * Regroupe l'authentification des Apprenants (/api/v1/auth/...)
 * et l'authentification exclusive des Formateurs (/api/v1/auth-formateur/...).
 */
@RestController
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

    // ==========================================
    // MODULE APPRENANT (Semaine 1)
    // ==========================================

    @PostMapping("/api/v1/auth/inscription")
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

    @PostMapping("/api/v1/auth/connexion")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {

        Optional<Utilisateur> utilisateurOptional = utilisateurService.chercherParEmail(request.email());

        if (utilisateurOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                    "message", "Email ou mot de passe incorrect"
            ));
        }

        Utilisateur utilisateur = utilisateurOptional.get();

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

    @PostMapping("/api/v1/auth/deconnexion")
    public ResponseEntity<?> logout() {
        return ResponseEntity.ok(Map.of("message", "Déconnexion réussie"));
    }

    // ==========================================
    // MODULE FORMATEUR - SANDA (Semaine 2 - T-B-106)
    // ==========================================

    /**
     * Authentification réservée aux formateurs.
     * Pas d'inscription disponible (les comptes sont gérés par le commercial).
     */
    @PostMapping("/api/v1/auth-formateur/connexion")
    public ResponseEntity<?> loginFormateur(@Valid @RequestBody LoginRequest request) {

        // 1. Recherche par email
        Optional<Utilisateur> utilisateurOptional = utilisateurService.chercherParEmail(request.email());

        if (utilisateurOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                    "message", "Email ou mot de passe incorrect"
            ));
        }

        Utilisateur utilisateur = utilisateurOptional.get();

        // 2. Filtrage strict du rôle (Doit être FORMATEUR ou formateur selon la casse de l'enum)
        if (utilisateur.getRole() != RoleUtilisateur.formateur && utilisateur.getRole() != RoleUtilisateur.formateur) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of(
                    "message", "Accès refusé : vous n'êtes pas un formateur"
            ));
        }

        // 3. Vérification du mot de passe via BCrypt
        if (!utilisateurService.verifierMotDePasse(request.motDePasse(), utilisateur)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                    "message", "Email ou mot de passe incorrect"
            ));
        }

        // 4. Vérification explicite du statut (Compte suspendu)
        if (Boolean.FALSE.equals(utilisateur.getEstActif())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of(
                    "message", "Votre compte formateur est suspendu. Veuillez contacter le service commercial"
            ));
        }

        // 5. Ajout du claim typeUtilisateur demandé pour la gestion multirôle du filtre JWT
        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("typeUtilisateur", "FORMATEUR");

        // 6. Génération du token signé avec les claims additionnels
        String token = jwtService.generateToken(extraClaims, utilisateur);

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
     * Déconnexion côté formateur (Suppression locale côté client).
     */
    @PostMapping("/api/v1/auth-formateur/deconnexion")
    public ResponseEntity<?> logoutFormateur() {
        return ResponseEntity.ok(Map.of("message", "Déconnexion formateur réussie"));
    }
}