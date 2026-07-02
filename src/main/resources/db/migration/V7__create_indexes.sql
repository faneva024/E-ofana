-- Description: Centralisation de tous les index de performance demandés

-- Index utilisateurs
CREATE INDEX "idxUtilisateursEmail"  ON "utilisateurs" ("email");
CREATE INDEX "idxUtilisateursRole"   ON "utilisateurs" ("role");
CREATE INDEX "idxUtilisateursActif"  ON "utilisateurs" ("estActif");

-- Index centres
CREATE INDEX "idxCentresVille"      ON "centres" ("ville");
CREATE INDEX "idxCentresAbonnement" ON "centres" ("abonnement");
CREATE INDEX "idxCentresStatut"     ON "centres" ("statut");

-- Index formations (Optimisation recherche et filtres)
CREATE INDEX "idxFormationsCentre"      ON "formations" ("idCentre");
CREATE INDEX "idxFormationsCategorie"   ON "formations" ("idCategorie");
CREATE INDEX "idxFormationsStatut"      ON "formations" ("statut");
CREATE INDEX "idxFormationsPrix"        ON "formations" ("prix");
CREATE INDEX "idxFormationsNote"        ON "formations" ("noteMoyenne" DESC);
CREATE INDEX "idxFormationsFts"         ON "formations" USING GIN (to_tsvector('french', "titre" || ' ' || COALESCE("description", '')));

-- Index sessions
CREATE INDEX "idxSessionsFormation"     ON "sessionsFormation" ("idFormation");
CREATE INDEX "idxSessionsDateDebut"     ON "sessionsFormation" ("dateDebut");
CREATE INDEX "idxSessionsStatut"        ON "sessionsFormation" ("statut");

-- Index inscriptions
CREATE INDEX "idxInscriptionsUser"      ON "inscriptions" ("idUser");
CREATE INDEX "idxInscriptionsSession"   ON "inscriptions" ("idSession");
CREATE INDEX "idxInscriptionsStatut"    ON "inscriptions" ("statut");
CREATE INDEX "idxInscriptionsOperateur" ON "inscriptions" ("operateur");
CREATE INDEX "idxInscriptionsCreated"   ON "inscriptions" ("createdAt" DESC);

-- Index paiements
CREATE INDEX "idxPaiementsInscription" ON "paiements" ("idInscription");
CREATE INDEX "idxPaiementsStatut"      ON "paiements" ("statut");

INSERT INTO "sessionsFormation" (
  "idFormation",
  "dateDebut",
  "dateFin",
  "dateLimiteInscription",
  "placesTotal",
  "placesRestantes"
)
SELECT
  f."idFormation",
  CURRENT_DATE + INTERVAL '7 days',
  CURRENT_DATE + INTERVAL '37 days',
  CURRENT_DATE + INTERVAL '6 days',
  20,
  20
FROM formations f
WHERE NOT EXISTS (
  SELECT 1
  FROM "sessionsFormation" s
  WHERE s."idFormation" = f."idFormation"
);