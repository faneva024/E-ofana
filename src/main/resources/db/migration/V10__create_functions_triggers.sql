-- Description : Fonctions PL/pgSQL et triggers automatiques
--               Requiert : V3-V9 (toutes les tables doivent exister)
SET search_path TO eofana;

-- ================================================================
-- FONCTIONS
-- ================================================================

-- ----------------------------------------------------------------
-- fn 1 : Mise à jour automatique de updatedAt
-- ----------------------------------------------------------------
CREATE OR REPLACE FUNCTION "fnSetUpdatedAt"()
RETURNS TRIGGER LANGUAGE plpgsql AS $$
BEGIN
    NEW."updatedAt" = NOW();
    RETURN NEW;
END;
$$;

COMMENT ON FUNCTION "fnSetUpdatedAt"
    IS 'Positionne updatedAt à NOW() avant chaque UPDATE';

-- ----------------------------------------------------------------
-- fn 2 : Génération du numéro de reçu unique (TXN-YYYYMMDD-XXXXX)
-- ----------------------------------------------------------------
CREATE OR REPLACE FUNCTION "fnGenererNumeroRecu"()
RETURNS TRIGGER LANGUAGE plpgsql AS $$
DECLARE
    "vSeq" INT;
BEGIN
    SELECT COUNT(*) + 1
      INTO "vSeq"
      FROM "inscriptions"
     WHERE DATE("createdAt") = CURRENT_DATE;

    NEW."numeroRecu" := 'TXN-' || TO_CHAR(NOW(), 'YYYYMMDD') || '-' || LPAD("vSeq"::TEXT, 5, '0');
    RETURN NEW;
END;
$$;

COMMENT ON FUNCTION "fnGenererNumeroRecu"
    IS 'Génère automatiquement un numéro de reçu unique au format TXN-YYYYMMDD-NNNNN';

-- ----------------------------------------------------------------
-- fn 3 : Calcul automatique des montants de commission
--        Remise 5% E-OFANA (incluse dans la commission)
--        Commission : 7% (basic) ou 15% (premium) sur montantPaye
-- ----------------------------------------------------------------
CREATE OR REPLACE FUNCTION "fnCalculerCommission"()
RETURNS TRIGGER LANGUAGE plpgsql AS $$
DECLARE
    "vTaux"   NUMERIC(5,2);
    "vPrix"   BIGINT;
    "vRemise" BIGINT;
BEGIN
    SELECT c."tauxCommission", COALESCE(f."prixRemise", f."prix")
      INTO "vTaux", "vPrix"
      FROM "sessionsFormation" s
      JOIN "formations"  f ON f."idFormation" = s."idFormation"
      JOIN "centres"     c ON c."idCentre"    = f."idCentre"
     WHERE s."idSession" = NEW."idSession";

    -- Remise 5% offerte par E-OFANA
    "vRemise"          := ROUND("vPrix" * 0.05);
    NEW."remise"       := "vRemise";
    NEW."montantPaye"  := "vPrix" - "vRemise";

    -- Commission (7% ou 15%) calculée sur le montant payé
    NEW."commission"       := ROUND(NEW."montantPaye" * "vTaux" / 100);
    NEW."montantFormateur" := NEW."montantPaye" - NEW."commission";

    RETURN NEW;
END;
$$;

COMMENT ON FUNCTION "fnCalculerCommission"
    IS 'Calcule remise (5%), commission (7% ou 15%) et montant formateur à l''insertion d''une inscription';

-- ----------------------------------------------------------------
-- fn 4 : Gestion des places restantes (décrémentation / restitution)
-- ----------------------------------------------------------------
CREATE OR REPLACE FUNCTION "fnGererPlaces"()
RETURNS TRIGGER LANGUAGE plpgsql AS $$
BEGIN
    -- Inscription validée → décrémenter les places
    IF (TG_OP = 'INSERT' AND NEW."statut" = 'valide')
    OR (TG_OP = 'UPDATE' AND NEW."statut" = 'valide' AND OLD."statut" <> 'valide') THEN

        UPDATE "sessionsFormation"
           SET "placesRestantes" = GREATEST(0, "placesRestantes" - 1)
         WHERE "idSession" = NEW."idSession";

        -- Passer la session en complet si plus de places
        UPDATE "sessionsFormation"
           SET "statut" = 'complet'
         WHERE "idSession"       = NEW."idSession"
           AND "placesRestantes" = 0
           AND "statut"          = 'ouvert';
    END IF;

    -- Annulation d'une inscription validée → restituer la place
    IF TG_OP = 'UPDATE' AND NEW."statut" = 'annule' AND OLD."statut" = 'valide' THEN

        UPDATE "sessionsFormation"
           SET "placesRestantes" = LEAST("placesTotal", "placesRestantes" + 1),
               "statut"          = CASE WHEN "statut" = 'complet' THEN 'ouvert' ELSE "statut" END
         WHERE "idSession" = NEW."idSession";
    END IF;

    RETURN NEW;
END;
$$;

COMMENT ON FUNCTION "fnGererPlaces"
    IS 'Décrémente/restitue les places restantes d''une session selon le statut de l''inscription';

