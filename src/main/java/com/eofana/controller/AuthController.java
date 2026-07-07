package com.eofana.controller;

import com.eofana.entity.Utilisateur;
import com.eofana.enums.RoleUtilisateur;
import com.eofana.repository.UtilisateurRepository;
import com.eofana.service.UtilisateurService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Contrôleur d'authentification unique pour toute la plateforme.
 *
 * T-B-106 : plutôt que de créer un FormateurAuthController séparé
 * (doublon), on ajoute ici loginFormateur()/logoutFormateur() qui
 * réutilisent exactement la même dépendance UtilisateurRepository et
 * la même logique de vérification que connexion() ci-dessus — seules
 * les règles métier de connexion diffèrent (filtrage sur role =
 * FORMATEUR, vérification estActif explicite).
 */
@RestController
@RequestMapping("/api/v1")
@CrossOrigin(origins = {
        "http://localhost:5173",
        "http://localhost:5174"
})
public class AuthController {

    private final UtilisateurRepository utilisateurRepository;
    private final UtilisateurService utilisateurService;

    public AuthController(UtilisateurRepository utilisateurRepository,
                           UtilisateurService utilisateurService) {
        this.utilisateurRepository = utilisateurRepository;
        this.utilisateurService = utilisateurService;
    }

    @PostMapping("/auth/connexion")
    public ResponseEntity<?> connexion(@RequestBody ConnexionRequest request) {

        if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of(
                    "message", "L'email est obligatoire"
            ));
        }

        if (request.getMotDePasse() == null || request.getMotDePasse().trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of(
                    "message", "Le mot de passe est obligatoire"
            ));
        }

        Optional<Utilisateur> utilisateurOptional =
                utilisateurRepository.findByEmail(request.getEmail());

        if (utilisateurOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                    "message", "Email ou mot de passe incorrect"
            ));
        }

        Utilisateur utilisateur = utilisateurOptional.get();

        if (!request.getMotDePasse().equals(utilisateur.getMotDePasse())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                    "message", "Email ou mot de passe incorrect"
            ));
        }

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Connexion réussie");
        response.put("idUser", utilisateur.getIdUser());
        response.put("nom", utilisateur.getNom());
        response.put("prenom", utilisateur.getPrenom());
        response.put("email", utilisateur.getEmail());
        response.put("telephone", utilisateur.getTelephone());
        response.put("role", utilisateur.getRole());

        // Token simple temporaire pour le frontend
        response.put("token", "TOKEN-DEMO-" + utilisateur.getIdUser());

        return ResponseEntity.ok(response);
    }

    /**
     * T-B-106 : connexion formateur.
     *
     * Filtre explicitement sur role = FORMATEUR : un compte apprenant
     * ou admin avec les mêmes identifiants ne doit jamais pouvoir se
     * connecter par ce endpoint. Aucune route d'inscription
     * formateur n'existe (le compte est créé par le commercial,
     * module Admin/Commercial, hors périmètre ici) — contrairement à
     * /api/v1/auth/inscription côté apprenant.
     */
    @PostMapping("/auth-formateur/connexion")
    public ResponseEntity<?> loginFormateur(@RequestBody ConnexionRequest request) {

        if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of(
                    "message", "L'email est obligatoire"
            ));
        }

        if (request.getMotDePasse() == null || request.getMotDePasse().trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of(
                    "message", "Le mot de passe est obligatoire"
            ));
        }

        Optional<Utilisateur> utilisateurOptional = utilisateurRepository
                .findByEmailAndRole(request.getEmail(), RoleUtilisateur.formateur);

        if (utilisateurOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                    "message", "Email ou mot de passe incorrect"
            ));
        }

        Utilisateur formateur = utilisateurOptional.get();

        if (!utilisateurService.verifierMotDePasse(request.getMotDePasse(), formateur)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                    "message", "Email ou mot de passe incorrect"
            ));
        }

        if (Boolean.FALSE.equals(formateur.getEstActif())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of(
                    "message", "Ce compte a été suspendu. Contactez votre commercial E-OFANA."
            ));
        }

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Connexion réussie");
        response.put("idUser", formateur.getIdUser());
        response.put("nom", formateur.getNom());
        response.put("prenom", formateur.getPrenom());
        response.put("email", formateur.getEmail());
        response.put("telephone", formateur.getTelephone());
        response.put("typeUtilisateur", RoleUtilisateur.formateur.name());

        // Token de démo temporaire, cohérent avec /api/v1/auth/connexion :
        // aucune infrastructure JWT n'existe encore dans ce projet
        // (voir SecurityFilterConfig, qui laisse toutes les routes
        // ouvertes pour l'instant). À remplacer par un vrai JWT
        // portant le claim typeUtilisateur=FORMATEUR quand
        // l'authentification réelle sera mise en place.
        response.put("token", "TOKEN-DEMO-FORMATEUR-" + formateur.getIdUser());

        return ResponseEntity.ok(response);
    }

    /**
     * T-B-106 : déconnexion formateur.
     *
     * Sans session serveur ni JWT actif, il n'y a rien à invalider
     * côté backend pour l'instant — le frontend doit simplement
     * supprimer le token stocké localement. Ce endpoint existe pour
     * que le frontend ait un point d'appel stable, et pour qu'une
     * vraie invalidation de token (liste noire JWT) puisse y être
     * branchée plus tard sans changer le contrat d'API.
     */
    @PostMapping("/auth-formateur/deconnexion")
    public ResponseEntity<?> logoutFormateur() {
        return ResponseEntity.ok(Map.of("message", "Déconnexion réussie"));
    }

    public static class ConnexionRequest {
        private String email;
        private String motDePasse;

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getMotDePasse() {
            return motDePasse;
        }

        public void setMotDePasse(String motDePasse) {
            this.motDePasse = motDePasse;
        }
    }
}