package com.eofana.enums;

/**
 * Statuts possibles d'une session de formation.
 * Miroir exact du type ENUM PostgreSQL "statutSession".
 *
 * @see com.eofana.entity.SessionFormation#statut
 */
public enum StatutSession {
    ouvert,
    complet,
    termine,
    annule
}
