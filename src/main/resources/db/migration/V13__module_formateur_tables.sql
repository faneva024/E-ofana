-- ============================================================
--  E-OFANA — Migration Flyway V13
--  Module Formateur — tables supplémentaires
--  Branche : Back → part-Rolph
--  Auteur  : ROLPH  |  Tâche : T-B-101
--  Date    : 29 Juin 2026
--
--  Aligné sur eofanaDb v3.0 (V1→V12 existants) :
--    - camelCase avec guillemets doubles
--    - OffsetDateTime → TIMESTAMPTZ
--    - La table "virements" existe déjà en V9 → NON recréée ici
--    - La colonne "statutCompte" n'existe pas dans V1 → ajoutée ici
-- ============================================================

SET search_path TO eofana;

-- ── COLONNE statutCompte dans utilisateurs ───────────────────
-- "role" est déjà présent (V1 — type ENUM "roleUtilisateur").
-- On ajoute uniquement le statut du compte formateur.
ALTER TABLE "utilisateurs"
    ADD COLUMN IF NOT EXISTS "statutCompte" VARCHAR(20) NOT NULL DEFAULT 'actif'
        CHECK ("statutCompte" IN ('actif', 'suspendu'));

-- ── PROFIL CENTRE DE FORMATION ────────────────────────────────
-- Note : "virements" et ses colonnes "idCentre", "montantBrut",
--        "montantNet", etc. existent déjà en V9. Ne pas recréer.
CREATE TABLE IF NOT EXISTS "profilCentre" (
    "idCentre"              BIGSERIAL       PRIMARY KEY,
    "idFormateur"           BIGINT          NOT NULL UNIQUE
                                            REFERENCES "utilisateurs" ("idUser")
                                            ON DELETE CASCADE,
    "nomCentre"             VARCHAR(200)    NOT NULL,
    "logo"                  VARCHAR(500),
    "description"           TEXT,
    "servicesProposes"      TEXT,
    "ville"                 VARCHAR(100),
    "adresse"               TEXT,
    "telephone"             VARCHAR(20),
    "emailContact"          VARCHAR(255),
    "siteWeb"               VARCHAR(300),
    "reseauxSociaux"        VARCHAR(500),
    "prixMin"               DECIMAL(10,2)   DEFAULT 0 CHECK ("prixMin" >= 0),
    "prixMax"               DECIMAL(10,2)   DEFAULT 0 CHECK ("prixMax" >= 0),
    "typeAbonnement"        "abonnementType" NOT NULL DEFAULT 'basic',
    "tauxCommission"        DECIMAL(5,2)    NOT NULL DEFAULT 7.00,
    "operateurMobileMoney"  "operateurMm",
    "numeroMobileMoney"     VARCHAR(20),
    "frequenceReversement"  "frequenceReversement" NOT NULL DEFAULT 'mensuel',
    "statutProfil"          VARCHAR(20)     NOT NULL DEFAULT 'enAttente'
                                            CHECK ("statutProfil" IN ('enAttente', 'approuve', 'rejete')),
    "motifRejet"            TEXT,
    "validatedAt"           TIMESTAMPTZ,
    "createdAt"             TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    "updatedAt"             TIMESTAMPTZ     NOT NULL DEFAULT NOW(),

    CONSTRAINT "chkPrixCentre"
        CHECK ("prixMax" IS NULL OR "prixMin" IS NULL OR "prixMax" >= "prixMin")
);

CREATE INDEX IF NOT EXISTS "idxProfilCentreFormateur"
    ON "profilCentre" ("idFormateur");
CREATE INDEX IF NOT EXISTS "idxProfilCentreStatut"
    ON "profilCentre" ("statutProfil");
CREATE INDEX IF NOT EXISTS "idxProfilCentreVille"
    ON "profilCentre" ("ville");

-- ── VISITES DE FORMATIONS ─────────────────────────────────────
-- Compteur de consultations par formation (analytics formateur)
CREATE TABLE IF NOT EXISTS "visiteFormation" (
    "idVisite"       BIGSERIAL    PRIMARY KEY,
    "idFormation"    BIGINT       NOT NULL
                                  REFERENCES "formations" ("idFormation")
                                  ON DELETE CASCADE,
    "idUtilisateur"  BIGINT       REFERENCES "utilisateurs" ("idUser")
                                  ON DELETE SET NULL,
    "adresseIp"      VARCHAR(45),
    "dateVisite"     TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS "idxVisiteFormationFormation"
    ON "visiteFormation" ("idFormation");
CREATE INDEX IF NOT EXISTS "idxVisiteFormationDate"
    ON "visiteFormation" ("dateVisite" DESC);

-- ── TRIGGER : updatedAt profilCentre ─────────────────────────
-- Réutilise fnSetUpdatedAt() déjà créée en V10.
DROP TRIGGER IF EXISTS "trgProfilCentreUpdatedAt" ON "profilCentre";
CREATE TRIGGER "trgProfilCentreUpdatedAt"
    BEFORE UPDATE ON "profilCentre"
    FOR EACH ROW EXECUTE FUNCTION "fnSetUpdatedAt"();
