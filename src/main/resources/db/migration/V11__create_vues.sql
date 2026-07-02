-- Description : Vues utilitaires (catalogue, dashboards, modération, finances)
--               Requiert : V9-V10 (tables et triggers)
SET search_path TO eofana;

-- ----------------------------------------------------------------
-- Vue 1 : Catalogue public — formations approuvées + prochaine session
-- ----------------------------------------------------------------
CREATE OR REPLACE VIEW "vCatalogue" AS
SELECT
    f."idFormation",
    f."titre",
    f."description",
    f."image",
    f."duree",
    f."lieu",
    f."prix",
    f."prixRemise",
    ROUND(COALESCE(f."prixRemise", f."prix") * 0.95)    AS "prixEofana",
    f."noteMoyenne",
    f."nbAvis",
    -- Centre
    c."idCentre"       AS "idCentre",
    c."nom"            AS "centreNom",
    c."ville"          AS "centreVille",
    c."logo"           AS "centreLogo",
    c."abonnement"     AS "centreAbonnement",
    -- Catégorie
    cat."idCategorie"  AS "idCategorie",
    cat."nom"          AS "categorieNom",
    cat."icone"        AS "categorieIcone",
    cat."couleur"      AS "categorieCouleur",
    -- Prochaine session disponible
    MIN(s."idSession")       AS "prochaineSessionId",
    MIN(s."dateDebut")       AS "prochaineSessionDate",
    MIN(s."dateFin")         AS "prochaineSessionDateFin",
    MIN(s."placesRestantes") AS "placesRestantesMin",
    COUNT(s."idSession")     AS "nbSessionsOuvertes"
FROM "formations" f
JOIN "centres"           c   ON c."idCentre"    = f."idCentre"
LEFT JOIN "categories"   cat ON cat."idCategorie" = f."idCategorie"
LEFT JOIN "sessionsFormation" s
          ON s."idFormation"           = f."idFormation"
         AND s."statut"                = 'ouvert'
         AND s."dateLimiteInscription" >= CURRENT_DATE
WHERE f."statut" = 'approuve'
  AND c."statut" = 'actif'
GROUP BY f."idFormation", c."idCentre", cat."idCategorie";

COMMENT ON VIEW "vCatalogue"
    IS 'Catalogue public des formations approuvées avec leur prochaine session disponible';

-- ----------------------------------------------------------------
-- Vue 2 : Tableau de bord formateur — statistiques par centre
-- ----------------------------------------------------------------
CREATE OR REPLACE VIEW "vDashboardFormateur" AS
SELECT
    c."idCentre",
    c."nom"            AS "centreNom",
    c."abonnement",
    c."tauxCommission",
    -- Formations
    COUNT(DISTINCT f."idFormation")                                                   AS "nbFormationsTotal",
    COUNT(DISTINCT CASE WHEN f."statut" = 'approuve'  THEN f."idFormation" END)       AS "nbFormationsActives",
    COUNT(DISTINCT CASE WHEN f."statut" = 'enAttente' THEN f."idFormation" END)       AS "nbFormationsEnAttente",
    COUNT(DISTINCT CASE WHEN f."statut" = 'brouillon' THEN f."idFormation" END)       AS "nbFormationsBrouillon",
    -- Sessions
    COUNT(DISTINCT s."idSession")                                                      AS "nbSessionsTotal",
    COUNT(DISTINCT CASE WHEN s."statut" = 'ouvert' THEN s."idSession" END)            AS "nbSessionsOuvertes",
    -- Inscriptions & finances
    COUNT(DISTINCT i."idInscription")                                                  AS "nbInscriptionsTotal",
    COALESCE(SUM(CASE WHEN i."statut" = 'valide' THEN i."montantPaye"      END), 0)  AS "revenusBruts",
    COALESCE(SUM(CASE WHEN i."statut" = 'valide' THEN i."commission"       END), 0)  AS "totalCommissions",
    COALESCE(SUM(CASE WHEN i."statut" = 'valide' THEN i."montantFormateur" END), 0)  AS "revenusNets",
    -- Qualité
    COALESCE(ROUND(AVG(CASE WHEN f."nbAvis" > 0 THEN f."noteMoyenne" END)::NUMERIC, 2), 0)
        AS "noteMoyenneGlobale"