-- ----------------------------------------------------------------
-- fn 5 : Recalcul de la note moyenne d'une formation
-- ----------------------------------------------------------------
CREATE OR REPLACE FUNCTION "fnMajNoteFormation"()
RETURNS TRIGGER LANGUAGE plpgsql AS $$
DECLARE
    "vIdFormation" BIGINT;
BEGIN
    "vIdFormation" := COALESCE(NEW."idFormation", OLD."idFormation");

    UPDATE "formations"
       SET "noteMoyenne" = (
               SELECT COALESCE(ROUND(AVG("note")::NUMERIC, 2), 0)
                 FROM "avis"
                WHERE "idFormation" = "vIdFormation"
                  AND "estApprouve" = TRUE
           ),
           "nbAvis" = (
               SELECT COUNT(*)
                 FROM "avis"
                WHERE "idFormation" = "vIdFormation"
                  AND "estApprouve" = TRUE
           )
     WHERE "idFormation" = "vIdFormation";

    RETURN NULL;  -- AFTER trigger
END;
$$;

COMMENT ON FUNCTION "fnMajNoteFormation"
    IS 'Recalcule noteMoyenne et nbAvis sur la formation après tout changement d''avis approuvé';

-- ----------------------------------------------------------------
-- fn 6 : Synchronisation du taux de commission lors du changement d'abonnement
-- ----------------------------------------------------------------
CREATE OR REPLACE FUNCTION "fnSyncTauxCommission"()
RETURNS TRIGGER LANGUAGE plpgsql AS $$
BEGIN
    IF NEW."abonnement" IS DISTINCT FROM OLD."abonnement" THEN
        NEW."tauxCommission" := CASE NEW."abonnement"
            WHEN 'basic'   THEN 7.00
            WHEN 'premium' THEN 15.00
            ELSE OLD."tauxCommission"
        END;
    END IF;
    RETURN NEW;
END;
$$;

COMMENT ON FUNCTION "fnSyncTauxCommission"
    IS 'Synchronise tauxCommission (7%/15%) automatiquement lors d''un changement d''abonnement centre';

-- ================================================================
-- TRIGGERS
-- ================================================================

-- ----------------------------------------------------------------
-- 2.1  updatedAt automatique sur les tables concernées
-- ----------------------------------------------------------------
CREATE TRIGGER "trgUtilisateursUpdatedAt"
    BEFORE UPDATE ON "utilisateurs"
    FOR EACH ROW EXECUTE FUNCTION "fnSetUpdatedAt"();

CREATE TRIGGER "trgCentresUpdatedAt"
    BEFORE UPDATE ON "centres"
    FOR EACH ROW EXECUTE FUNCTION "fnSetUpdatedAt"();

CREATE TRIGGER "trgFormationsUpdatedAt"
    BEFORE UPDATE ON "formations"
    FOR EACH ROW EXECUTE FUNCTION "fnSetUpdatedAt"();

CREATE TRIGGER "trgInscriptionsUpdatedAt"
    BEFORE UPDATE ON "inscriptions"
    FOR EACH ROW EXECUTE FUNCTION "fnSetUpdatedAt"();

-- ----------------------------------------------------------------
-- 2.2  Numéro de reçu généré automatiquement à l'insertion
-- ----------------------------------------------------------------
CREATE TRIGGER "trgNumeroRecuInscription"
    BEFORE INSERT ON "inscriptions"
    FOR EACH ROW
    WHEN (NEW."numeroRecu" IS NULL)
    EXECUTE FUNCTION "fnGenererNumeroRecu"();

-- ----------------------------------------------------------------
-- 2.3  Calcul automatique des montants de commission à l'inscription
-- ----------------------------------------------------------------
CREATE TRIGGER "trgCalculerCommissionInscription"
    BEFORE INSERT ON "inscriptions"
    FOR EACH ROW EXECUTE FUNCTION "fnCalculerCommission"();

-- ----------------------------------------------------------------
-- 2.4  Gestion des places restantes (insert et update)
-- ----------------------------------------------------------------
CREATE TRIGGER "trgPlacesInscriptionInsert"
    AFTER INSERT ON "inscriptions"
    FOR EACH ROW EXECUTE FUNCTION "fnGererPlaces"();

CREATE TRIGGER "trgPlacesInscriptionUpdate"
    AFTER UPDATE ON "inscriptions"
    FOR EACH ROW EXECUTE FUNCTION "fnGererPlaces"();

-- ----------------------------------------------------------------
-- 2.5  Recalcul de la note moyenne après toute modification d'avis
-- ----------------------------------------------------------------
CREATE TRIGGER "trgMajNoteAvis"
    AFTER INSERT OR UPDATE OR DELETE ON "avis"
    FOR EACH ROW EXECUTE FUNCTION "fnMajNoteFormation"();

-- ----------------------------------------------------------------
-- 2.6  Synchronisation du taux de commission lors d'un changement d'abonnement
-- ----------------------------------------------------------------
CREATE TRIGGER "trgSyncTauxCommission"
    BEFORE UPDATE ON "centres"
    FOR EACH ROW EXECUTE FUNCTION "fnSyncTauxCommission"();
