package com.eofana.controller;

import com.eofana.entity.Abonnement;
import com.eofana.entity.Centre;
import com.eofana.enums.AbonnementType;
import com.eofana.repository.AbonnementRepository;
import com.eofana.repository.CentreRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * T-B-208 : gestion admin de l'abonnement d'un centre (Basic ↔ Premium).
 *
 * {id} désigne l'idCentre, même convention que FormateurAccountController
 * ("/admin/formateurs/{id}/..."), puisque l'abonnement appartient au
 * centre et non directement au compte Utilisateur du formateur.
 */
@RestController
@RequestMapping("/api/v1/admin/formateurs/{id}")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:5174"})
public class AbonnementController {

    private final CentreRepository centreRepository;
    private final AbonnementRepository abonnementRepository;

    @PersistenceContext
    private EntityManager entityManager;

    /**
     * Change le type d'abonnement d'un centre (Basic ↔ Premium).
     *
     * Étapes :
     *  1. clôture l'enregistrement "abonnements" actuellement ouvert
     *     (dateFin = aujourd'hui), s'il existe ;
     *  2. met à jour centres.abonnement — le trigger PostgreSQL
     *     "trgSyncTauxCommission" (V8) recalcule automatiquement
     *     centres.tauxCommission ; cette logique n'est JAMAIS
     *     dupliquée côté application, conformément au ticket ;
     *  3. relit le centre après flush (le trigger s'exécute côté
     *     base, Hibernate ne connaît pas la nouvelle valeur tant
     *     qu'on ne la relit pas) pour connaître le tauxCommission
     *     réellement appliqué ;
     *  4. insère une nouvelle ligne d'historique dans "abonnements"
     *     avec ce taux, ouverte (dateFin = null).
     */
    @PutMapping("/abonnement")
    @Transactional
    public ResponseEntity<?> changeSubscription(@PathVariable Long id, @RequestBody ChangeSubscriptionRequest request) {
        if (request.type() == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "Le type d'abonnement est obligatoire"));
        }

        Centre centre = centreRepository.findById(id).orElse(null);
        if (centre == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "Centre introuvable"));
        }

        if (centre.getAbonnement() == request.type()) {
            return ResponseEntity.badRequest().body(Map.of(
                    "message", "Ce centre est déjà abonné au plan " + request.type().name()));
        }

        abonnementRepository.findByCentreIdCentreAndDateFinIsNull(id)
                .ifPresent(ancien -> {
                    ancien.setDateFin(LocalDate.now());
                    abonnementRepository.save(ancien);
                });

        centre.setAbonnement(request.type());
        centre = centreRepository.saveAndFlush(centre);
        // Le trigger BEFORE UPDATE a recalculé tauxCommission en base ;
        // on relit l'entité pour récupérer cette valeur plutôt que de
        // recoder la règle 7.00/15.00 côté Java.
        entityManager.refresh(centre);

        Abonnement nouvelAbonnement = Abonnement.builder()
                .centre(centre)
                .type(request.type())
                .tauxCommission(centre.getTauxCommission())
                .dateDebut(LocalDate.now())
                .build();
        abonnementRepository.save(nouvelAbonnement);

        return ResponseEntity.ok(Map.of(
                "message", "Abonnement mis à jour",
                "idCentre", centre.getIdCentre(),
                "abonnement", centre.getAbonnement().name(),
                "tauxCommission", centre.getTauxCommission()
        ));
    }

    /** Historique complet des abonnements souscrits par un centre, du plus récent au plus ancien. */
    @GetMapping("/abonnements/historique")
    @Transactional(readOnly = true)
    public ResponseEntity<?> getSubscriptionHistory(@PathVariable Long id) {
        if (!centreRepository.existsById(id)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "Centre introuvable"));
        }

        List<Abonnement> historique = abonnementRepository.findByCentreIdCentreOrderByDateDebutDesc(id);

        List<AbonnementHistoriqueResponse> reponse = historique.stream()
                .map(a -> new AbonnementHistoriqueResponse(
                        a.getIdAbonnement(),
                        a.getType().name(),
                        a.getTauxCommission(),
                        a.getDateDebut(),
                        a.getDateFin()
                ))
                .toList();

        return ResponseEntity.ok(reponse);
    }

    public record ChangeSubscriptionRequest(AbonnementType type) {
    }

    public record AbonnementHistoriqueResponse(
            Long idAbonnement,
            String type,
            BigDecimal tauxCommission,
            LocalDate dateDebut,
            LocalDate dateFin
    ) {
    }
}
