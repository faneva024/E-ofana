-- Description: Création des tables de catalogue (catégories, centres et formations)

CREATE TABLE "categories" (
    "idCategorie" BIGSERIAL       PRIMARY KEY,
    "nom"         VARCHAR(100)    NOT NULL UNIQUE,
    "description" VARCHAR(300),
    "icone"       VARCHAR(50)     NOT NULL DEFAULT 'bi-book',
    "couleur"     VARCHAR(20)     NOT NULL DEFAULT '#2E75B6'
);

CREATE TABLE "centres" (
    "idCentre"              BIGSERIAL           PRIMARY KEY,
    "idUser"                BIGINT              NOT NULL UNIQUE REFERENCES "utilisateurs" ("idUser") ON DELETE CASCADE,
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
    CONSTRAINT "chkFourchette"      CHECK ("fourchettePrixMax" >= "fourchettePrixMin"),
    CONSTRAINT "chkTauxCommission"  CHECK ("tauxCommission" IN (7.00, 15.00))
);

CREATE TABLE "formations" (
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