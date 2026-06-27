
--Tâche : T-B-001

-- Type ENUM des rôles (créé en premier car utilisé par la table)
CREATE TYPE "roleUtilisateur" AS ENUM (
    'apprenant',
    'formateur',
    'moderateur',
    'commercial',
    'admin'
);

CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TABLE IF NOT EXISTS "utilisateurs" (
    "idUser"      BIGSERIAL                   PRIMARY KEY,
    "uuid"        UUID                        NOT NULL DEFAULT uuid_generate_v4() UNIQUE,
    "nom"         VARCHAR(100)                NOT NULL,
    "prenom"      VARCHAR(100)                NOT NULL,
    "email"       VARCHAR(180)                NOT NULL UNIQUE,
    "motDePasse"  VARCHAR(255)                NOT NULL,
    "role"        "roleUtilisateur"           NOT NULL DEFAULT 'apprenant',
    "telephone"   VARCHAR(20),
    "avatar"      VARCHAR(255),
    "estActif"    BOOLEAN                     NOT NULL DEFAULT TRUE,
    "createdAt"   TIMESTAMPTZ                 NOT NULL DEFAULT NOW(),
    "updatedAt"   TIMESTAMPTZ                 NOT NULL DEFAULT NOW(),

    CONSTRAINT "chkEmailFormat" CHECK ("email" ~* '^[^@]+@[^@]+\.[^@]+$')
);

-- Index alignés sur la BDD v3.0
CREATE INDEX IF NOT EXISTS "idxUtilisateursEmail" ON "utilisateurs" ("email");
CREATE INDEX IF NOT EXISTS "idxUtilisateursRole"  ON "utilisateurs" ("role");
CREATE INDEX IF NOT EXISTS "idxUtilisateursActif" ON "utilisateurs" ("estActif");

-- Trigger updatedAt automatique
CREATE OR REPLACE FUNCTION "fnSetUpdatedAt"()
RETURNS TRIGGER LANGUAGE plpgsql AS $$
BEGIN
    NEW."updatedAt" = NOW();
    RETURN NEW;
END;
$$;

CREATE TRIGGER "trgUtilisateursUpdatedAt"
    BEFORE UPDATE ON "utilisateurs"
    FOR EACH ROW EXECUTE FUNCTION "fnSetUpdatedAt"();
