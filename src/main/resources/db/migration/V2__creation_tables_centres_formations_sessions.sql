-- ============================================================
--  E-OFANA — Migration Flyway V2
--  Création des tables : categories, centres, formations,
--                        sessionsFormation
--  Branche : back → part-Rolph
--  Auteur  : ROLPH  |  Tâche : T-B-001
--  Date    : Juin 2026
--
--  Référence BDD : eofanaDb v3.0
--  Ces tables sont nécessaires avant inscriptions et paiements.
-- ============================================================

-- ── ENUMs ───────────────────────────────────────────────────
CREATE TYPE "statutCentre" AS ENUM (
    'enAttente', 'actif', 'inactif', 'suspendu'
);

CREATE TYPE "abonnementType" AS ENUM (
    'basic', 'premium'
);

CREATE TYPE "operateurMm" AS ENUM (
    'mvola', 'orange', 'airtel'
);

CREATE TYPE "statutFormation" AS ENUM (
    'brouillon', 'enAttente', 'approuve',
    'rejete', 'correctionDemandee', 'archive'
);

CREATE TYPE "statutSession" AS ENUM (
    'ouvert', 'complet', 'termine', 'annule'
);

CREATE TYPE "frequenceReversement" AS ENUM (
    'hebdomadaire', 'mensuel'
);

-- ── CATEGORIES ──────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS "categories" (
    "idCategorie"  BIGSERIAL       PRIMARY KEY,
    "nom"          VARCHAR(100)    NOT NULL UNIQUE,
    "description"  VARCHAR(300),
    "icone"        VARCHAR(50)     NOT NULL DEFAULT 'bi-book',
    "couleur"      VARCHAR(20)     NOT NULL DEFAULT '#2E75B6'
);

-- ── CENTRES DE FORMATION ────────────────────────────────────
CREATE TABLE IF NOT EXISTS "centres" (
    "idCentre"              BIGSERIAL           PRIMARY KEY,
    "idUser"                BIGINT              NOT NULL UNIQUE
                                                REFERENCES "utilisateurs" ("idUser") ON DELETE CASCADE,
    "nom"                   VARCHAR(200)        NOT NULL,
    "logo"                  VARCHAR(255),
    "description"           TEXT,
    "services"              TEXT,
    "ville"                 VARCHAR(100),
    "adresse"               TEXT,
    "fourchettePrixMin"     INTEGER             NOT NULL DEFAULT 0 CHECK ("fourchettePrixMin" >= 0),
    "fourchettePrixMax"     INTEGER             NOT NULL DEFAULT 0 CHECK ("fourchettePrixMax" >= 0),
    "telephone"             VARCHAR(20),
    "email"                 VARCHAR(180),
    "siteWeb"               VARCHAR(255),
    "reseauxSociaux"        JSONB,
    "abonnement"            "abonnementType"    NOT NULL DEFAULT 'basic',
    "tauxCommission"        NUMERIC(5,2)        NOT NULL DEFAULT 7.00,
    "frequenceReversement"  "frequenceReversement" NOT NULL DEFAULT 'mensuel',
    "mobileMoneyOperateur"  "operateurMm",
    "mobileMoneyNumero"     VARCHAR(20),
    "statut"                "statutCentre"      NOT NULL DEFAULT 'enAttente',
    "idModerateur"          BIGINT              REFERENCES "utilisateurs" ("idUser"),
    "validatedAt"           TIMESTAMPTZ,
    "createdAt"             TIMESTAMPTZ         NOT NULL DEFAULT NOW(),
    "updatedAt"             TIMESTAMPTZ         NOT NULL DEFAULT NOW(),

    CONSTRAINT "chkFourchette"     CHECK ("fourchettePrixMax" >= "fourchettePrixMin"),
    CONSTRAINT "chkTauxCommission" CHECK ("tauxCommission" IN (7.00, 15.00))
);

CREATE INDEX IF NOT EXISTS "idxCentresVille"      ON "centres" ("ville");
CREATE INDEX IF NOT EXISTS "idxCentresAbonnement" ON "centres" ("abonnement");
CREATE INDEX IF NOT EXISTS "idxCentresStatut"     ON "centres" ("statut");

