--  E-OFANA — Migration Flyway V2
--  Création de la table : formation
--  T-B-001

CREATE TABLE IF NOT EXISTS formation (
    id                       BIGSERIAL       PRIMARY KEY,
    formateur_id             BIGINT          NOT NULL,
    titre                    VARCHAR(250)    NOT NULL,
    description              TEXT            NOT NULL,
    categorie                VARCHAR(100)    NOT NULL,
    lieu                     VARCHAR(200)    NOT NULL,
    date_debut               DATE            NOT NULL,
    date_limite_inscription  DATE            NOT NULL,
    places_disponibles       SMALLINT        NOT NULL CHECK (places_disponibles >= 0),
    prix                     BIGINT          NOT NULL CHECK (prix >= 0),
    prix_remise              BIGINT          CHECK (prix_remise >= 0 AND prix_remise < prix),
    chemin_image             VARCHAR(300),
    statut                   VARCHAR(30)     NOT NULL DEFAULT 'en_attente'
                                             CHECK (statut IN ('en_attente','approuve','rejete','archive')),
    date_creation            TIMESTAMPTZ     NOT NULL DEFAULT NOW(),

    CONSTRAINT chk_date_limite CHECK (date_limite_inscription <= date_debut)
);

-- Index sur les colonnes de filtre les plus fréquentes
CREATE INDEX IF NOT EXISTS idx_formation_statut
    ON formation (statut);

CREATE INDEX IF NOT EXISTS idx_formation_categorie
    ON formation (categorie);

CREATE INDEX IF NOT EXISTS idx_formation_formateur
    ON formation (formateur_id);

CREATE INDEX IF NOT EXISTS idx_formation_date_debut
    ON formation (date_debut);

COMMENT ON TABLE  formation                          IS 'Formations publiées par les centres sur E-OFANA';
COMMENT ON COLUMN formation.statut                   IS 'Workflow : en_attente → approuve / rejete / archive';
COMMENT ON COLUMN formation.places_disponibles       IS 'Décrémenté à chaque inscription confirmée';
COMMENT ON COLUMN formation.prix_remise              IS 'Prix promotionnel optionnel, doit être inférieur au prix normal';
