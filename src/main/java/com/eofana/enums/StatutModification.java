package com.eofana.enums;

/**
 * Statuts possibles d'une demande de modification de profil centre.
 * Miroir exact du type ENUM PostgreSQL "statutModification" créé
 * dans V8__module_formateur.sql.
 *
 * @see com.eofana.entity.DemandeModificationCentre#statut
 */
public enum StatutModification {
    enAttente,
    approuve,
    rejete
}
