package com.eofana.controller;

import com.eofana.dto.ModificationProfilRequete;
import com.eofana.entity.Inscription;
import com.eofana.entity.Utilisateur;
import com.eofana.repository.InscriptionRepository;
import com.eofana.repository.UtilisateurRepository;
import com.eofana.service.ServiceGenerationRecu;
import com.eofana.service.ServiceUtilisateur;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/utilisateurs")
@CrossOrigin(origins = "*")
public class ControleurUtilisateur {

    private final UtilisateurRepository utilisateurRepository;
    private final InscriptionRepository inscriptionRepository;
    private final ServiceUtilisateur serviceUtilisateur;
    private final ServiceGenerationRecu serviceGenerationRecu;
    private final PasswordEncoder passwordEncoder;

    public ControleurUtilisateur(UtilisateurRepository utilisateurRepository,
                                   InscriptionRepository inscriptionRepository,
                                   ServiceUtilisateur serviceUtilisateur,
                                   ServiceGenerationRecu serviceGenerationRecu,
                                   PasswordEncoder passwordEncoder) {
        this.utilisateurRepository = utilisateurRepository;
        this.inscriptionRepository = inscriptionRepository;
        this.serviceUtilisateur = serviceUtilisateur;
        this.serviceGenerationRecu = serviceGenerationRecu;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/profil")
    public ResponseEntity<?> getProfil(@AuthenticationPrincipal UserDetails userDetails) {
        Utilisateur u = serviceUtilisateur.chercherParEmail(userDetails.getUsername());
        return ResponseEntity.ok(Map.of(
                "idUser", u.getIdUser(),
                "email", u.getEmail(),
                "prenom", u.getPrenom(),
                "nom", u.getNom(),
                "telephone", u.getTelephone() != null ? u.getTelephone() : "",
                "createdAt", u.getCreatedAt() != null ? u.getCreatedAt().toString() : ""
        ));
    }

    @PutMapping("/profil")
    public ResponseEntity<?> modifierProfil(@RequestBody ModificationProfilRequete requete,
                                             @AuthenticationPrincipal UserDetails userDetails) {
        Utilisateur u = serviceUtilisateur.chercherParEmail(userDetails.getUsername());

        if (requete.getPrenom() != null && !requete.getPrenom().isBlank()) {
            u.setPrenom(requete.getPrenom());
        }
        if (requete.getNom() != null && !requete.getNom().isBlank()) {
            u.setNom(requete.getNom());
        }
        if (requete.getTelephone() != null) {
            u.setTelephone(requete.getTelephone());
        }
        if (requete.getMotDePasse() != null && requete.getMotDePasse().length() >= 8) {
            u.setMotDePasse(passwordEncoder.encode(requete.getMotDePasse()));
        }

        utilisateurRepository.save(u);
        return ResponseEntity.ok(Map.of("message", "Profil mis à jour avec succès"));
    }

    @GetMapping("/inscriptions")
    public ResponseEntity<List<Inscription>> mesInscriptions(@AuthenticationPrincipal UserDetails userDetails) {
        Utilisateur u = serviceUtilisateur.chercherParEmail(userDetails.getUsername());
        List<Inscription> inscriptions = inscriptionRepository.findByUtilisateurIdUser(u.getIdUser());
        return ResponseEntity.ok(inscriptions);
    }

    @GetMapping("/recus")
    public ResponseEntity<?> historiquesRecus(@AuthenticationPrincipal UserDetails userDetails) {
        Utilisateur u = serviceUtilisateur.chercherParEmail(userDetails.getUsername());
        List<Inscription> inscriptions = inscriptionRepository.findByUtilisateurIdUser(u.getIdUser());

        List<Map<String, Object>> recus = inscriptions.stream()
                .filter(i -> i.getNumeroRecu() != null)
                .map(i -> Map.of(
                        "idInscription", (Object) i.getIdInscription(),
                        "numeroRecu", i.getNumeroRecu(),
                        "formation", i.getFormation().getTitre(),
                        "dateInscription", i.getDateInscription() != null ? i.getDateInscription().toString() : "",
                        "lienTelechargement", "/api/v1/inscriptions/" + i.getIdInscription() + "/recu"
                ))
                .collect(Collectors.toList());

        return ResponseEntity.ok(recus);
    }
}
