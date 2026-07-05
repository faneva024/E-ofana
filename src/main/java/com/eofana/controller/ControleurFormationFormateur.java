package com.eofana.controller;

import com.eofana.dto.FormationFormateurRequete;
import com.eofana.entity.Categorie;
import com.eofana.entity.Centre;
import com.eofana.entity.Formation;
import com.eofana.enums.StatutFormation;
import com.eofana.repository.CategorieRepository;
import com.eofana.repository.CentreRepository;
import com.eofana.repository.FormationRepository;
import com.eofana.repository.SessionFormationRepository;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * ControleurFormationFormateur — T-B-108
 * Branche : Back → part-Rolph  |  Semaine 2
 *
 * Gère la publication et la gestion des formations côté formateur.
 * Aligné sur le projet réel :
 *   - formation.getCentre().getUtilisateur() pour identifier le propriétaire
 *   - findByCentre_IdCentre() (méthode réelle de FormationRepository)
 *   - Token provisoire "TOKEN-DEMO-{idUser}" en attendant le JWT complet (SANDA T-B-006)
 */
@RestController
@RequestMapping("/api/v1/formations")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:5174"})
public class ControleurFormationFormateur {

    private final FormationRepository       formationRepository;
    private final CentreRepository          centreRepository;
    private final CategorieRepository       categorieRepository;
    private final SessionFormationRepository sessionFormationRepository;

    // ── Extraire l'idUser du token provisoire ────────────────────────────────
    private Long extraireIdUser(HttpServletRequest request) {
        String auth = request.getHeader("Authorization");
        if (auth == null || !auth.startsWith("Bearer TOKEN-DEMO-")) {
            throw new RuntimeException("Token manquant ou invalide.");
        }
        try {
            return Long.parseLong(auth.replace("Bearer TOKEN-DEMO-", "").trim());
        } catch (NumberFormatException e) {
            throw new RuntimeException("Token invalide.");
        }
    }

    // ── Récupérer le centre du formateur connecté ────────────────────────────
    private Centre getCentreFormateur(Long idUser) {
        return centreRepository.findByUtilisateur_IdUser(idUser)
            .orElseThrow(() -> new RuntimeException(
                "Aucun centre trouvé. Créez votre profil centre d'abord."));
    }

    // ── Vérifier propriété via formation.getCentre().getUtilisateur() ────────
    private void verifierPropriete(Formation formation, Long idUser) {
        if (!formation.getCentre().getUtilisateur().getIdUser().equals(idUser)) {
            throw new SecurityException("Cette formation ne vous appartient pas.");
        }
    }

