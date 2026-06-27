package com.eofana.controller;

import com.eofana.dto.AuthReponse;
import com.eofana.dto.ConnexionRequete;
import com.eofana.dto.UtilisateurInscriptionRequete;
import com.eofana.entity.Utilisateur;
import com.eofana.repository.UtilisateurRepository;
import com.eofana.service.ServiceJwt;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
@CrossOrigin(origins = "*")
public class ControleurAuthentification {

    private final UtilisateurRepository utilisateurRepository;
    private final PasswordEncoder passwordEncoder;
    private final ServiceJwt serviceJwt;
    private final AuthenticationManager authenticationManager;

    public ControleurAuthentification(UtilisateurRepository utilisateurRepository,
                                       PasswordEncoder passwordEncoder,
                                       ServiceJwt serviceJwt,
                                       AuthenticationManager authenticationManager) {
        this.utilisateurRepository = utilisateurRepository;
        this.passwordEncoder = passwordEncoder;
        this.serviceJwt = serviceJwt;
        this.authenticationManager = authenticationManager;
    }

    @PostMapping("/inscription")
    public ResponseEntity<?> inscrire(@Valid @RequestBody UtilisateurInscriptionRequete requete) {
        if (utilisateurRepository.existsByEmail(requete.getEmail())) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("erreur", "Cet email est déjà utilisé"));
        }

        Utilisateur utilisateur = new Utilisateur();
        utilisateur.setEmail(requete.getEmail());
        utilisateur.setMotDePasse(passwordEncoder.encode(requete.getMotDePasse()));
        utilisateur.setPrenom(requete.getPrenom());
        utilisateur.setNom(requete.getNom());
        utilisateur.setTelephone(requete.getTelephone());

        Utilisateur sauvegarde = utilisateurRepository.save(utilisateur);
        String token = serviceJwt.genererToken(sauvegarde.getEmail());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new AuthReponse(sauvegarde.getIdUser(), token, sauvegarde.getEmail(),
                        sauvegarde.getPrenom(), sauvegarde.getNom()));
    }

    @PostMapping("/connexion")
    public ResponseEntity<?> connecter(@Valid @RequestBody ConnexionRequete requete) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(requete.getEmail(), requete.getMotDePasse()));
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("erreur", "Email ou mot de passe incorrect"));
        }

        Utilisateur utilisateur = utilisateurRepository.findByEmail(requete.getEmail())
                .orElseThrow();
        String token = serviceJwt.genererToken(utilisateur.getEmail());

        return ResponseEntity.ok(new AuthReponse(utilisateur.getIdUser(), token, utilisateur.getEmail(),
                utilisateur.getPrenom(), utilisateur.getNom()));
    }

    @PostMapping("/deconnexion")
    public ResponseEntity<?> deconnecter() {
        // JWT est stateless : la déconnexion se fait côté client
        return ResponseEntity.ok(Map.of("message", "Déconnexion réussie. Supprimez le token côté client."));
    }
}
