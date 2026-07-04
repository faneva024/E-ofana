package com.eofana.enums;

/**
 * Statuts possibles d'une formation, dans son workflow de modération.
 *
 * Miroir exact du type ENUM PostgreSQL "statutFormation" créé dans
 * V2__create_types.sql.
 *
 * Workflow : brouillon → enAttente → approuve | rejete | correctionDemandee
 *            (puis archive en fin de vie)
 *
 * @see com.eofana.entity.Formation#statut
 */
public enum StatutFormation {
    brouillon,
    enAttente,
    approuve,
    rejete,
    correctionDemandee,
    archive
}
