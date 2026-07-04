package com.eofana.repository.projection;

/**
 * Projection Spring Data pour la requête native {@code compterInscriptionsParCentre}.
 *
 * Chaque instance représente les statistiques agrégées d'UNE formation
 * (toutes sessions confondues) pour un centre donné.
 * Utilisée pour remplir le tableau de bord formateur (liste de formations
 * avec leurs KPIs associés).
 *
 * <h3>Convention de nommage</h3>
 * Identique à {@link StatInscriptionParSession} : les alias SQL doivent
 * correspondre exactement aux noms de propriétés (camelCase sans "get").
 *
 * @see com.eofana.repository.FormationRepository#compterInscriptionsParCentre(Long)
 */
public interface StatFormationParCentre {

    /** Identifiant de la formation. */
    Long getIdFormation();

    /** Titre de la formation. */
    String getTitre();

    /**
     * Statut de la formation (valeur de l'enum PostgreSQL sous forme de String).
     * Ex : "approuve", "enAttente", "rejete"…
     * Utiliser {@code StatutFormation.valueOf(getStat())} pour obtenir l'enum Java.
     */
    String getStatut();

    /** Nombre de sessions créées pour cette formation (tous statuts). */
    Long getNbSessions();

    /** Nombre de sessions encore ouvertes à l'inscription. */
    Long getNbSessionsOuvertes();

    /**
     * Nombre total d'inscriptions actives (typeInsc = 'inscription',
     * statut ≠ annule et ≠ rembourse) sur toutes les sessions.
     */
    Long getNbInscrits();

    /**
     * Nombre total de réservations actives (typeInsc = 'reservation',
     * statut ≠ annule et ≠ rembourse) sur toutes les sessions.
     */
    Long getNbReservations();

    /**
     * Somme totale des {@code montantFormateur} sur les inscriptions validées
     * (statut = 'valide') pour toutes les sessions de cette formation.
     * Revenu net cumulé dû au formateur, en Ariary.
     */
    Long getMontantFormateurTotal();

    /**
     * Places restantes cumulées sur toutes les sessions ouvertes.
     * Indique si la formation peut encore accueillir des apprenants.
     */
    Integer getPlacesRestantesTotales();
}
