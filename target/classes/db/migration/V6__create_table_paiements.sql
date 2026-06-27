-- Description: Création de la table de suivi des transactions de paiement Mobile Money
CREATE TABLE "paiements" (
    "idPaiement"          BIGSERIAL           PRIMARY KEY,
    "idInscription"       BIGINT              NOT NULL REFERENCES "inscriptions" ("idInscription"),
    "montant"             BIGINT              NOT NULL CHECK ("montant" > 0),
    "operateur"           "operateurMm"       NOT NULL,
    "numeroTransaction"   VARCHAR(100)        NOT NULL,
    "statut"              "statutPaiement"    NOT NULL DEFAULT 'enAttente',
    "createdAt"           TIMESTAMPTZ         NOT NULL DEFAULT NOW()
);

