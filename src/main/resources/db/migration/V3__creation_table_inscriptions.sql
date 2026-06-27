-- ============================================================
--  E-OFANA — Migration Flyway V3
--  Création de la table : inscriptions
--  Branche : back → part-Rolph
--  Auteur  : ROLPH  |  Tâche : T-B-001
--  Date    : Juin 2026
--
--  Référence BDD : eofanaDb v3.0 — table "inscriptions"
--  Dépend de : V1 (utilisateurs), V2 (sessionsFormation)
-- ============================================================

CREATE TYPE "typeInscription" AS ENUM (
    'inscription',
    'reservation'
);

CREATE TYPE "statutInscription" AS ENUM (
    'enAttente',
    'valide',
    'annule',
    'rembourse',
    'termine'
);

CREATE TABLE IF NOT EXISTS "inscriptions" (
    "idInscription"     BIGSERIAL               PRIMARY KEY,
    "idUser"            BIGINT                  NOT NULL REFERENCES "utilisateurs" ("idUser"),
    "idSession"         BIGINT                  NOT NULL REFERENCES "sessionsFormation" ("idSession"),
    "typeInsc"          "typeInscription"       NOT NULL DEFAULT 'inscription',
    "statut"            "statutInscription"     NOT NULL DEFAULT 'enAttente',
    "montantPaye"       BIGINT                  NOT NULL DEFAULT 0 CHECK ("montantPaye" >= 0),
    "remise"            BIGINT                  NOT NULL DEFAULT 0 CHECK ("remise" >= 0),
    "commission"        BIGINT                  NOT NULL DEFAULT 0 CHECK ("commission" >= 0),
    "montantFormateur"  BIGINT                  NOT NULL DEFAULT 0 CHECK ("montantFormateur" >= 0),
    "operateur"         "operateurMm",
    "transactionId"     VARCHAR(100),
    "numeroRecu"        VARCHAR(50)             UNIQUE,
    "recuPdf"           VARCHAR(255),
    "createdAt"         TIMESTAMPTZ             NOT NULL DEFAULT NOW(),
    "updatedAt"         TIMESTAMPTZ             NOT NULL DEFAULT NOW(),

    CONSTRAINT "uqUserSession" UNIQUE ("idUser", "idSession")
);

-- Index alignés sur la BDD v3.0
CREATE INDEX IF NOT EXISTS "idxInscriptionsUser"      ON "inscriptions" ("idUser");
CREATE INDEX IF NOT EXISTS "idxInscriptionsSession"   ON "inscriptions" ("idSession");
CREATE INDEX IF NOT EXISTS "idxInscriptionsStatut"    ON "inscriptions" ("statut");
CREATE INDEX IF NOT EXISTS "idxInscriptionsOperateur" ON "inscriptions" ("operateur");
CREATE INDEX IF NOT EXISTS "idxInscriptionsCreated"   ON "inscriptions" ("createdAt" DESC);

-- Trigger updatedAt
CREATE TRIGGER "trgInscriptionsUpdatedAt"
    BEFORE UPDATE ON "inscriptions"
    FOR EACH ROW EXECUTE FUNCTION "fnSetUpdatedAt"();

-- Trigger : génération automatique du numéro de reçu (TXN-YYYYMMDD-NNNNN)
CREATE OR REPLACE FUNCTION "fnGenererNumeroRecu"()
RETURNS TRIGGER LANGUAGE plpgsql AS $$
DECLARE
    "vSeq" INT;
BEGIN
    SELECT COUNT(*) + 1
      INTO "vSeq"
      FROM "inscriptions"
     WHERE DATE("createdAt") = CURRENT_DATE;

    NEW."numeroRecu" := 'TXN-' || TO_CHAR(NOW(), 'YYYYMMDD')
                        || '-' || LPAD("vSeq"::TEXT, 5, '0');
    RETURN NEW;
END;
$$;

CREATE TRIGGER "trgNumeroRecuInscription"
    BEFORE INSERT ON "inscriptions"
    FOR EACH ROW
    WHEN (NEW."numeroRecu" IS NULL)
    EXECUTE FUNCTION "fnGenererNumeroRecu"();

-- Trigger : calcul automatique remise (5%), commission (7%/15%), montantFormateur
CREATE OR REPLACE FUNCTION "fnCalculerCommission"()
RETURNS TRIGGER LANGUAGE plpgsql AS $$
DECLARE
    "vTaux"   NUMERIC(5,2);
    "vPrix"   BIGINT;
    "vRemise" BIGINT;
BEGIN
    SELECT c."tauxCommission", COALESCE(f."prixRemise", f."prix")
      INTO "vTaux", "vPrix"
      FROM "sessionsFormation" s
      JOIN "formations" f ON f."idFormation" = s."idFormation"
      JOIN "centres"    c ON c."idCentre"    = f."idCentre"
     WHERE s."idSession" = NEW."idSession";

    "vRemise"              := ROUND("vPrix" * 0.05);
    NEW."remise"           := "vRemise";
    NEW."montantPaye"      := "vPrix" - "vRemise";
    NEW."commission"       := ROUND(NEW."montantPaye" * "vTaux" / 100);
    NEW."montantFormateur" := NEW."montantPaye" - NEW."commission";

    RETURN NEW;
END;
$$;

CREATE TRIGGER "trgCalculerCommissionInscription"
    BEFORE INSERT ON "inscriptions"
    FOR EACH ROW EXECUTE FUNCTION "fnCalculerCommission"();

-- Trigger : gestion des places restantes
CREATE OR REPLACE FUNCTION "fnGererPlaces"()
RETURNS TRIGGER LANGUAGE plpgsql AS $$
BEGIN
    IF (TG_OP = 'INSERT' AND NEW."statut" = 'valide')
    OR (TG_OP = 'UPDATE' AND NEW."statut" = 'valide' AND OLD."statut" <> 'valide') THEN
        UPDATE "sessionsFormation"
           SET "placesRestantes" = GREATEST(0, "placesRestantes" - 1)
         WHERE "idSession" = NEW."idSession";

        UPDATE "sessionsFormation"
           SET "statut" = 'complet'
         WHERE "idSession" = NEW."idSession"
           AND "placesRestantes" = 0
           AND "statut" = 'ouvert';
    END IF;

    IF TG_OP = 'UPDATE' AND NEW."statut" = 'annule' AND OLD."statut" = 'valide' THEN
        UPDATE "sessionsFormation"
           SET "placesRestantes" = LEAST("placesTotal", "placesRestantes" + 1),
               "statut" = CASE WHEN "statut" = 'complet' THEN 'ouvert' ELSE "statut" END
         WHERE "idSession" = NEW."idSession";
    END IF;

    RETURN NEW;
END;
$$;

CREATE TRIGGER "trgPlacesInscriptionInsert"
    AFTER INSERT ON "inscriptions"
    FOR EACH ROW EXECUTE FUNCTION "fnGererPlaces"();

CREATE TRIGGER "trgPlacesInscriptionUpdate"
    AFTER UPDATE ON "inscriptions"
    FOR EACH ROW EXECUTE FUNCTION "fnGererPlaces"();
