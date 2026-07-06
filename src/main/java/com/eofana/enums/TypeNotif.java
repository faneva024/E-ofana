package com.eofana.enums;

/**
 * Types de notification liés au workflow de modération d'une formation.
 *
 * Placeholder en attendant la table "notifications" (tsy mbola vo migre) — sert uniquement à typer l'appel dans
 * CourseValidationService.notifyStatusChange() pour l'instant.
 */
public enum TypeNotif {
    formationApprouvee,
    formationRejetee,
    formationCorrectionDemandee
}