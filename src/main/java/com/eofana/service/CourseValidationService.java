package com.eofana.service;

import com.eofana.entity.Formation;
import com.eofana.enums.StatutFormation;
import com.eofana.enums.TypeNotif;
import com.eofana.repository.FormationRepository;
import com.eofana.repository.SessionFormationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * Service de validation et de workflow de modération des formations (T-B-114).
 *
 * Workflow rappel (StatutFormation) :
 * brouillon → enAttente → approuve | rejete | correctionDemandee → archive
 *
 * Compatible avec FormationRepository (T-B-104, Faneva) : utilise
 * uniquement .save() (hérité de JpaRepository) et les getters standards
 * de Formation (categorie, statut en StatutFormation, prix, prixRemise,
 * motifRejet), aucune méthode supplémentaire requise côté FormationRepository.
 */
@Service
@RequiredArgsConstructor
public class CourseValidationService {

    private final FormationRepository formationRepository;
    private final SessionFormationRepository sessionFormationRepository;
    private final NotificationService notificationService;

    /**
     
     *
     * @return la liste des erreurs de validation (vide si le formulaire est complet)
     */
    public List<String> validateRequiredFields(Formation formation) {
        List<String> erreurs = new ArrayList<>();

        if (formation.getTitre() == null || formation.getTitre().isBlank()) {
            erreurs.add("Le titre de la formation est obligatoire");
        }
        if (formation.getDescription() == null || formation.getDescription().isBlank()) {
            erreurs.add("La description est obligatoire");
        }
        if (formation.getImage() == null || formation.getImage().isBlank()) {
            erreurs.add("Une image de couverture est obligatoire");
        }
        if (formation.getDuree() == null || formation.getDuree().isBlank()) {
            erreurs.add("La durée est obligatoire");
        }
        if (formation.getLieu() == null || formation.getLieu().isBlank()) {
            erreurs.add("Le lieu est obligatoire");
        }
        if (formation.getCategorie() == null) {
            erreurs.add("La catégorie est obligatoire");
        }
        if (formation.getPrix() == null || formation.getPrix() <= 0) {
            erreurs.add("Le prix doit être renseigné et supérieur à 0");
        }
        if (formation.getPrixRemise() != null && formation.getPrix() != null
                && formation.getPrixRemise() >= formation.getPrix()) {
            erreurs.add("Le prix remisé doit être inférieur au prix normal");
        }
        if (formation.getIdFormation() == null
                || sessionFormationRepository.findByFormation_IdFormation(formation.getIdFormation()).isEmpty()) {
            erreurs.add("La formation doit avoir au moins une session programmée");
        }

        return erreurs;
    }

    /**
     * Place une formation en file d'attente de modération.
     * Rejette la soumission (sans toucher au statut) si le formulaire
     * est incomplet — voir validateRequiredFields().
     *
     * @throws IllegalArgumentException si le formulaire est incomplet,
     *         avec le détail des champs manquants dans le message
     */
    @Transactional
    public Formation submitForValidation(Formation formation) {
        List<String> erreurs = validateRequiredFields(formation);

        if (!erreurs.isEmpty()) {
            throw new IllegalArgumentException(
                    "Formulaire de publication incomplet : " + String.join(" ; ", erreurs));
        }

        formation.setStatut(StatutFormation.enAttente);
        formation.setMotifRejet(null); // on efface un éventuel motif de rejet précédent

        return formationRepository.save(formation);
    }

    /**
     * Notifie le formateur (via son Centre) d'un changement de statut
     * de sa formation — approbation, rejet, ou demande de correction.
     
     */
    public void notifyStatusChange(Formation formation) {
        if (formation.getCentre() == null || formation.getCentre().getUtilisateur() == null) {
            return; // pas de destinataire connu, rien à notifier
        }

        TypeNotif type;
        String message;

        switch (formation.getStatut()) {
            case approuve -> {
                type = TypeNotif.formationApprouvee;
                message = "Votre formation \"" + formation.getTitre() + "\" a été approuvée et est désormais visible dans le catalogue.";
            }
            case rejete -> {
                type = TypeNotif.formationRejetee;
                message = "Votre formation \"" + formation.getTitre() + "\" a été rejetée."
                        + (formation.getMotifRejet() != null ? " Motif : " + formation.getMotifRejet() : "");
            }
            case correctionDemandee -> {
                type = TypeNotif.formationCorrectionDemandee;
                message = "Des corrections sont demandées sur votre formation \"" + formation.getTitre() + "\"."
                        + (formation.getMotifRejet() != null ? " Détail : " + formation.getMotifRejet() : "");
            }
            default -> {
                return; // brouillon / enAttente / archive : pas de notification pertinente ici
            }
        }

        notificationService.envoyer(formation.getCentre().getUtilisateur(), type, message);
    }
}