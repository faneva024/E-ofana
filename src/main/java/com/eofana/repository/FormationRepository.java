package com.eofana.repository;

import com.eofana.entity.Formation;
import com.eofana.enums.StatutFormation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * Extension du FormationRepository existant (T-B-003, FANEVA).
 *
 * ⚠️ CE FICHIER REMPLACE le FormationRepository.java existant.
 * Toutes les méthodes d'origine ont été conservées.
 * Les méthodes ajoutées pour le module Formateur (T-B-108, T-B-109)
 * sont clairement marquées ci-dessous.
 */
public interface FormationRepository extends JpaRepository<Formation, Long> {

    // ── Méthodes existantes (T-B-003 — FANEVA, Semaine 1) ───────────────────

    List<Formation> findByStatut(StatutFormation statut);

    @Query("""
            SELECT DISTINCT f FROM Formation f
            JOIN SessionFormation s ON s.formation = f
            WHERE f.statut = :statut
              AND s.dateLimiteInscription >= CURRENT_DATE
            """)
    List<Formation> trouverFormationsActivesAvecSessionOuverte(@Param("statut") StatutFormation statut);

    default List<Formation> trouverFormationsActives() {
        return findByStatut(StatutFormation.approuve);
    }

    default List<Formation> trouverFormationsActivesEtNonExpirees() {
        return trouverFormationsActivesAvecSessionOuverte(StatutFormation.approuve);
    }

    @Query("""
            SELECT f FROM Formation f
            WHERE f.statut = :statut
              AND f.categorie.idCategorie = :idCategorie
            """)
    List<Formation> trouverParCategorie(@Param("idCategorie") Long idCategorie,
                                         @Param("statut") StatutFormation statut);

    default List<Formation> trouverParCategorie(Long idCategorie) {
        return trouverParCategorie(idCategorie, StatutFormation.approuve);
    }

    List<Formation> findByCentre_IdCentre(Long idCentre);

    @Query(value = """
            SELECT * FROM eofana.formations f
            WHERE f."statut" = 'approuve'
              AND to_tsvector('french', f."titre" || ' ' || COALESCE(f."description", ''))
                  @@ to_tsquery('french', :motsCles)
            """, nativeQuery = true)
    List<Formation> rechercherParMotsCles(@Param("motsCles") String motsCles);

    // ── Nouvelles méthodes — Module Formateur (T-B-108, T-B-109 — ROLPH, S2) ─

    /** Formations d'un centre, paginées (pour GET /mes-formations) */
    Page<Formation> findByCentre_IdCentre(Long idCentre, Pageable pageable);

    /** Formations d'un centre filtrées par statut, paginées */
    Page<Formation> findByCentre_IdCentreAndStatut(Long idCentre, StatutFormation statut, Pageable pageable);

    /** Compter les formations d'un centre par statut */
    long countByCentre_IdCentreAndStatut(Long idCentre, StatutFormation statut);
}
