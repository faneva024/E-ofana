package com.eofana.enums;

/**
 * Statuts possibles d'un centre de formation.
 * Miroir exact du type ENUM PostgreSQL "statutCentre".
 *
 * @see com.eofana.entity.Centre#statut
 */
public enum StatutCentre {
    enAttente,
    actif,
    inactif,
    suspendu
}
