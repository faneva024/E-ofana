package com.eofana.enums;

/**
 * Statuts possibles d'un virement vers un centre de formation.
 * Miroir exact du type ENUM PostgreSQL "statutVirement" créé en V8.
 *
 * @see com.eofana.entity.Virement#statut
 */
public enum StatutVirement {
    planifie,
    effectue,
    echoue
}
