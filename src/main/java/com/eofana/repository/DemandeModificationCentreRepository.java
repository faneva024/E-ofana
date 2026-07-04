package com.eofana.repository;

import com.eofana.entity.DemandeModificationCentre;
import com.eofana.enums.StatutModification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;


public interface DemandeModificationCentreRepository
        extends JpaRepository<DemandeModificationCentre, Long> {
    /**
     * Retrouve toutes les demandes d'un centre filtrées par statut.
     */
    List<DemandeModificationCentre> findByCentre_IdCentreAndStatutOrderByCreatedAtDesc(
            Long idCentre,
            StatutModification statut
    );

    /**
     * Retrouve toutes les demandes d'un centre, tous statuts confondus.
     */
    List<DemandeModificationCentre> findByCentre_IdCentreOrderByCreatedAtDesc(Long idCentre);

    /**
     * Vérifie si un centre a au moins une demande encore en attente.
     */
    boolean existsByCentre_IdCentreAndStatut(Long idCentre, StatutModification statut);

    /**
     * Retrouve la dernière demande enAttente d'un centre (une seule active à la fois).
     */
    Optional<DemandeModificationCentre> findFirstByCentre_IdCentreAndStatutOrderByCreatedAtDesc(
            Long idCentre,
            StatutModification statut
    );

    /**
     * Retrouve toutes les demandes en attente, tous centres confondus.
     */
    List<DemandeModificationCentre> findByStatutOrderByCreatedAtAsc(StatutModification statut);

    /**
     * Compte les demandes en attente sur toute la plateforme.
     */
    long countByStatut(StatutModification statut);

    /**
     * Retrouve toutes les demandes traitées par un modérateur donné.
     */
    @Query("""
        SELECT d FROM DemandeModificationCentre d
        WHERE d.traitePar.idUser = :idModerateur
        ORDER BY d.traiteAt DESC
        """)
    List<DemandeModificationCentre> findByModerateur(@Param("idModerateur") Long idModerateur);
}
