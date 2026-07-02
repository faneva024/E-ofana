-- Description : Types ENUM complémentaires absents de V2
--               (remboursements, virements, notifications, modifications)
SET search_path TO eofana;

-- Statut d'un remboursement
CREATE TYPE "statutRemboursement" AS ENUM (
    'enAttente',
    'traite',
    'rejete'
);

-- Statut d'un reversement / virement vers un centre
CREATE TYPE "statutVirement" AS ENUM (
    'planifie',
    'effectue',
    'echoue'
);

-- Types de notifications envoyées aux utilisateurs
CREATE TYPE "typeNotification" AS ENUM (
    'inscription',
    'rappelJ3',
    'rappelJ1',
    'annulation',
    'remboursement',
    'virement',
    'rapportMensuel',
    'systeme'
);

-- Canal d'envoi d'une notification
CREATE TYPE "canalNotification" AS ENUM (
    'email',
    'sms',
    'lesDeux'
);

-- Statut d'envoi d'un email
CREATE TYPE "statutEmail" AS ENUM (
    'enAttente',
    'envoye',
    'erreur'
);

-- Statut d'envoi d'un SMS
CREATE TYPE "statutSms" AS ENUM (
    'enAttente',
    'envoye',
    'erreur'
);

-- Statut d'une demande de modification de profil centre
CREATE TYPE "statutModification" AS ENUM (
    'enAttente',
    'approuve',
    'rejete'
);
