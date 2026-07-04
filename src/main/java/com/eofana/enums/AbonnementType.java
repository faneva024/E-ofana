package com.eofana.enums;

/**
 * Types d'abonnement souscrits par un centre.
 * Miroir exact du type ENUM PostgreSQL "abonnementType".
 *
 * basic   = 60 000 Ar/mois, 7% de commission
 * premium = 200 000 Ar/mois, 15% de commission
 *
 * @see com.eofana.entity.Centre#abonnement
 */
public enum AbonnementType {
    basic,
    premium
}
