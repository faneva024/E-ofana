package com.eofana.enums;

/**
 * Types de notifications envoyées par le module Admin (T-B-201/V9).
 * Miroir exact du type ENUM PostgreSQL "typeNotification".
 */
public enum TypeNotification {
    creationCompte,
    formationApprouvee,
    formationRejetee,
    formationCorrectionDemandee,
    profilApprouve,
    profilRejete,
    systeme
}
