package com.eofana.controller;

import com.eofana.entity.Utilisateur;
import com.eofana.repository.UtilisateurRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/auth")
@CrossOrigin(origins = {
        "http://localhost:5173",
        "http://localhost:5174"
})
public class AuthController {

    private final UtilisateurRepository utilisateurRepository;

    public AuthController(UtilisateurRepository utilisateurRepository) {
        this.utilisateurRepository = utilisateurRepository;
    }

    @PostMapping("/connexion")
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