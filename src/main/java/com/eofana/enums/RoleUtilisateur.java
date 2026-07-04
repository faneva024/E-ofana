package com.eofana.enums;

/**
 * Rôles possibles d'un utilisateur sur la plateforme E-OFANA.
 *
 * Miroir exact du type ENUM PostgreSQL "roleUtilisateur" créé dans
 * V2__create_types.sql. Les valeurs doivent rester strictement
 * identiques (même orthographe, même casse) pour que Hibernate
 * puisse faire correspondre les deux côtés.
 *
 * @see com.eofana.entity.Utilisateur#role
 */
public enum RoleUtilisateur {
    apprenant,
    formateur,
    moderateur,
    commercial,
    admin
}