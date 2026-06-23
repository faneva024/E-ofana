-- ============================================================
--  E-OFANA — Migration Flyway V4
--  Création de la table : paiement
--  T-B-001


CREATE TABLE IF NOT EXISTS paiement (
    id              BIGSERIAL       PRIMARY KEY,
    inscription_id  BIGINT          NOT NULL
                                    REFERENCES inscription (id) ON DELETE RESTRICT,
    montant         BIGINT          NOT NULL CHECK (montant > 0),
    operateur       VARCHAR(30)     NOT NULL
                                    CHECK (operateur IN ('mvola','orange_money','airtel_money')),
    id_transaction  VARCHAR(100)    NOT NULL UNIQUE,
    statut          VARCHAR(30)     NOT NULL DEFAULT 'en_attente'
                                    CHECK (statut IN ('en_attente','confirme','echoue','rembourse')),
    date_paiement   TIMESTAMPTZ     NOT NULL DEFAULT NOW()
);

-- Index sur inscription_id pour récupérer les paiements d'une inscription
CREATE INDEX IF NOT EXISTS idx_paiement_inscription
    ON paiement (inscription_id);

CREATE INDEX IF NOT EXISTS idx_paiement_statut
    ON paiement (statut);

CREATE INDEX IF NOT EXISTS idx_paiement_id_transaction
    ON paiement (id_transaction);

COMMENT ON TABLE  paiement               IS 'Journal des paiements Mobile Money liés aux inscriptions';
COMMENT ON COLUMN paiement.id_transaction IS 'Référence unique retournée par l''API de l''opérateur Mobile Money';
COMMENT ON COLUMN paiement.montant        IS 'Montant en Ariary Malagasy (entier, pas de décimale)';
