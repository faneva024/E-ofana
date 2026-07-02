-- Description: Déclaration de tous les types énumérés (ENUM)
CREATE TYPE "roleUtilisateur" AS ENUM ('apprenant', 'formateur', 'moderateur', 'commercial', 'admin');
CREATE TYPE "statutCentre" AS ENUM ('enAttente', 'actif', 'inactif', 'suspendu');
CREATE TYPE "abonnementType" AS ENUM ('basic', 'premium');
CREATE TYPE "operateurMm" AS ENUM ('mvola', 'orange', 'airtel');
CREATE TYPE "statutFormation" AS ENUM ('brouillon', 'enAttente', 'approuve', 'rejete', 'correctionDemandee', 'archive');
CREATE TYPE "statutSession" AS ENUM ('ouvert', 'complet', 'termine', 'annule');
CREATE TYPE "typeInscription" AS ENUM ('inscription', 'reservation');
CREATE TYPE "statutInscription" AS ENUM ('enAttente', 'valide', 'annule', 'rembourse', 'termine');
CREATE TYPE "statutPaiement" AS ENUM ('enAttente', 'confirme', 'echoue');
CREATE TYPE "frequenceReversement" AS ENUM ('hebdomadaire', 'mensuel');