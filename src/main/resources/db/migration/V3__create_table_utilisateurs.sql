-- Description: Création de la table utilisateurs principale
CREATE TABLE "utilisateurs" (
    "idUser"        BIGSERIAL           PRIMARY KEY,
    "uuid"          UUID                NOT NULL DEFAULT uuid_generate_v4() UNIQUE,
    "nom"           VARCHAR(100)        NOT NULL,
    "prenom"        VARCHAR(100)        NOT NULL,
    "email"         VARCHAR(180)        NOT NULL UNIQUE,
    "motDePasse"    VARCHAR(255)        NOT NULL,
    "role"          "roleUtilisateur"   NOT NULL DEFAULT 'apprenant',
    "telephone"     VARCHAR(20),
    "avatar"        VARCHAR(255),
    "estActif"      BOOLEAN             NOT NULL DEFAULT TRUE,
    "createdAt"     TIMESTAMPTZ         NOT NULL DEFAULT NOW(),
    "updatedAt"     TIMESTAMPTZ         NOT NULL DEFAULT NOW(),
    CONSTRAINT "chkEmailFormat" CHECK ("email" ~* '^[^@]+@[^@]+\.[^@]+$')
);