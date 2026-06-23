-- ============================================================
--  E-OFANA — Migration Flyway V3
--  Création de la table : inscription
--  T-B-001

CREATE TABLE IF NOT EXISTS inscription (
    id                    BIGSERIAL       PRIMARY KEY,
    apprenant_id          BIGINT          NOT NULL
                                          REFERENCES apprenant (id) ON DELETE RESTRICT,
    formation_id          BIGINT          NOT NULL
                                          REFERENCES formation (id) ON DELETE RESTRICT,
    statut_paiement       VARCHAR(30)     NOT NULL DEFAULT 'en_attente'
                                          CHECK (statut_paiement IN ('en_attente','complete','partiel','annule')),
    reservation_uniquement BOOLEAN        NOT NULL DEFAULT FALSE,
    date_inscription      TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    numero_recu           VARCHAR(50)     UNIQUE,
    operateur_paiement    VARCHAR(30)
                                          CHECK (operateur_paiement IN ('mvola','orange_money','airtel_money')),
    date_creation         TIMESTAMPTZ     NOT NULL DEFAULT NOW(),

    -- Un apprenant ne peut s'inscrire qu'une seule fois à une formation
    CONSTRAINT uq_apprenant_formation UNIQUE (apprenant_id, formation_id)
);

-- Index sur les clés étrangères
CREATE INDEX IF NOT EXISTS idx_inscription_apprenant
    ON inscription (apprenant_id);

CREATE INDEX IF NOT EXISTS idx_inscription_formation
    ON inscription (formation_id);

CREATE INDEX IF NOT EXISTS idx_inscription_statut
    ON inscription (statut_paiement);

COMMENT ON TABLE  inscription                         IS 'Inscriptions et réservations des apprenants aux formations';
COMMENT ON COLUMN inscription.reservation_uniquement  IS 'TRUE = réservation (paiement commission seule), FALSE = inscription complète';
COMMENT ON COLUMN inscription.numero_recu             IS 'Format : REC-AAAA-XXXXX, généré après confirmation du paiement';
COMMENT ON COLUMN inscription.statut_paiement         IS 'en_attente → complete (inscription) ou partiel (réservation)';
