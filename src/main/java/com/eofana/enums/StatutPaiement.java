package com.eofana.enums;

/**
 * Statuts possibles d'une transaction de paiement Mobile Money.
 * Miroir exact du type ENUM PostgreSQL "statutPaiement".
 *
 * @see com.eofana.entity.Paiement#statut
 */
public enum StatutPaiement {
    enAttente,
    confirme,
    echoue
}
