package com.eofana.entite;
 
/**
 
 *
 * EN_ATTENTE : inscription creee mais aucun paiement reçu
 * PARTIEL    : réservation effectuee avec un acompte (paiement partiel)
 * COMPLETE   : paiement integral reçu
 */
public enum StatutPaiement {
    EN_ATTENTE,
    PARTIEL,
    COMPLETE
}
 