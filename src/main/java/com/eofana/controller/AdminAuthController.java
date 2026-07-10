package com.eofana.controller;

import com.eofana.entity.Utilisateur;
import com.eofana.enums.RoleUtilisateur;
import com.eofana.repository.UtilisateurRepository;
import com.eofana.service.UtilisateurService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * T-B-204 : connexion admin, espace distinct des formateurs/apprenants.
 *
 * Le ticket demande explicitement un fichier séparé
 * "AdminAuthController.java" (contrairement à loginFormateur(),
 * consolidée dans AuthController par T-B-106 pour éviter un doublon
 * de dépendances) : on respecte ce choix pour garder l'espace
 * d'authentification admin architecturalement isolé du contrôleur
 * public partagé apprenant/formateur — cohérent avec le principe du
 * module ("espace distinct des formateurs et apprenants").
 *
 * Le token retourné est un "TOKEN-DEMO-ADMIN-{idUser}", même famille
 * que "TOKEN-DEMO-{idUser}" et "TOKEN-DEMO-FORMATEUR-{idUser}" déjà
 * utilisés dans ce projet — pas de vraie infrastructure JWT pour
 * l'instant (voir SecurityFilterConfig) malgré le libellé "token JWT"
 * du ticket ; AdminContextResolver (T-B-215) reconnaît ce préfixe
 * pour authentifier les autres contrôleurs admin.
 */
@RestController
@RequestMapping("/api/v1/auth-admin")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:5174"})
public class AdminAuthController {

    private final UtilisateurRepository utilisateurRepository;
    private final UtilisateurService utilisateurService;

    /**
     * Connexion admin : email + mot de passe, filtré strictement sur
     * role = ADMIN (un compte apprenant/formateur avec les mêmes
     * identifiants ne doit jamais pouvoir se connecter ici), vérifie
     * estActif.
     */
    @PostMapping("/connexion")
    public ResponseEntity<?> login(@RequestBody ConnexionRequest request) {
        if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "L'email est obligatoire"));
        }
        if (request.getMotDePasse() == null || request.getMotDePasse().trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Le mot de passe est obligatoire"));
        }

        Optional<Utilisateur> utilisateurOptional =
                utilisateurRepository.findByEmailAndRole(request.getEmail(), RoleUtilisateur.admin);

        if (utilisateurOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Email ou mot de passe incorrect"));
        }

        Utilisateur admin = utilisateurOptional.get();

        if (!utilisateurService.verifierMotDePasse(request.getMotDePasse(), admin)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Email ou mot de passe incorrect"));
        }

        if (Boolean.FALSE.equals(admin.getEstActif())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", "Ce compte administrateur a été suspendu"));
        }

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Connexion réussie");
        response.put("idUser", admin.getIdUser());
        response.put("nom", admin.getNom());
        response.put("prenom", admin.getPrenom());
        response.put("email", admin.getEmail());
        response.put("typeUtilisateur", RoleUtilisateur.admin.name());
        response.put("token", "TOKEN-DEMO-ADMIN-" + admin.getIdUser());

        return ResponseEntity.ok(response);
    }

    /**
     * Déconnexion admin : comme logoutFormateur(), sans session
     * serveur ni JWT actif il n'y a rien à invalider côté backend —
     * ce endpoint donne au frontend un point d'appel stable.
     */
    @PostMapping("/deconnexion")
    public ResponseEntity<?> logout() {
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