CREATE TRIGGER "trgCentresUpdatedAt"
    BEFORE UPDATE ON "centres"
    FOR EACH ROW EXECUTE FUNCTION "fnSetUpdatedAt"();

-- ── FORMATIONS ──────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS "formations" (
    "idFormation"   BIGSERIAL           PRIMARY KEY,
    "idCentre"      BIGINT              NOT NULL REFERENCES "centres" ("idCentre") ON DELETE CASCADE,
    "idCategorie"   BIGINT              REFERENCES "categories" ("idCategorie") ON DELETE SET NULL,
    "titre"         VARCHAR(250)        NOT NULL,
    "description"   TEXT,
    "image"         VARCHAR(255),
    "duree"         VARCHAR(100),
    "lieu"          VARCHAR(200),
    "prix"          BIGINT              NOT NULL CHECK ("prix" >= 0),
    "prixRemise"    BIGINT              CHECK ("prixRemise" >= 0 AND "prixRemise" < "prix"),
    "statut"        "statutFormation"   NOT NULL DEFAULT 'brouillon',
    "motifRejet"    TEXT,
    "idModerateur"  BIGINT              REFERENCES "utilisateurs" ("idUser"),
    "validatedAt"   TIMESTAMPTZ,
    "noteMoyenne"   NUMERIC(3,2)        NOT NULL DEFAULT 0 CHECK ("noteMoyenne" BETWEEN 0 AND 5),
    "nbAvis"        INTEGER             NOT NULL DEFAULT 0 CHECK ("nbAvis" >= 0),
    "createdAt"     TIMESTAMPTZ         NOT NULL DEFAULT NOW(),
    "updatedAt"     TIMESTAMPTZ         NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS "idxFormationsCentre"    ON "formations" ("idCentre");
CREATE INDEX IF NOT EXISTS "idxFormationsCategorie" ON "formations" ("idCategorie");
CREATE INDEX IF NOT EXISTS "idxFormationsStatut"    ON "formations" ("statut");
CREATE INDEX IF NOT EXISTS "idxFormationsPrix"      ON "formations" ("prix");
CREATE INDEX IF NOT EXISTS "idxFormationsNote"      ON "formations" ("noteMoyenne" DESC);
CREATE INDEX IF NOT EXISTS "idxFormationsFts"       ON "formations"
    USING GIN (to_tsvector('french', "titre" || ' ' || COALESCE("description", '')));

CREATE TRIGGER "trgFormationsUpdatedAt"
    BEFORE UPDATE ON "formations"
    FOR EACH ROW EXECUTE FUNCTION "fnSetUpdatedAt"();

-- ── SESSIONS DE FORMATION ───────────────────────────────────
CREATE TABLE IF NOT EXISTS "sessionsFormation" (
    "idSession"              BIGSERIAL       PRIMARY KEY,
    "idFormation"            BIGINT          NOT NULL REFERENCES "formations" ("idFormation") ON DELETE CASCADE,
    "dateDebut"              DATE            NOT NULL,
    "dateFin"                DATE,
    "dateLimiteInscription"  DATE            NOT NULL,
    "placesTotal"            INTEGER         NOT NULL DEFAULT 20 CHECK ("placesTotal" > 0),
    "placesRestantes"        INTEGER         NOT NULL DEFAULT 20 CHECK ("placesRestantes" >= 0),
    "statut"                 "statutSession" NOT NULL DEFAULT 'ouvert',
    "createdAt"              TIMESTAMPTZ     NOT NULL DEFAULT NOW(),

    CONSTRAINT "chkDatesSession"  CHECK ("dateFin" IS NULL OR "dateFin" >= "dateDebut"),
    CONSTRAINT "chkLimiteDebut"   CHECK ("dateLimiteInscription" <= "dateDebut"),
    CONSTRAINT "chkPlacesSession" CHECK ("placesRestantes" <= "placesTotal")
);

CREATE INDEX IF NOT EXISTS "idxSessionsFormation"  ON "sessionsFormation" ("idFormation");
CREATE INDEX IF NOT EXISTS "idxSessionsDateDebut"  ON "sessionsFormation" ("dateDebut");
CREATE INDEX IF NOT EXISTS "idxSessionsStatut"     ON "sessionsFormation" ("statut");
