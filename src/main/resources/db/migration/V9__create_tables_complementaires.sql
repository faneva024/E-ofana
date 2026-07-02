-- Description : Tables complémentaires absentes de V3-V6
--               Ordre : dépendances respectées (FK résolues dans l'ordre)
--               Requiert : V8 (types), V4 (centres, formations), V5 (inscriptions, sessionsFormation)
SET search_path TO eofana;

-- ╔══════════════════════════════════════════════════════════════╗
-- ║  1. DEMANDES DE MODIFICATION DE PROFIL CENTRE                ║
-- ║     Dépend de : centres, utilisateurs                        ║
-- ╚══════════════════════════════════════════════════════════════╝
CREATE TABLE "demandesModificationCentre" (
    "idDemande"          BIGSERIAL            PRIMARY KEY,
    "idCentre"           BIGINT               NOT NULL
                                              REFERENCES "centres" ("idCentre") ON DELETE CASCADE,
    "donneesProposees"   JSONB                NOT NULL,
    "commentaireAdmin"   TEXT,
    "statut"             "statutModification" NOT NULL DEFAULT 'enAttente',
    "traiteParId"        BIGINT               REFERENCES "utilisateurs" ("idUser"),
    "traiteAt"           TIMESTAMPTZ,
    "createdAt"          TIMESTAMPTZ          NOT NULL DEFAULT NOW()
);

CREATE INDEX "idxDemandesCentre"  ON "demandesModificationCentre" ("idCentre");
CREATE INDEX "idxDemandesStatut"  ON "demandesModificationCentre" ("statut");

COMMENT ON TABLE "demandesModificationCentre"
    IS 'Modifications de profil centre soumises à validation modérateur';

-- ╔══════════════════════════════════════════════════════════════╗
-- ║  2. HISTORIQUE DES ABONNEMENTS                               ║
-- ║     Dépend de : centres                                      ║
-- ╚══════════════════════════════════════════════════════════════╝
CREATE TABLE "abonnements" (
    "idAbonnement"    BIGSERIAL           PRIMARY KEY,
    "idCentre"        BIGINT              NOT NULL
                                          REFERENCES "centres" ("idCentre") ON DELETE CASCADE,
    "typeAbonnement"  "abonnementType"    NOT NULL,
    "prixMensuel"     INTEGER             NOT NULL CHECK ("prixMensuel" > 0),
    "tauxCommission"  NUMERIC(5,2)        NOT NULL,
    "dateDebut"       DATE                NOT NULL,
    "dateFin"         DATE,
    "statut"          VARCHAR(20)         NOT NULL DEFAULT 'actif'
                                          CHECK ("statut" IN ('actif', 'expire', 'suspendu')),
    "createdAt"       TIMESTAMPTZ         NOT NULL DEFAULT NOW(),

    CONSTRAINT "chkAboDates" CHECK ("dateFin" IS NULL OR "dateFin" >= "dateDebut")
);

CREATE INDEX "idxAbonnementsCentre" ON "abonnements" ("idCentre");
CREATE INDEX "idxAbonnementsStatut" ON "abonnements" ("statut");

COMMENT ON TABLE "abonnements"
    IS 'Historique des abonnements souscrits par chaque centre (basic 60 000 Ar/mois, premium 200 000 Ar/mois)';

-- ╔══════════════════════════════════════════════════════════════╗
-- ║  3. HISTORIQUE DE VALIDATION DES FORMATIONS                  ║
-- ║     Dépend de : formations, utilisateurs                     ║
-- ╚══════════════════════════════════════════════════════════════╝
CREATE TABLE "validationFormations" (
    "idValidation"   BIGSERIAL           PRIMARY KEY,
    "idFormation"    BIGINT              NOT NULL
                                         REFERENCES "formations" ("idFormation") ON DELETE CASCADE,
    "idAdmin"        BIGINT              NOT NULL REFERENCES "utilisateurs" ("idUser"),
    "ancienStatut"   "statutFormation",
    "nouveauStatut"  "statutFormation",
    "commentaire"    TEXT,
    "createdAt"      TIMESTAMPTZ         NOT NULL DEFAULT NOW()
);

CREATE INDEX "idxValidationFormation" ON "validationFormations" ("idFormation");

COMMENT ON TABLE "validationFormations"
    IS 'Audit trail de toutes les décisions de modération sur les formations';

-- ╔══════════════════════════════════════════════════════════════╗
-- ║  4. REMBOURSEMENTS                                           ║
-- ║     Dépend de : inscriptions, utilisateurs                   ║
-- ║     Utilise  : statutRemboursement (V8)                      ║
-- ╚══════════════════════════════════════════════════════════════╝
CREATE TABLE "remboursements" (
    "idRemboursement"     BIGSERIAL                PRIMARY KEY,
    "idInscription"       BIGINT                   NOT NULL
                                                   REFERENCES "inscriptions" ("idInscription"),
    "montantRembourse"    BIGINT                   NOT NULL CHECK ("montantRembourse" >= 0),
    "tauxApplique"        NUMERIC(5,2)             NOT NULL
                                                   CHECK ("tauxApplique" IN (0, 50, 80, 100)),
    "motif"               TEXT,
    "statut"              "statutRemboursement"    NOT NULL DEFAULT 'enAttente',
    "idAdminTraitant"     BIGINT                   REFERENCES "utilisateurs" ("idUser"),
    "createdAt"           TIMESTAMPTZ              NOT NULL DEFAULT NOW(),
    "processedAt"         TIMESTAMPTZ
);

CREATE INDEX "idxRemboursementsInscription" ON "remboursements" ("idInscription");
CREATE INDEX "idxRemboursementsStatut"      ON "remboursements" ("statut");

COMMENT ON TABLE "remboursements"
    IS 'Demandes de remboursement : annulation formateur=100%, apprenant >7j=80%, 3-7j=50%, <3j=0%';

-- ╔══════════════════════════════════════════════════════════════╗
-- ║  5. VIREMENTS AUX FORMATEURS                                 ║
-- ║     Dépend de : centres                                      ║
-- ║     Utilise  : statutVirement, operateurMm (V2 + V8)         ║
-- ╚══════════════════════════════════════════════════════════════╝
CREATE TABLE "virements" (
    "idVirement"          BIGSERIAL           PRIMARY KEY,
    "idCentre"            BIGINT              NOT NULL REFERENCES "centres" ("idCentre"),
    "montantBrut"         BIGINT              NOT NULL CHECK ("montantBrut" >= 0),
    "commission"          BIGINT              NOT NULL CHECK ("commission" >= 0),
    "montantNet"          BIGINT              NOT NULL CHECK ("montantNet" >= 0),
    "operateur"           "operateurMm",
    "referenceVirement"   VARCHAR(100),
    "statut"              "statutVirement"    NOT NULL DEFAULT 'planifie',
    "periodeDebut"        DATE,
    "periodeFin"          DATE,
    "dateVirement"        TIMESTAMPTZ,
    "createdAt"           TIMESTAMPTZ         NOT NULL DEFAULT NOW(),

    CONSTRAINT "chkVirementNet"
        CHECK ("montantNet" = "montantBrut" - "commission"),
    CONSTRAINT "chkVirementPeriode"
        CHECK ("periodeFin" IS NULL OR "periodeFin" >= "periodeDebut")
);

CREATE INDEX "idxVirementsCentre" ON "virements" ("idCentre");
CREATE INDEX "idxVirementsStatut" ON "virements" ("statut");

COMMENT ON TABLE "virements"
    IS 'Reversements périodiques (hebdo ou mensuel) d''E-OFANA vers les centres de formation';

-- ╔══════════════════════════════════════════════════════════════╗
-- ║  6. INSCRIPTIONS GROUPÉES B2B                                ║
-- ║     entreprises → inscriptionsGroupees → participantsGroupe  ║
-- ╚══════════════════════════════════════════════════════════════╝
CREATE TABLE "entreprises" (
    "idEntreprise" BIGSERIAL       PRIMARY KEY,
    "nom"          VARCHAR(200)    NOT NULL,
    "email"        VARCHAR(180),
    "telephone"    VARCHAR(30),
    "adresse"      TEXT,
    "createdAt"    TIMESTAMPTZ     NOT NULL DEFAULT NOW()
);

COMMENT ON TABLE "entreprises"
    IS 'Entreprises clientes pour les inscriptions B2B';

CREATE TABLE "inscriptionsGroupees" (
    "idGroupe"            BIGSERIAL       PRIMARY KEY,
    "idEntreprise"        BIGINT          NOT NULL REFERENCES "entreprises" ("idEntreprise"),
    "idSession"           BIGINT          NOT NULL
                                          REFERENCES "sessionsFormation" ("idSession"),
    "idResponsable"       BIGINT          NOT NULL REFERENCES "utilisateurs" ("idUser"),
    "montantTotal"        BIGINT          NOT NULL CHECK ("montantTotal" > 0),
    "operateur"           "operateurMm",
    "numeroTransaction"   VARCHAR(100),
    "statut"              VARCHAR(20)     NOT NULL DEFAULT 'enAttente'
                                          CHECK ("statut" IN ('enAttente', 'confirme', 'annule')),
    "createdAt"           TIMESTAMPTZ     NOT NULL DEFAULT NOW()
);

CREATE INDEX "idxInscriptionsGroupeesEntreprise" ON "inscriptionsGroupees" ("idEntreprise");
CREATE INDEX "idxInscriptionsGroupeesSession"    ON "inscriptionsGroupees" ("idSession");

COMMENT ON TABLE "inscriptionsGroupees"
    IS 'Inscription B2B : une entreprise inscrit plusieurs employés en un seul paiement';

CREATE TABLE "participantsGroupe" (
    "idParticipant"  BIGSERIAL       PRIMARY KEY,
    "idGroupe"       BIGINT          NOT NULL
                                     REFERENCES "inscriptionsGroupees" ("idGroupe") ON DELETE CASCADE,
    "nom"            VARCHAR(100)    NOT NULL,
    "prenom"         VARCHAR(100)    NOT NULL,
    "email"          VARCHAR(180),
    "numeroRecu"     VARCHAR(50)     UNIQUE,
    "createdAt"      TIMESTAMPTZ     NOT NULL DEFAULT NOW()
);

CREATE INDEX "idxParticipantsGroupeInscription" ON "participantsGroupe" ("idGroupe");

COMMENT ON TABLE "participantsGroupe"
    IS 'Participants d''une inscription groupée B2B — chacun reçoit un reçu PDF individuel';

-- ╔══════════════════════════════════════════════════════════════╗
-- ║  7. AVIS ET NOTATIONS                                        ║
-- ║     Dépend de : inscriptions, utilisateurs, formations       ║
-- ╚══════════════════════════════════════════════════════════════╝
CREATE TABLE "avis" (
    "idAvis"         BIGSERIAL   PRIMARY KEY,
    "idInscription"  BIGINT      NOT NULL UNIQUE
                                 REFERENCES "inscriptions" ("idInscription"),
    "idUser"         BIGINT      NOT NULL REFERENCES "utilisateurs" ("idUser"),
    "idFormation"    BIGINT      NOT NULL
                                 REFERENCES "formations" ("idFormation") ON DELETE CASCADE,
    "note"           SMALLINT    NOT NULL CHECK ("note" BETWEEN 1 AND 5),
    "commentaire"    TEXT,
    "estApprouve"    BOOLEAN     NOT NULL DEFAULT FALSE,
    "idModerateur"   BIGINT      REFERENCES "utilisateurs" ("idUser"),
    "createdAt"      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX "idxAvisFormation"  ON "avis" ("idFormation");
CREATE INDEX "idxAvisApprouve"   ON "avis" ("estApprouve");
CREATE INDEX "idxAvisUser"       ON "avis" ("idUser");

COMMENT ON TABLE  "avis"
    IS 'Notes (1-5) et commentaires des apprenants, visibles uniquement après approbation modérateur';
COMMENT ON COLUMN "avis"."idInscription"
    IS 'Garantit un seul avis par inscription terminée';

-- ╔══════════════════════════════════════════════════════════════╗
-- ║  8. NOTIFICATIONS (file d''attente email / SMS)              ║
-- ║     Dépend de : utilisateurs, inscriptions                   ║
-- ║     Utilise  : typeNotification, canalNotification,          ║
-- ║                statutEmail, statutSms (V8)                   ║
-- ╚══════════════════════════════════════════════════════════════╝
CREATE TABLE "notifications" (
    "idNotification"  BIGSERIAL               PRIMARY KEY,
    "idUser"          BIGINT                  NOT NULL
                                              REFERENCES "utilisateurs" ("idUser") ON DELETE CASCADE,
    "idInscription"   BIGINT                  REFERENCES "inscriptions" ("idInscription"),
    "titre"           VARCHAR(200),
    "message"         TEXT,
    "typeNotif"       "typeNotification"      NOT NULL DEFAULT 'systeme',
    "canal"           "canalNotification"     NOT NULL DEFAULT 'email',
    "statutEmail"     "statutEmail"           DEFAULT 'enAttente',
    "statutSms"       "statutSms"             DEFAULT 'enAttente',
    "estLu"           BOOLEAN                 NOT NULL DEFAULT FALSE,
    "scheduledAt"     TIMESTAMPTZ,
    "sentAt"          TIMESTAMPTZ,
    "createdAt"       TIMESTAMPTZ             NOT NULL DEFAULT NOW()
);

CREATE INDEX "idxNotificationsUser"
    ON "notifications" ("idUser");
CREATE INDEX "idxNotificationsStatutEmail"
    ON "notifications" ("statutEmail")
    WHERE "statutEmail" = 'enAttente';
CREATE INDEX "idxNotificationsScheduled"
    ON "notifications" ("scheduledAt")
    WHERE "statutEmail" = 'enAttente' OR "statutSms" = 'enAttente';
CREATE INDEX "idxNotificationsLu"
    ON "notifications" ("estLu");

COMMENT ON TABLE  "notifications"
    IS 'File d''attente des envois email et SMS, traitée par cron job';
COMMENT ON COLUMN "notifications"."typeNotif"
    IS 'rappelJ3 et rappelJ1 = envoyés automatiquement 3j et 1j avant la session';
