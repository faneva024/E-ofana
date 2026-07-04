-- Description: Création du type statutVirement et de la table de suivi des virements aux centres
CREATE TYPE "statutVirement" AS ENUM ('planifie', 'effectue', 'echoue');

CREATE TABLE "virements" (
    "idVirement"         BIGSERIAL           PRIMARY KEY,
    "idCentre"           BIGINT              NOT NULL REFERENCES "centres" ("idCentre"),
    "montantBrut"        BIGINT              NOT NULL CHECK ("montantBrut" > 0),
    "commission"         BIGINT              NOT NULL CHECK ("commission" >= 0),
    "montantNet"         BIGINT              NOT NULL CHECK ("montantNet" >= 0),
    "operateur"          "operateurMm"       NOT NULL,
    "referenceVirement"  VARCHAR(100)        UNIQUE,
    "statut"             "statutVirement"    NOT NULL DEFAULT 'planifie',
    "periodeDebut"       DATE                NOT NULL,
    "periodeFin"         DATE                NOT NULL,
    "dateVirement"       DATE,
    "createdAt"          TIMESTAMPTZ         NOT NULL DEFAULT NOW(),
    CONSTRAINT "chkPeriodeVirement"     CHECK ("periodeFin" >= "periodeDebut"),
    CONSTRAINT "chkMontantNetVirement"  CHECK ("montantNet" = "montantBrut" - "commission")
);

CREATE INDEX "idxVirementsCentre" ON "virements" ("idCentre");
CREATE INDEX "idxVirementsStatut" ON "virements" ("statut");