    // ════════════════════════════════════════════════════════════════════════
    // POST /api/v1/formations — Publier une nouvelle formation
    // ════════════════════════════════════════════════════════════════════════
    @PostMapping
    @Transactional
    public ResponseEntity<?> publierFormation(
            @Valid @RequestBody FormationFormateurRequete requete,
            HttpServletRequest request) {

        Long idUser = extraireIdUser(request);
        Centre centre = getCentreFormateur(idUser);

        Categorie categorie = null;
        if (requete.categorie() != null && !requete.categorie().isBlank()) {
            categorie = categorieRepository.findByNom(requete.categorie()).orElse(null);
        }

        Formation formation = Formation.builder()
            .centre(centre)
            .categorie(categorie)
            .titre(requete.titre())
            .description(requete.description())
            .lieu(requete.lieu())
            .duree(requete.duree())
            .prix(requete.prix())
            .prixRemise(requete.prixRemise())
            .image(requete.image())
            .statut(StatutFormation.enAttente)
            .build();

        Formation sauvegardee = formationRepository.save(formation);

        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
            "message",     "Formation soumise. En attente de validation par le modérateur.",
            "idFormation", sauvegardee.getIdFormation(),
            "titre",       sauvegardee.getTitre(),
            "statut",      sauvegardee.getStatut().name()
        ));
    }

    // ════════════════════════════════════════════════════════════════════════
    // PUT /api/v1/formations/{id} — Modifier une formation existante
    // ════════════════════════════════════════════════════════════════════════
    @PutMapping("/{id}")
    @Transactional
    public ResponseEntity<?> modifierFormation(
            @PathVariable Long id,
            @Valid @RequestBody FormationFormateurRequete requete,
            HttpServletRequest request) {

        Long idUser = extraireIdUser(request);
        Formation formation = formationRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Formation introuvable."));

        try {
            verifierPropriete(formation, idUser);
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("erreur", e.getMessage()));
        }

        StatutFormation ancienStatut = formation.getStatut();

        formation.setTitre(requete.titre());
        formation.setDescription(requete.description());
        formation.setLieu(requete.lieu());
        formation.setDuree(requete.duree());
        formation.setPrix(requete.prix());
        formation.setPrixRemise(requete.prixRemise());
        if (requete.image() != null) formation.setImage(requete.image());

        // Toute modification repasse en enAttente
        if (StatutFormation.approuve.equals(ancienStatut)
         || StatutFormation.rejete.equals(ancienStatut)
         || StatutFormation.correctionDemandee.equals(ancienStatut)) {
            formation.setStatut(StatutFormation.enAttente);
        }

        formationRepository.save(formation);

        return ResponseEntity.ok(Map.of(
            "message",       "Formation mise à jour." +
                             (StatutFormation.approuve.equals(ancienStatut)
                              ? " Elle repassera en validation." : ""),
            "idFormation",   formation.getIdFormation(),
            "nouveauStatut", formation.getStatut().name(),
            "ancienStatut",  ancienStatut.name()
        ));
    }

    // ════════════════════════════════════════════════════════════════════════
    // GET /api/v1/formations/mes-formations — Lister mes formations
    // ════════════════════════════════════════════════════════════════════════
    @GetMapping("/mes-formations")
    @Transactional(readOnly = true)
    public ResponseEntity<?> mesFormations(
            @RequestParam(required = false) String statut,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int taille,
            HttpServletRequest request) {

        Long idUser = extraireIdUser(request);
        Centre centre = getCentreFormateur(idUser);

        var pageable = PageRequest.of(page, taille, Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<Formation> formations = (statut != null && !statut.isBlank())
            ? formationRepository.findByCentre_IdCentreAndStatut(
                centre.getIdCentre(), StatutFormation.valueOf(statut), pageable)
            : formationRepository.findByCentre_IdCentre(centre.getIdCentre(), pageable);

        List<Map<String, Object>> liste = formations.getContent().stream().map(f -> {
            Integer placesRestantes = sessionFormationRepository
                .calculerPlacesRestantes(f.getIdFormation());
            return Map.<String, Object>of(
                "idFormation",     f.getIdFormation(),
                "titre",           f.getTitre(),
                "lieu",            f.getLieu() != null ? f.getLieu() : "",
                "prix",            f.getPrix(),
                "statut",          f.getStatut().name(),
                "placesRestantes", placesRestantes != null ? placesRestantes : 0
            );
        }).toList();

        return ResponseEntity.ok(Map.of(
            "total",       formations.getTotalElements(),
            "pages",       formations.getTotalPages(),
            "pageCourante",formations.getNumber(),
            "formations",  liste
        ));
    }

    // ════════════════════════════════════════════════════════════════════════
    // DELETE /api/v1/formations/{id} — Supprimer ou archiver une formation
    // ════════════════════════════════════════════════════════════════════════
    @DeleteMapping("/{id}")
    @Transactional
    public ResponseEntity<?> supprimerFormation(
            @PathVariable Long id,
            HttpServletRequest request) {

        Long idUser = extraireIdUser(request);
        Formation formation = formationRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Formation introuvable."));

        try {
            verifierPropriete(formation, idUser);
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("erreur", e.getMessage()));
        }

        // Si des sessions existent → archiver plutôt que supprimer
        var sessions = sessionFormationRepository.findByFormation_IdFormation(id);
        if (!sessions.isEmpty()) {
            formation.setStatut(StatutFormation.archive);
            formationRepository.save(formation);
            return ResponseEntity.ok(Map.of(
                "message",     "Formation archivée (des sessions existent).",
                "idFormation", formation.getIdFormation(),
                "statut",      "archive"
            ));
        }

        formationRepository.delete(formation);
        return ResponseEntity.ok(Map.of(
            "message",     "Formation supprimée définitivement.",
            "idFormation", id
        ));
    }
}
