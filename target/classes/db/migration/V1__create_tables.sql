-- Table utilisateurs
CREATE TABLE IF NOT EXISTS utilisateurs (
    id_user BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    mot_de_passe VARCHAR(255) NOT NULL,
    prenom VARCHAR(100) NOT NULL,
    nom VARCHAR(100) NOT NULL,
    telephone VARCHAR(20),
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

-- Table formations
CREATE TABLE IF NOT EXISTS formations (
    id_formation BIGSERIAL PRIMARY KEY,
    id_formateur BIGINT NOT NULL REFERENCES utilisateurs(id_user),
    titre VARCHAR(255) NOT NULL,
    description TEXT,
    categorie VARCHAR(100),
    lieu VARCHAR(255),
    date_debut TIMESTAMP,
    date_limite_inscription TIMESTAMP,
    places_disponibles INT NOT NULL DEFAULT 0,
    prix DECIMAL(10,2) NOT NULL DEFAULT 0,
    prix_remise DECIMAL(10,2),
    chemin_image VARCHAR(500),
    statut VARCHAR(20) NOT NULL DEFAULT 'enAttente',
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

-- Table inscriptions
CREATE TABLE IF NOT EXISTS inscriptions (
    id_inscription BIGSERIAL PRIMARY KEY,
    id_utilisateur BIGINT NOT NULL REFERENCES utilisateurs(id_user),
    id_formation BIGINT NOT NULL REFERENCES formations(id_formation),
    statut_paiement VARCHAR(20) NOT NULL DEFAULT 'enAttente',
    reservation_uniquement BOOLEAN NOT NULL DEFAULT FALSE,
    date_inscription TIMESTAMP NOT NULL DEFAULT NOW(),
    numero_recu VARCHAR(50),
    operateur_paiement VARCHAR(50),
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

-- Table paiements
CREATE TABLE IF NOT EXISTS paiements (
    id_paiement BIGSERIAL PRIMARY KEY,
    id_inscription BIGINT NOT NULL REFERENCES inscriptions(id_inscription),
    montant DECIMAL(10,2) NOT NULL,
    operateur VARCHAR(50) NOT NULL,
    id_transaction VARCHAR(100),
    statut VARCHAR(20) NOT NULL DEFAULT 'enAttente',
    date_paiement TIMESTAMP NOT NULL DEFAULT NOW()
);

-- Index
CREATE INDEX IF NOT EXISTS idx_utilisateurs_email ON utilisateurs(email);
CREATE INDEX IF NOT EXISTS idx_formations_id_formateur ON formations(id_formateur);
CREATE INDEX IF NOT EXISTS idx_formations_statut ON formations(statut);
CREATE INDEX IF NOT EXISTS idx_inscriptions_id_utilisateur ON inscriptions(id_utilisateur);
CREATE INDEX IF NOT EXISTS idx_inscriptions_id_formation ON inscriptions(id_formation);
CREATE INDEX IF NOT EXISTS idx_paiements_id_inscription ON paiements(id_inscription);
