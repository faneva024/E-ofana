package com.eofana.enums;

/**
 * Statut d'envoi d'une notification (email ou SMS).
 * Miroir exact du type ENUM PostgreSQL "statutEnvoi".
 */
public enum StatutEnvoi {
    enAttente,
    envoye,
    erreur
}
