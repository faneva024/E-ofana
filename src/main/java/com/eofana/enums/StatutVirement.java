package com.eofana.enums;

/**
 * Statuts possibles d'un virement destiné à un centre de formation.
 *  exact du type ENUM PostgreSQL "statutVirement".
 *
 * @see com.eofana.entity.Virement#statut
 */
public enum StatutVirement {
    planifie,
    effectue,
    echoue
}