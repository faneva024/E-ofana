package com.eofana.enums;

/**
 * Statuts possibles d'une inscription (ou réservation).
 * Miroir exact du type ENUM PostgreSQL "statutInscription".
 *
 * enAttente  : créée, paiement pas encore confirmé (ou acompte reçu
 *              mais solde manquant pour une réservation)
 * valide     : entièrement payée, place définitivement acquise
 * annule     : annulée par l'apprenant ou le centre
 * rembourse  : remboursée
 * termine    : la session s'est déroulée
 *
 * @see com.eofana.entity.Inscription#statut
 */
public enum StatutInscription {
    enAttente,
    valide,
    annule,
    rembourse,
    termine
}
