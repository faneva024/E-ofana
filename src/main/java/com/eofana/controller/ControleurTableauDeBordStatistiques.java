package com.eofana.controller;

import com.eofana.entity.Centre;
import com.eofana.entity.Formation;
import com.eofana.enums.StatutFormation;
import com.eofana.repository.CentreRepository;
import com.eofana.repository.FormationRepository;
import com.eofana.repository.SessionFormationRepository;
import com.eofana.repository.VisiteFormationRepository;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;

/**
 * ControleurTableauDeBordStatistiques — T-B-109
 * Branche : Back → part-Rolph  |  Semaine 2
 *
 * Statistiques globales et par formation pour le formateur.
 * Aligné sur le projet réel :
 *   - formation.getCentre() pour identifier le formateur
 *   - SessionFormationRepository.calculerPlacesRestantes() existante
 *   - VisiteFormationRepository.countByFormation_IdFormation() (V13)
 */
@RestController
@RequestMapping("/api/v1/formateurs/tableau-de-bord")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:5174"})
public class ControleurTableauDeBordStatistiques {

    private final FormationRepository       formationRepository;
    private final CentreRepository          centreRepository;
    private final SessionFormationRepository sessionFormationRepository;
    private final VisiteFormationRepository  visiteFormationRepository;

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

    // ── Récupérer le centre du formateur ─────────────────────────────────────
    private Centre getCentreFormateur(Long idUser) {
        return centreRepository.findByUtilisateur_IdUser(idUser)
            .orElseThrow(() -> new RuntimeException("Aucun centre trouvé pour ce formateur."));
    }

    // ════════════════════════════════════════════════════════════════════════
    // GET /api/v1/formateurs/tableau-de-bord
    // Résumé global : formations, visites, inscrits, CA du mois
    // ════════════════════════════════════════════════════════════════════════
    @GetMapping
    @Transactional(readOnly = true)
    public ResponseEntity<?> tableauDeBordGlobal(HttpServletRequest request) {

        Long idUser = extraireIdUser(request);
        Centre centre = getCentreFormateur(idUser);
        Long idCentre = centre.getIdCentre();

        List<Formation> formations = formationRepository.findByCentre_IdCentre(idCentre);

        long nbFormationsTotal    = formations.size();
        long nbFormationsApprouv  = formations.stream()
            .filter(f -> StatutFormation.approuve.equals(f.getStatut())).count();
        long nbFormationsAttente  = formations.stream()
            .filter(f -> StatutFormation.enAttente.equals(f.getStatut())).count();

        // Total des visites sur toutes les formations du centre
        long totalVisites = visiteFormationRepository
            .countTotalVisitesParCentre(idCentre);

        // Calcul CA net du mois en cours
        // commission = tauxCommission du centre (BigDecimal)
        BigDecimal taux = centre.getTauxCommission()
            .divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);

        long totalInscrits = 0;
        BigDecimal chiffreAffairesNet = BigDecimal.ZERO;

        for (Formation f : formations) {
            if (!StatutFormation.approuve.equals(f.getStatut())) continue;

            Integer placesRestantes = sessionFormationRepository
                .calculerPlacesRestantes(f.getIdFormation());
            Integer placesTotal = sessionFormationRepository
                .findByFormation_IdFormation(f.getIdFormation())
                .stream().mapToInt(s -> s.getPlacesTotal()).sum();

            int inscrits = Math.max(0, placesTotal - (placesRestantes != null ? placesRestantes : placesTotal));
            totalInscrits += inscrits;

            if (inscrits > 0) {
                BigDecimal prixBase = f.getPrixRemise() != null
                    ? BigDecimal.valueOf(f.getPrixRemise())
                    : BigDecimal.valueOf(f.getPrix());
                BigDecimal commission = prixBase.multiply(taux).setScale(2, RoundingMode.HALF_UP);
                BigDecimal net = prixBase.subtract(commission).multiply(BigDecimal.valueOf(inscrits));
                chiffreAffairesNet = chiffreAffairesNet.add(net);
            }
        }

        double tauxInscription = totalVisites > 0
            ? Math.round((totalInscrits * 10000.0) / totalVisites) / 100.0
            : 0.0;

        return ResponseEntity.ok(Map.of(
            "nomCentre",              centre.getNom(),
            "typeAbonnement",         centre.getAbonnement().name(),
            "tauxCommission",         centre.getTauxCommission(),
            "nbFormationsTotal",      nbFormationsTotal,
            "nbFormationsApprouvees", nbFormationsApprouv,
            "nbFormationsEnAttente",  nbFormationsAttente,
            "totalVisites",           totalVisites,
            "totalInscrits",          totalInscrits,
            "tauxInscriptionPourcent",tauxInscription,
            "chiffreAffairesNetAr",   chiffreAffairesNet
        ));
    }

    // ════════════════════════════════════════════════════════════════════════
    // GET /api/v1/formateurs/tableau-de-bord/formations/{id}
    // Statistiques détaillées pour une formation spécifique
    // ════════════════════════════════════════════════════════════════════════
    @GetMapping("/formations/{id}")
    @Transactional(readOnly = true)
    public ResponseEntity<?> tableauDeBordFormation(
            @PathVariable Long id,
            HttpServletRequest request) {

        Long idUser = extraireIdUser(request);
        Centre centre = getCentreFormateur(idUser);

        Formation formation = formationRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Formation introuvable."));

        // Vérifier appartenance via getCentre()
        if (!formation.getCentre().getIdCentre().equals(centre.getIdCentre())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(Map.of("erreur", "Cette formation ne vous appartient pas."));
        }

        long nbVisites = visiteFormationRepository
            .countByFormation_IdFormation(id);

        Integer placesRestantes = sessionFormationRepository
            .calculerPlacesRestantes(id);
        int totalPlaces = sessionFormationRepository
            .findByFormation_IdFormation(id)
            .stream().mapToInt(s -> s.getPlacesTotal()).sum();

        int nbInscrits = Math.max(0, totalPlaces - (placesRestantes != null ? placesRestantes : totalPlaces));

        BigDecimal prixBase = formation.getPrixRemise() != null
            ? BigDecimal.valueOf(formation.getPrixRemise())
            : BigDecimal.valueOf(formation.getPrix());

        BigDecimal taux = centre.getTauxCommission()
            .divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);
        BigDecimal commission = prixBase.multiply(taux).setScale(2, RoundingMode.HALF_UP);
        BigDecimal revenusGeneres = prixBase.subtract(commission)
            .multiply(BigDecimal.valueOf(nbInscrits));

        double tauxInscription = nbVisites > 0
            ? Math.round((nbInscrits * 10000.0) / nbVisites) / 100.0
            : 0.0;

        return ResponseEntity.ok(Map.of(
            "idFormation",     formation.getIdFormation(),
            "titre",           formation.getTitre(),
            "statut",          formation.getStatut().name(),
            "nbVisites",       nbVisites,
            "nbInscrits",      nbInscrits,
            "placesRestantes", placesRestantes != null ? placesRestantes : 0,
            "tauxInscription", tauxInscription,
            "revenusGeneresAr",revenusGeneres
        ));
    }
}
