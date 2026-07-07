-- Description: Tables du module Formateur (Semaine 2) :
--   demandes de modification de profil centre, historique des
--   abonnements, virements, audit de modération des formations.
--
-- Note de conception : "centres"."abonnement" et "tauxCommission"
-- existent déjà (V4). La table "abonnements" ci-dessous ne stocke
-- QUE l'historique des souscriptions (utile pour la facturation /
-- l'audit) ; la valeur "vivante" utilisée par l'application reste
-- centres.abonnement, synchronisée automatiquement vers
-- centres.tauxCommission par le trigger fnSyncTauxCommission.

CREATE TYPE "statutModification" AS ENUM ('enAttente', 'approuve', 'rejete');
CREATE TYPE "statutVirement" AS ENUM ('planifie', 'effectue', 'echoue');

-- ---------------------------------------------------------------
-- demandesModificationCentre
-- ---------------------------------------------------------------
CREATE TABLE "demandesModificationCentre" (
    "idDemande"         BIGSERIAL               PRIMARY KEY,
    "idCentre"          BIGINT                  NOT NULL REFERENCES "centres" ("idCentre") ON DELETE CASCADE,
    "donneesProposees"  JSONB                   NOT NULL,
    "commentaireAdmin"  TEXT,
    "statut"            "statutModification"    NOT NULL DEFAULT 'enAttente',
    "traiteParId"       BIGINT                  REFERENCES "utilisateurs" ("idUser"),
    "traiteAt"          TIMESTAMPTZ,
    "createdAt"         TIMESTAMPTZ             NOT NULL DEFAULT NOW()
);

CREATE INDEX "idxDemandesModifCentre"  ON "demandesModificationCentre" ("idCentre");
CREATE INDEX "idxDemandesModifStatut"  ON "demandesModificationCentre" ("statut");

-- ---------------------------------------------------------------
-- abonnements (historique des souscriptions par centre)
-- ---------------------------------------------------------------
CREATE TABLE "abonnements" (
    "idAbonnement"  BIGSERIAL           PRIMARY KEY,
    "idCentre"      BIGINT              NOT NULL REFERENCES "centres" ("idCentre") ON DELETE CASCADE,
    "type"          "abonnementType"    NOT NULL,
    "tauxCommission" NUMERIC(5,2)       NOT NULL,
    "dateDebut"     DATE                NOT NULL DEFAULT CURRENT_DATE,
    "dateFin"       DATE,
    "createdAt"     TIMESTAMPTZ         NOT NULL DEFAULT NOW(),
    CONSTRAINT "chkAbonnementDates" CHECK ("dateFin" IS NULL OR "dateFin" >= "dateDebut")
);

CREATE INDEX "idxAbonnementsCentre" ON "abonnements" ("idCentre");

-- ---------------------------------------------------------------
-- virements
-- ---------------------------------------------------------------
CREATE TABLE "virements" (
    "idVirement"        BIGSERIAL           PRIMARY KEY,
    "idCentre"          BIGINT              NOT NULL REFERENCES "centres" ("idCentre") ON DELETE CASCADE,
    "montantBrut"       BIGINT              NOT NULL CHECK ("montantBrut" >= 0),
    "commission"        BIGINT              NOT NULL CHECK ("commission" >= 0),
    "montantNet"        BIGINT              NOT NULL CHECK ("montantNet" >= 0),
    "operateur"         "operateurMm"       NOT NULL,
    "referenceVirement" VARCHAR(100)        UNIQUE,
    "statut"            "statutVirement"    NOT NULL DEFAULT 'planifie',
    "periodeDebut"      DATE                NOT NULL,
    "periodeFin"        DATE                NOT NULL,
    "dateVirement"      TIMESTAMPTZ,
    "createdAt"         TIMESTAMPTZ         NOT NULL DEFAULT NOW(),
    CONSTRAINT "chkVirementPeriode" CHECK ("periodeFin" >= "periodeDebut"),
    CONSTRAINT "chkVirementMontantNet" CHECK ("montantNet" = "montantBrut" - "commission")
);

CREATE INDEX "idxVirementsCentre"  ON "virements" ("idCentre");
CREATE INDEX "idxVirementsStatut"  ON "virements" ("statut");
CREATE INDEX "idxVirementsCreated" ON "virements" ("createdAt" DESC);

-- ---------------------------------------------------------------
-- validationFormations (audit trail des décisions de modération)
-- ---------------------------------------------------------------
CREATE TABLE "validationFormations" (
    "idValidation"  BIGSERIAL           PRIMARY KEY,
    "idFormation"   BIGINT              NOT NULL REFERENCES "formations" ("idFormation") ON DELETE CASCADE,
    "idModerateur"  BIGINT              REFERENCES "utilisateurs" ("idUser"),
    "decision"      "statutFormation"   NOT NULL,
    "commentaire"   TEXT,
    "createdAt"     TIMESTAMPTZ         NOT NULL DEFAULT NOW()
);

CREATE INDEX "idxValidationFormation" ON "validationFormations" ("idFormation");

-- ---------------------------------------------------------------
-- Trigger fnSyncTauxCommission
--
-- Recalcule automatiquement centres.tauxCommission chaque fois que
-- centres.abonnement change (basic -> 7.00, premium -> 15.00),
-- conformément à la contrainte "chkTauxCommission" de V4. Se
-- déclenche aussi à l'insertion pour couvrir la création d'un centre.
-- ---------------------------------------------------------------
CREATE OR REPLACE FUNCTION "fnSyncTauxCommission"()
RETURNS TRIGGER AS $$
BEGIN
    IF NEW."abonnement" = 'premium' THEN
        NEW."tauxCommission" := 15.00;
    ELSE
        NEW."tauxCommission" := 7.00;
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER "trgSyncTauxCommission"
    BEFORE INSERT OR UPDATE OF "abonnement" ON "centres"
    FOR EACH ROW
    EXECUTE FUNCTION "fnSyncTauxCommission"();
