package com.eofana.depot;

import com.eofana.entite.Inscription;
import com.eofana.entite.SessionFormation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

/**
 * Dépôt Inscription — table "inscriptions" de eofanaDb v3.0
 */
@Repository
public interface InscriptionDepot extends JpaRepository<Inscription, Long> {

    /**
     * Récupérer toutes les inscriptions d'un apprenant, triées par date décroissante.
     * Utilisé par GET /api/v1/inscriptions/mes-inscriptions
     */
    @Query("""
        SELECT i FROM Inscription i
        JOIN FETCH i.session s
        JOIN FETCH s.formation f
        JOIN FETCH f.centre c
        WHERE i.utilisateur.idUser = :idUser
        ORDER BY i.createdAt DESC
    """)
    List<Inscription> trouverInscriptionsApprenant(@Param("idUser") Long idUser);

    /**
     * Récupérer tous les inscrits d'une session (pour le formateur).
     */
    @Query("""
        SELECT i FROM Inscription i
        JOIN FETCH i.utilisateur u
        WHERE i.session.idSession = :idSession
        ORDER BY i.createdAt DESC
    """)
    List<Inscription> trouverInscriptionsSession(@Param("idSession") Long idSession);

    /**
     * Vérifier si un utilisateur est déjà inscrit à une session (évite les doublons).
     * Tient compte de la contrainte UNIQUE (idUser, idSession) de la BDD v3.0.
     */
    boolean existsByUtilisateur_IdUserAndSession_IdSessionAndStatutNot(
        Long idUser,
        Long idSession,
        Inscription.StatutInscription statut
    );

    /**
     * Nombre d'inscriptions validées pour une session (contrôle de capacité).
     */
    long countBySession_IdSessionAndStatut(Long idSession, Inscription.StatutInscription statut);
}
