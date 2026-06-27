-- ============================================================
--  E-OFANA — Migration Flyway V4
--  Création de la table : paiements
--  Branche : back → part-Rolph
--  Auteur  : ROLPH  |  Tâche : T-B-001
--  Date    : Juin 2026
--
--  Référence BDD : eofanaDb v3.0 — table "paiements"
--  Dépend de : V3 (inscriptions)
-- ============================================================

CREATE TYPE "statutPaiement" AS ENUM (
    'enAttente',
    'confirme',
    'echoue'
);

CREATE TABLE IF NOT EXISTS "paiements" (
    "idPaiement"          BIGSERIAL           PRIMARY KEY,
    "idInscription"       BIGINT              NOT NULL REFERENCES "inscriptions" ("idInscription"),
    "montant"             BIGINT              NOT NULL CHECK ("montant" > 0),
    "operateur"           "operateurMm"       NOT NULL,
    "numeroTransaction"   VARCHAR(100)        NOT NULL,
    "statut"              "statutPaiement"    NOT NULL DEFAULT 'enAttente',
    "createdAt"           TIMESTAMPTZ         NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS "idxPaiementsInscription" ON "paiements" ("idInscription");
CREATE INDEX IF NOT EXISTS "idxPaiementsStatut"      ON "paiements" ("statut");
