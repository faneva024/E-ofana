package com.eofana.repository;
 
import com.eofana.entite.Apprenant;
import com.eofana.entite.Formation;
import com.eofana.entite.Inscription;
import com.eofana.entite.StatutPaiement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
 
import java.time.LocalDateTime;
import java.util.List;
 
/**
 * Dépôt (repository) pour l'entité Inscription.
 *
 * Fournit :
 *  - des méthodes de recherche (dérivées du nom de méthode, gérées automatiquement par Spring Data JPA)
 *  - des méthodes "métier" par défaut (creerInscription, creerReservation) qui construisent
 *    une Inscription correctement initialisée avant de la sauvegarder.
 */
public interface InscriptionRepository extends JpaRepository<Inscription, Long> {
 
    /**
     * Récupère toutes les inscriptions d'un apprenant donné.
     */
    List<Inscription> findByApprenantId(Long apprenantId);
 
    /**
     * Récupère toutes les inscriptions (tous les inscrits) d'une formation donnée.
     */
    List<Inscription> findByFormationId(Long formationId);
 
    /**
     * Vérifie si un apprenant est déjà inscrit (ou a réservé) à une formation donnée.
     * Utile pour éviter les doublons d'inscription.
     */
    boolean existsByApprenantIdAndFormationId(Long apprenantId, Long formationId);
 
    // ------------------------------------------------------------------
    // Méthodes métier demandées par la tâche T-B-004
    // ------------------------------------------------------------------
 
    /**
     * Enregistre une inscription complète : l'apprenant a réglé l'intégralité du prix.
     *
     * @param apprenant          l'apprenant qui s'inscrit
     * @param formation          la formation concernée
     * @param numeroRecu         numéro de reçu généré lors du paiement
     * @param operateurPaiement  opérateur Mobile Money utilisé (Mvola, Orange Money, Airtel Money)
     * @return l'inscription enregistrée en base
     */
    default Inscription creerInscription(Apprenant apprenant,
                                          Formation formation,
                                          String numeroRecu,
                                          String operateurPaiement) {
        Inscription inscription = Inscription.builder()
                .apprenant(apprenant)
                .formation(formation)
                .statutPaiement(StatutPaiement.COMPLETE)
                .reservationUniquement(false)
                .dateInscription(LocalDateTime.now())
                .numeroRecu(numeroRecu)
                .operateurPaiement(operateurPaiement)
                .build();
        return save(inscription);
    }
 
    /**
     * Enregistre une réservation : l'apprenant souhaite simplement réserver sa place,
     * généralement via un paiement partiel (acompte), sans solder le prix total.
     *
     * @param apprenant          l'apprenant qui réserve
     * @param formation          la formation concernée
     * @param numeroRecu         numéro de reçu de l'acompte (peut être null si aucun paiement encore)
     * @param operateurPaiement  opérateur Mobile Money utilisé, si un acompte a été versé
     * @return l'inscription (réservation) enregistrée en base
     */
    default Inscription creerReservation(Apprenant apprenant,
                                          Formation formation,
                                          String numeroRecu,
                                          String operateurPaiement) {
        Inscription reservation = Inscription.builder()
                .apprenant(apprenant)
                .formation(formation)
                .statutPaiement(StatutPaiement.PARTIEL)
                .reservationUniquement(true)
                .dateInscription(LocalDateTime.now())
                .numeroRecu(numeroRecu)
                .operateurPaiement(operateurPaiement)
                .build();
        return save(reservation);
    }
 
    /**
     * Alias explicite demandé par la tâche : récupère toutes les inscriptions d'un apprenant.
     */
    default List<Inscription> trouverInscriptionsApprenant(Long apprenantId) {
        return findByApprenantId(apprenantId);
    }
 
    /**
     * Alias explicite demandé par la tâche : récupère tous les inscrits d'une formation.
     */
    default List<Inscription> trouverInscriptionsFormation(Long formationId) {
        return findByFormationId(formationId);
    }
}
 