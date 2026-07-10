package com.eofana.service;

import com.eofana.entity.Formation;
import com.eofana.entity.Utilisateur;
import com.eofana.entity.ValidationFormation;
import com.eofana.enums.StatutFormation;
import com.eofana.enums.TypeNotification;
import com.eofana.repository.FormationRepository;
import com.eofana.repository.ValidationFormationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

/**
 * T-B-114 (module Formateur) + T-B-214 (module Admin) : file d'attente
 * de modération des formations et décisions de l'admin dessus.
 *
 * notifyStatusChange() était volontairement non implémentée tant que
 * le module Admin (table "notifications", V9) n'existait pas — c'est
 * désormais le cas (cf. NotificationService), donc elle est
 * implémentée ici plutôt que de continuer à lever
 * UnsupportedOperationException.
 */
@Service
@RequiredArgsConstructor
public class CourseValidationService {

    private final FormationRepository formationRepository;
    private final ValidationFormationRepository validationFormationRepository;
    private final NotificationService notificationService;

    /** Place une formation en file d'attente de modération. */
    public Formation submitForValidation(Formation formation) {
        formation.setStatut(StatutFormation.enAttente);
        return formationRepository.save(formation);
    }

    /**
     * Valide la complétude du formulaire de publication, conformément
     * au tableau des champs obligatoires du cahier des charges (§3.3) :
     * image, titre, description, catégorie, durée, lieu, date de
     * début, date limite d'inscription, places disponibles, prix.
     * prixRemise reste optionnel.
     *
     * @return null si tout est valide, sinon le premier message d'erreur rencontré
     */
    public String validateRequiredFields(Formation formation) {
        if (isBlank(formation.getTitre())) return "Le titre est obligatoire";
        if (isBlank(formation.getDescription())) return "La description est obligatoire";
        if (isBlank(formation.getImage())) return "L'image est obligatoire";
        if (formation.getCategorie() == null) return "La catégorie est obligatoire";
        if (isBlank(formation.getDuree())) return "La durée est obligatoire";
        if (isBlank(formation.getLieu())) return "Le lieu est obligatoire";
        if (formation.getPrix() == null || formation.getPrix() < 0) return "Le prix est obligatoire";
        return null;
    }

    /**
     * T-B-214 : approuve une formation en attente — statut APPROUVE,
     * formation visible sur la plateforme (cf. la vue "vCatalogue" qui
     * ne renvoie que statut = approuve), trace dans
     * validationFormations, notification au formateur.
     */
    @Transactional
    public Formation approveCourse(Formation formation, Utilisateur admin) {
        StatutFormation ancienStatut = formation.getStatut();
        formation.setStatut(StatutFormation.approuve);
        formation.setMotifRejet(null);
        formation = formationRepository.save(formation);

        enregistrerDecision(formation, admin, StatutFormation.approuve, null);
        notifyStatusChange(formation, ancienStatut, TypeNotification.formationApprouvee,
                "Votre formation a été approuvée",
                "Votre formation \"" + formation.getTitre() + "\" a été approuvée et est maintenant visible sur E-OFANA.");

        return formation;
    }

    /**
     * T-B-214 : rejette une formation — statut REJETE, motif obligatoire
     * (répercuté à la fois sur Formation.motifRejet pour l'état courant
     * et sur validationFormations.commentaire pour l'audit trail),
     * notification au formateur.
     */
    @Transactional
    public Formation rejectCourse(Formation formation, Utilisateur admin, String motif) {
        if (isBlank(motif)) {
            throw new IllegalArgumentException("Le motif de rejet est obligatoire");
        }

        StatutFormation ancienStatut = formation.getStatut();
        formation.setStatut(StatutFormation.rejete);
        formation.setMotifRejet(motif);
        formation = formationRepository.save(formation);

        enregistrerDecision(formation, admin, StatutFormation.rejete, motif);
        notifyStatusChange(formation, ancienStatut, TypeNotification.formationRejetee,
                "Votre formation a été rejetée",
                "Votre formation \"" + formation.getTitre() + "\" a été rejetée. Motif : " + motif);

        return formation;
    }

    /**
     * T-B-214 : demande une correction — statut CORRECTION_DEMANDEE,
     * commentaire obligatoire, notification au formateur.
     */
    @Transactional
    public Formation requestCorrection(Formation formation, Utilisateur admin, String commentaire) {
        if (isBlank(commentaire)) {
            throw new IllegalArgumentException("Le commentaire de correction est obligatoire");
        }

        StatutFormation ancienStatut = formation.getStatut();
        formation.setStatut(StatutFormation.correctionDemandee);
        formation.setMotifRejet(commentaire);
        formation = formationRepository.save(formation);

        enregistrerDecision(formation, admin, StatutFormation.correctionDemandee, commentaire);
        notifyStatusChange(formation, ancienStatut, TypeNotification.formationCorrectionDemandee,
                "Une correction est demandée sur votre formation",
                "Une correction est demandée sur votre formation \"" + formation.getTitre() + "\". Commentaire : " + commentaire);

        return formation;
    }

    /**
     * Enregistre la décision dans validationFormations (qui / quand /
     * quoi), critère d'acceptation commun à T-B-205 et T-B-214.
     */
    private void enregistrerDecision(Formation formation, Utilisateur admin, StatutFormation decision, String commentaire) {
        ValidationFormation validation = ValidationFormation.builder()
                .formation(formation)
                .moderateur(admin)
                .decision(decision)
                .commentaire(commentaire)
                .build();
        validationFormationRepository.save(validation);
    }

    /**
     * Déclenche la notification au formateur propriétaire de la
     * formation. Implémentée depuis que le module Admin (V9,
     * "notifications") existe — cf. NotificationService pour la
     * politique best-effort d'envoi.
     */
    private void notifyStatusChange(Formation formation, StatutFormation ancienStatut,
                                     TypeNotification type, String titre, String message) {
        Utilisateur formateur = formation.getCentre() != null ? formation.getCentre().getUtilisateur() : null;
        if (formateur == null) {
            return;
        }
        notificationService.creerEtEnvoyer(formateur, type, titre, message, Map.of(
                "destinataire", String.valueOf(formateur.getEmail()),
                "typeNotif", type.name(),
                "formationTitre", String.valueOf(formation.getTitre()),
                "ancienStatut", String.valueOf(ancienStatut),
                "nouveauStatut", String.valueOf(formation.getStatut())
        ));
    }

    /**
     * Conservée pour compatibilité de signature avec T-B-114
     * (module Formateur), qui ne passait que la formation. Les
     * nouvelles méthodes de T-B-214 appellent la variante privée
     * ci-dessus avec le contexte complet (ancien statut, type,
     * message) — préférer approveCourse/rejectCourse/requestCorrection.
     */
    public void notifyStatusChange(Formation formation) {
        TypeNotification type = switch (formation.getStatut()) {
            case approuve -> TypeNotification.formationApprouvee;
            case rejete -> TypeNotification.formationRejetee;
            case correctionDemandee -> TypeNotification.formationCorrectionDemandee;
            default -> TypeNotification.systeme;
        };
        notifyStatusChange(formation, null, type,
                "Mise à jour de votre formation",
                "Le statut de votre formation \"" + formation.getTitre() + "\" a changé : " + formation.getStatut());
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
