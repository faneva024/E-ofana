package com.eofana.repository.projection;

import java.time.LocalDate;

/**
 * Projection Spring Data pour la requête native {@code compterInscriptionsParSession}.
 *
 * Chaque instance représente les statistiques d'inscriptions d'UNE session
 * d'une formation donnée (vue formateur : combien d'inscrits, combien de
 * réservations, revenus estimés).
 *
 * <h3>Convention de nommage</h3>
 * Les alias SQL de la requête native doivent correspondre EXACTEMENT
 * aux noms de propriétés de cette interface (camelCase, sans "get").
 * Spring Data crée un proxy dynamique qui mappe chaque colonne du
 * ResultSet sur le getter correspondant.
 *
 * @see com.eofana.repository.FormationRepository#compterInscriptionsParSession(Long)
 */
public interface StatInscriptionParSession {

    /** Identifiant de la session. */
    Long getIdSession();

    /** Date de début de la session. */
    LocalDate getDateDebut();

    /** Date de fin de la session (peut être null). */
    LocalDate getDateFin();

    /** Capacité totale de la session. */
    Integer getPlacesTotal();

    /** Places encore disponibles à cet instant. */
    Integer getPlacesRestantes();

    /**
     * Nombre d'apprenants ayant effectué une INSCRIPTION complète
     * (typeInsc = 'inscription') avec statut non annulé/remboursé.
     */
    Long getNbInscrits();

    /**
     * Nombre d'apprenants ayant effectué une RÉSERVATION de place
     * (typeInsc = 'reservation') avec statut non annulé/remboursé.
     */
    Long getNbReservations();

    /**
     * Total des participations actives (inscrits + réservations),
     * statuts non annulés et non remboursés.
     */
    Long getNbTotal();

    /**
     * Somme des {@code montantFormateur} sur les inscriptions validées
     * (statut = 'valide') pour cette session.
     * Représente le revenu net dû au formateur pour cette session.
     * En Ariary.
     */
    Long getMontantFormateurValide();
}
