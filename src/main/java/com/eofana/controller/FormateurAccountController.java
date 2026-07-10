package com.eofana.controller;

import com.eofana.entity.Centre;
import com.eofana.entity.Utilisateur;
import com.eofana.enums.AbonnementType;
import com.eofana.enums.StatutCentre;
import com.eofana.repository.CentreRepository;
import com.eofana.repository.UtilisateurRepository;
import com.eofana.service.FormateurAccountService;
import com.eofana.service.FormateurAccountService.CreateFormateurDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

/**
 * T-B-207 : gestion admin des comptes formateurs (ex-fonction
 * "commercial" — création de compte + profil centre + abonnement en
 * une seule opération, liste, suspension).
 *
 * Toutes les routes vivent sous "/api/v1/admin/formateurs" : leur
 * protection par rôle ADMIN est déclarative, portée par SecurityConfig
 * (T-B-215), pas par un contrôle manuel ici — même séparation des
 * responsabilités que FormateurContextResolver pour le périmètre
 * formateur.
 */
@RestController
@RequestMapping("/api/v1/admin/formateurs")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:5174"})
public class FormateurAccountController {

    private final FormateurAccountService formateurAccountService;
    private final CentreRepository centreRepository;
    private final UtilisateurRepository utilisateurRepository;

    /**
     * Création complète d'un compte formateur par l'admin (§3.2).
     * Le mot de passe temporaire n'est JAMAIS renvoyé dans la réponse
     * JSON : il est transmis uniquement par email
     * (FormateurAccountService#sendCredentialsByEmail), pour éviter
     * qu'il ne transite ou ne soit journalisé côté HTTP/frontend.
     */
    @PostMapping
    @Transactional
    public ResponseEntity<?> createFormateurAccount(@RequestBody CreateFormateurDto request) {
        String erreur = formateurAccountService.validateRequiredFields(request);
        if (erreur != null) {
            return ResponseEntity.badRequest().body(Map.of("message", erreur));
        }

        Utilisateur formateur = formateurAccountService.createFormateurAccount(request);
        Centre centre = centreRepository.findByUtilisateur_IdUser(formateur.getIdUser())
                .orElseThrow(() -> new IllegalStateException("Centre introuvable juste après sa création"));

        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "message", "Compte formateur créé avec succès. Les identifiants de connexion ont été envoyés par email.",
                "formateur", toSummary(centre, formateur)
        ));
    }

    /**
     * Liste paginée de tous les centres/formateurs, filtrable par
     * nom, ville, statut et abonnement (tous les filtres sont
     * optionnels et combinables, cf. CentreRepository#rechercherFormateurs).
     */
    @GetMapping
    @Transactional(readOnly = true)
    public ResponseEntity<Page<FormateurSummaryResponse>> listFormateurs(
            @RequestParam(required = false) String nom,
            @RequestParam(required = false) String ville,
            @RequestParam(required = false) StatutCentre statut,
            @RequestParam(required = false) AbonnementType abonnement,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int taille) {

        Page<Centre> centres = centreRepository.rechercherFormateurs(
                normaliser(nom), normaliser(ville), statut, abonnement, PageRequest.of(page, taille));

        return ResponseEntity.ok(centres.map(c -> toSummary(c, c.getUtilisateur())));
    }

    /**
     * Suspend le compte d'un formateur : estActif = false sur son
     * Utilisateur. {id} est l'idCentre (identifiant utilisé par
     * toutes les routes /admin/formateurs/{id}/...), résolu vers le
     * compte Utilisateur associé via la relation OneToOne Centre <-> Utilisateur.
     */
    @PutMapping("/{id}/suspendre")
    @Transactional
    public ResponseEntity<?> suspendFormateur(@PathVariable Long id) {
        Centre centre = centreRepository.findById(id).orElse(null);
        if (centre == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "Centre introuvable"));
        }

        Utilisateur formateur = centre.getUtilisateur();
        formateur.setEstActif(false);
        utilisateurRepository.save(formateur);

        return ResponseEntity.ok(Map.of(
                "message", "Le compte formateur a été suspendu",
                "idCentre", centre.getIdCentre(),
                "estActif", formateur.getEstActif()
        ));
    }

    private String normaliser(String valeur) {
        return (valeur == null || valeur.isBlank()) ? null : valeur.trim();
    }

    private FormateurSummaryResponse toSummary(Centre centre, Utilisateur formateur) {
        return new FormateurSummaryResponse(
                centre.getIdCentre(),
                formateur != null ? formateur.getIdUser() : null,
                centre.getNom(),
                formateur != null ? formateur.getNom() : null,
                formateur != null ? formateur.getPrenom() : null,
                formateur != null ? formateur.getEmail() : null,
                centre.getVille(),
                centre.getStatut() != null ? centre.getStatut().name() : null,
                centre.getAbonnement() != null ? centre.getAbonnement().name() : null,
                centre.getTauxCommission(),
                formateur != null ? formateur.getEstActif() : null
        );
    }

    public record FormateurSummaryResponse(
            Long idCentre,
            Long idUser,
            String nomCentre,
            String nomResponsable,
            String prenomResponsable,
            String emailConnexion,
            String ville,
            String statutCentre,
            String abonnement,
            BigDecimal tauxCommission,
            Boolean estActif
    ) {
    }
}