FROM "centres" c
LEFT JOIN "formations"        f ON f."idCentre"   = c."idCentre"
LEFT JOIN "sessionsFormation" s ON s."idFormation" = f."idFormation"
LEFT JOIN "inscriptions"      i ON i."idSession"   = s."idSession"
GROUP BY c."idCentre";

COMMENT ON VIEW "vDashboardFormateur"
    IS 'Statistiques agrégées par centre de formation pour le tableau de bord formateur';

-- ----------------------------------------------------------------
-- Vue 3 : Statistiques globales administrateur
-- ----------------------------------------------------------------
CREATE OR REPLACE VIEW "vStatsAdmin" AS
SELECT
    -- Formations
    (SELECT COUNT(*)                                            FROM "formations")                          AS "formationsTotal",
    (SELECT COUNT(*) FROM "formations" WHERE "statut" = 'enAttente')                                       AS "formationsEnAttente",
    (SELECT COUNT(*) FROM "formations" WHERE "statut" = 'approuve')                                        AS "formationsApprouvees",
    -- Utilisateurs
    (SELECT COUNT(*) FROM "utilisateurs" WHERE "role" = 'apprenant')                                       AS "nbApprenants",
    (SELECT COUNT(*) FROM "utilisateurs" WHERE "role" = 'formateur')                                       AS "nbFormateurs",
    (SELECT COUNT(*) FROM "utilisateurs" WHERE "estActif" = TRUE)                                          AS "nbUtilisateursActifs",
    -- Centres
    (SELECT COUNT(*) FROM "centres" WHERE "statut" = 'actif')                                              AS "nbCentresActifs",
    (SELECT COUNT(*) FROM "centres" WHERE "statut" = 'enAttente')                                          AS "nbCentresEnAttente",
    -- Finances
    (SELECT COALESCE(SUM("montantPaye"), 0) FROM "inscriptions" WHERE "statut" = 'valide')                AS "volumeTransactionsTotal",
    (SELECT COALESCE(SUM("commission"),  0) FROM "inscriptions" WHERE "statut" = 'valide')                AS "commissionsTotales",
    (SELECT COUNT(*)                        FROM "inscriptions" WHERE "statut" = 'valide')                 AS "inscriptionsValidees",
    (SELECT COUNT(*)                        FROM "inscriptions" WHERE "statut" = 'enAttente')              AS "inscriptionsEnAttente",
    -- Virements
    (SELECT COALESCE(SUM("montantNet"), 0)  FROM "virements"    WHERE "statut" = 'effectue')              AS "virementsEffectuesTotal",
    (SELECT COUNT(*)                        FROM "virements"    WHERE "statut" = 'planifie')               AS "virementsEnAttente";

COMMENT ON VIEW "vStatsAdmin"
    IS 'Indicateurs clés globaux pour le tableau de bord administrateur';

-- ----------------------------------------------------------------
-- Vue 4 : Détail des inscriptions — pour export et suivi
-- ----------------------------------------------------------------
CREATE OR REPLACE VIEW "vInscriptionsDetail" AS
SELECT
    i."idInscription",
    i."numeroRecu",
    i."createdAt"    AS "dateInscription",
    i."statut"       AS "statutInscription",
    i."typeInsc",
    -- Apprenant
    u."idUser",
    u."nom"          AS "apprenantNom",
    u."prenom"       AS "apprenantPrenom",
    u."email"        AS "apprenantEmail",
    u."telephone"    AS "apprenantTelephone",
    -- Formation
    f."idFormation",
    f."titre"        AS "formationTitre",
    -- Session
    s."idSession",
    s."dateDebut",
    s."dateFin",
    -- Centre
    c."idCentre",
    c."nom"          AS "centreNom",
    -- Finances
    i."montantPaye",
    i."remise",
    i."commission",
    i."montantFormateur",
    i."operateur",
    i."transactionId"
FROM "inscriptions" i
JOIN "utilisateurs"       u ON u."idUser"      = i."idUser"
JOIN "sessionsFormation"  s ON s."idSession"   = i."idSession"
JOIN "formations"         f ON f."idFormation" = s."idFormation"
JOIN "centres"            c ON c."idCentre"    = f."idCentre";

