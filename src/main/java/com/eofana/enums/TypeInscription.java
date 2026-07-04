package com.eofana.enums;

/**
 * Type d'une inscription : inscription complète (paiement intégral,
 * moins la remise E-OFANA) ou réservation (acompte = commission
 * uniquement, solde réglé lors de l'inscription définitive).
 * Miroir exact du type ENUM PostgreSQL "typeInscription".
 *
 * @see com.eofana.entity.Inscription#typeInsc
 */
public enum TypeInscription {
    inscription,
    reservation
}
