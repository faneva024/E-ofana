 --  E-OFANA — Migration Flyway V1
--  Création de la table : apprenant
--  T-B-001
--  
 

CREATE TABLE IF NOT EXISTS apprenant (
    id                 BIGSERIAL       PRIMARY KEY,
    email              VARCHAR(180)    NOT NULL UNIQUE,
    mot_de_passe       VARCHAR(255)    NOT NULL,
    prenom             VARCHAR(100)    NOT NULL,
    nom                VARCHAR(100)    NOT NULL,
    telephone          VARCHAR(20),
    date_creation      TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    date_modification  TIMESTAMPTZ     NOT NULL DEFAULT NOW()
);

-- Index sur email pour les recherches de connexion
CREATE INDEX IF NOT EXISTS idx_apprenant_email
    ON apprenant (email);

COMMENT ON TABLE  apprenant                IS 'Comptes apprenants inscrits sur la plateforme E-OFANA';
COMMENT ON COLUMN apprenant.mot_de_passe  IS 'Hash BCrypt du mot de passe (min 8 caractères)';
COMMENT ON COLUMN apprenant.email         IS 'Identifiant de connexion unique par apprenant';