COMMENT ON VIEW "vInscriptionsDetail"
    IS 'Vue dénormalisée des inscriptions avec toutes les informations utiles pour export et suivi';

-- ----------------------------------------------------------------
-- Vue 5 : Remboursements en attente de traitement
-- ----------------------------------------------------------------
CREATE OR REPLACE VIEW "vRemboursementsEnAttente" AS
SELECT
    r."idRemboursement",
    r."createdAt"       AS "dateDemande",
    r."motif",
    r."tauxApplique",
    r."montantRembourse",
    -- Inscription
    i."idInscription",
    i."numeroRecu",
    i."montantPaye",
    -- Apprenant
    u."nom"             AS "apprenantNom",
    u."prenom"          AS "apprenantPrenom",
    u."email"           AS "apprenantEmail",
    -- Formation + session
    f."titre"           AS "formationTitre",
    s."dateDebut"       AS "sessionDateDebut",
    c."nom"             AS "centreNom"
FROM "remboursements" r
JOIN "inscriptions"      i ON i."idInscription" = r."idInscription"
JOIN "utilisateurs"      u ON u."idUser"        = i."idUser"
JOIN "sessionsFormation" s ON s."idSession"     = i."idSession"
JOIN "formations"        f ON f."idFormation"   = s."idFormation"
JOIN "centres"           c ON c."idCentre"      = f."idCentre"
WHERE r."statut" = 'enAttente'
ORDER BY r."createdAt";

COMMENT ON VIEW "vRemboursementsEnAttente"
    IS 'Remboursements en attente de traitement par l''administration';

-- ----------------------------------------------------------------
-- Vue 6 : Virements à planifier (inscriptions validées non reversées)
-- ----------------------------------------------------------------
CREATE OR REPLACE VIEW "vVirementsAPlanifier" AS
SELECT
    c."idCentre",
    c."nom"                    AS "centreNom",
    c."mobileMoneyOperateur",
    c."mobileMoneyNumero",
    c."frequenceReversement",
    COUNT(DISTINCT i."idInscription") AS "nbInscriptions",
    SUM(i."montantPaye")       AS "montantBrut",
    SUM(i."commission")        AS "totalCommission",
    SUM(i."montantFormateur")  AS "montantNetAReverser",
    MIN(i."createdAt")         AS "premiereInscription",
    MAX(i."createdAt")         AS "derniereInscription"
FROM "inscriptions" i
JOIN "sessionsFormation" s ON s."idSession"   = i."idSession"
JOIN "formations"        f ON f."idFormation" = s."idFormation"
JOIN "centres"           c ON c."idCentre"    = f."idCentre"
WHERE i."statut" = 'valide'
  AND NOT EXISTS (
      SELECT 1 FROM "virements" v
       WHERE v."idCentre" = c."idCentre"
         AND v."statut"   = 'effectue'
         AND i."createdAt" BETWEEN v."periodeDebut" AND v."periodeFin"
  )
GROUP BY c."idCentre"
HAVING SUM(i."montantFormateur") > 0;

COMMENT ON VIEW "vVirementsAPlanifier"
    IS 'Montants nets dus aux centres et non encore reversés';

-- ----------------------------------------------------------------
-- Vue 7 : Formations en attente de modération
-- ----------------------------------------------------------------
CREATE OR REPLACE VIEW "vFormationsAModerer" AS
SELECT
    f."idFormation",
    f."titre",
    f."statut",
    f."createdAt"   AS "soumiseLe",
    f."updatedAt"   AS "derniereModif",
    c."idCentre",
    c."nom"         AS "centreNom",
    u."nom"         AS "formateurNom",
    u."prenom"      AS "formateurPrenom",
    u."email"       AS "formateurEmail"
FROM "formations" f
JOIN "centres"     c ON c."idCentre" = f."idCentre"
JOIN "utilisateurs" u ON u."idUser"  = c."idUser"
WHERE f."statut" IN ('enAttente', 'correctionDemandee')
ORDER BY f."createdAt";

COMMENT ON VIEW "vFormationsAModerer"
    IS 'Formations en attente de validation ou de correction par un modérateur';
