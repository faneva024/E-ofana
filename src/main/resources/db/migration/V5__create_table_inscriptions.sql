-- Description: Création des sessions et de la table des inscriptions apprenants

CREATE TABLE "sessionsFormation" (
    "idSession"               BIGSERIAL       PRIMARY KEY,
    "idFormation"             BIGINT          NOT NULL REFERENCES "formations" ("idFormation") ON DELETE CASCADE,
    "dateDebut"               DATE            NOT NULL,
    "dateFin"                 DATE,
    "dateLimiteInscription"   DATE            NOT NULL,
    "placesTotal"             INTEGER         NOT NULL DEFAULT 20 CHECK ("placesTotal" > 0),
    "placesRestantes"         INTEGER         NOT NULL DEFAULT 20 CHECK ("placesRestantes" >= 0),
    "statut"                  "statutSession" NOT NULL DEFAULT 'ouvert',
    "createdAt"               TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    CONSTRAINT "chkDatesSession"    CHECK ("dateFin" IS NULL OR "dateFin" >= "dateDebut"),
    CONSTRAINT "chkLimiteDebut"     CHECK ("dateLimiteInscription" <= "dateDebut"),
    CONSTRAINT "chkPlacesSession"   CHECK ("placesRestantes" <= "placesTotal")
);

CREATE TABLE "inscriptions" (
    "idInscription"      BIGSERIAL            PRIMARY KEY,
    "idUser"             BIGINT               NOT NULL REFERENCES "utilisateurs" ("idUser"),
    "idSession"          BIGINT               NOT NULL REFERENCES "sessionsFormation" ("idSession"),
    "typeInsc"           "typeInscription"    NOT NULL DEFAULT 'inscription',
    "statut"             "statutInscription"  NOT NULL DEFAULT 'enAttente',
    "montantPaye"        BIGINT               NOT NULL DEFAULT 0 CHECK ("montantPaye" >= 0),
    "remise"             BIGINT               NOT NULL DEFAULT 0 CHECK ("remise" >= 0),
    "commission"         BIGINT               NOT NULL DEFAULT 0 CHECK ("commission" >= 0),
    "montantFormateur"   BIGINT               NOT NULL DEFAULT 0 CHECK ("montantFormateur" >= 0),
    "operateur"          "operateurMm",
    "transactionId"      VARCHAR(100),
    "numeroRecu"         VARCHAR(50)          UNIQUE,
    "recuPdf"            VARCHAR(255),
    "createdAt"          TIMESTAMPTZ          NOT NULL DEFAULT NOW(),
    "updatedAt"          TIMESTAMPTZ          NOT NULL DEFAULT NOW(),
    CONSTRAINT "uqUserSession"   UNIQUE ("idUser", "idSession")
);