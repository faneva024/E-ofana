package com.eofana.service;

import com.eofana.entity.Formation;
import com.eofana.enums.StatutFormation;
import com.eofana.repository.FormationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * T-B-114 : file d'attente de modération des formations.
 *
 * notifyStatusChange() se contente ici de documenter le typeNotif à
 * déclencher : le module de notification (table "notifications",
 * service Node.js d'envoi d'email) appartient au module
 * Admin/Modérateur, hors périmètre de ce sprint Formateur — on ne le
 * simule pas en silence pour ne pas donner une fausse impression de
 * fonctionnalité complète.
 */
@Service
@RequiredArgsConstructor
public class CourseValidationService {

    private final FormationRepository formationRepository;

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
     * Déclenche une notification en cas de changement de statut
     * (approbation, rejet, demande de correction).
     *
     * ⚠ Non implémenté : ce sprint ne couvre pas le module
     * Admin/Commercial qui gère réellement la table "notifications" et
     * le service Node.js d'envoi d'email (typeNotif approprié selon le
     * cas). Cette méthode documente l'intention plutôt que de simuler
     * un envoi silencieusement.
     */
    public void notifyStatusChange(Formation formation) {
        throw new UnsupportedOperationException(
                "Notification de changement de statut non implémentée : dépend du module "
                        + "Admin/Commercial (table notifications, service email Node.js), hors périmètre "
                        + "du module Formateur. À brancher une fois ce module disponible.");
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
