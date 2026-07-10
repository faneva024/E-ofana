-- Description: Tables du module Admin (Semaine 3) — T-B-201.
--
-- Rapport de vérification T-B-201 :
--   - "validationFormations" (V8) couvre déjà l'audit trail des
--     décisions de modération : sa colonne "commentaire" unique sert
--     à la fois de motifRejet ET de commentaireCorrection (un seul
--     champ texte libre, pas deux colonnes séparées) — aucune
--     migration nécessaire sur cette table.
--   - "demandesModificationCentre" (V8) couvre déjà la file d'attente
--     des modifications de profil (donneesProposees JSONB, statut,
--     traiteParId, traiteAt) — aucune migration nécessaire.
--   - "abonnements" (V8) couvre déjà l'historique des changements
--     d'abonnement — aucune migration nécessaire.
--   - "notifications" : AUCUNE table de ce nom n'existe dans les
--     migrations V1-V8, alors que T-B-213 (envoi des identifiants
--     formateur) et T-B-214 (notification de décision de modération)
--     en ont explicitement besoin (typeNotif = CREATION_COMPTE, etc.).
--     CourseValidationService#notifyStatusChange (module Formateur,
--     Semaine 2) documentait déjà ce manque sans le combler, en
--     renvoyant à "une fois ce module [Admin] disponible" — c'est ce
--     module. Ajoutée ci-dessous avec une migration versionnée,
--     conformément au critère d'acceptation de T-B-201.

-- ---------------------------------------------------------------
-- typeNotification / canalNotification / statutEnvoi
-- ---------------------------------------------------------------
CREATE TYPE "typeNotification" AS ENUM (
    'creationCompte',
    'formationApprouvee',
    'formationRejetee',
    'formationCorrectionDemandee',
    'profilApprouve',
    'profilRejete',
    'systeme'
);

CREATE TYPE "canalNotification" AS ENUM ('email', 'sms', 'lesDeux');

-- Réutilisé pour statutEmail ET statutSms : mêmes valeurs possibles
-- des deux côtés, pas de raison d'avoir deux types ENUM distincts.
CREATE TYPE "statutEnvoi" AS ENUM ('enAttente', 'envoye', 'erreur');

-- ---------------------------------------------------------------
-- notifications (file d'attente email / SMS)
-- ---------------------------------------------------------------
CREATE TABLE "notifications" (
    "idNotification"  BIGSERIAL               PRIMARY KEY,
    "idUser"          BIGINT                  NOT NULL REFERENCES "utilisateurs" ("idUser") ON DELETE CASCADE,
    "titre"           VARCHAR(200),
    "message"         TEXT,
    "typeNotif"       "typeNotification"      NOT NULL DEFAULT 'systeme',
    "canal"           "canalNotification"     NOT NULL DEFAULT 'email',
    "statutEmail"     "statutEnvoi"           NOT NULL DEFAULT 'enAttente',
    "estLu"           BOOLEAN                 NOT NULL DEFAULT FALSE,
    "sentAt"          TIMESTAMPTZ,
    "createdAt"       TIMESTAMPTZ             NOT NULL DEFAULT NOW()
);

CREATE INDEX "idxNotificationsUser"        ON "notifications" ("idUser");
CREATE INDEX "idxNotificationsStatutEmail" ON "notifications" ("statutEmail")
    WHERE "statutEmail" = 'enAttente';

COMMENT ON TABLE  "notifications"             IS 'File d''attente des envois email/SMS (tentative immédiate best-effort + trace pour audit/relance)';
COMMENT ON COLUMN "notifications"."typeNotif" IS 'creationCompte = identifiants formateur (T-B-213) ; formation*/profil* = décisions de modération (T-B-205/T-B-206)';
