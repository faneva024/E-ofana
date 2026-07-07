package com.eofana.enums;

/**
 * Statuts possibles d'un virement vers un centre de formation.
 * Miroir exact du type ENUM PostgreSQL "statutVirement" créé dans
 * V8__module_formateur.sql.
 *
 * @see com.eofana.entity.Virement#statut
 */
public enum StatutVirement {
    planifie,
    effectue,
    echoue
}
