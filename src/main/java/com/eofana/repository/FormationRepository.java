package com.eofana.repository;

import com.eofana.entity.Formation;
import com.eofana.enums.StatutFormation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * Dépôt Spring Data JPA pour l'entité Formation.
 *
 * Nom volontairement "FormationRepository" plutôt que "FormationDepot"
 * mentionné dans le ticket T-B-003 — convention Spring idiomatique,
 * cohérente avec UtilisateurRepository créé au sprint précédent.
 */
public interface FormationRepository extends JpaRepository<Formation, Long> {

    /**
     * Formations actives proposées aux apprenants.
     *
     * Note de conception : le ticket demandait "approuvées et non
     * expirées", mais la notion d'expiration appartient à
     * SessionFormation.dateLimiteInscription (pas à Formation elle-
     * même, qui n'a pas de date de fin). Cette méthode renvoie donc
     * pour l'instant toutes les formations au statut "approuve" ;
     * la version qui ne renvoie que celles ayant au moins une
     * session ENCORE ouverte est trouverFormationsActivesAvecSessionOuverte()
     * ci-dessous, qui s'appuie sur un vrai JOIN.
     */
    List<Formation> findByStatut(StatutFormation statut);

    /**
     * Variante plus fidèle à l'intention métier du ticket : formations
     * approuvées qui ont AU MOINS une session encore ouverte à
     * l'inscription (date limite non dépassée). Exploite l'index
     * idxFormationsStatut et idxSessionsDateDebut.
     *
     * Le statut est passé en paramètre (:statut) plutôt qu'écrit en
     * dur dans la requête JPQL ('approuve') : un littéral de chaîne
     * dans le JPQL ne passe pas de façon fiable par notre
     * StatutFormationConverter personnalisé (qui mappe vers le type
     * ENUM natif PostgreSQL) — passer l'enum Java en paramètre est
     * la façon correcte de garantir que le converter s'applique.
     */
    @Query("""
            SELECT DISTINCT f FROM Formation f
            JOIN SessionFormation s ON s.formation = f
            WHERE f.statut = :statut
              AND s.dateLimiteInscription >= CURRENT_DATE
            """)
    List<Formation> trouverFormationsActivesAvecSessionOuverte(@Param("statut") StatutFormation statut);

    /**
     * Formations actives (statut = approuve), sans condition sur les
     * sessions. C'est la méthode demandée littéralement par le ticket
     * T-B-003 ("formations approuvées et non expirées"), simplifiée
     * pour ce sprint à "approuvées" — voir la note de conception
     * ci-dessus pour la version avec sessions.
     *
     * Méthode "default" : pas une vraie requête générée par Spring
     * Data, juste un raccourci pratique au-dessus de findByStatut().
     */
    default List<Formation> trouverFormationsActives() {
        return findByStatut(StatutFormation.approuve);
    }

    /**
     * Équivalent à trouverFormationsActives() mais qui exige en plus
     * une session encore ouverte — recommandé pour la recherche
     * apprenant réelle (sprint suivant, une fois SessionFormation
     * peuplée de vraies données).
     */
    default List<Formation> trouverFormationsActivesEtNonExpirees() {
        return trouverFormationsActivesAvecSessionOuverte(StatutFormation.approuve);
    }

    /**
     * Filtre les formations actives par catégorie.
     * Exploite l'index idxFormationsCategorie.
     *
     * Même remarque que ci-dessus : le statut "approuve" est codé en
     * dur ici via l'enum Java StatutFormation.approuve passé comme
     * paramètre par défaut (et non comme littéral JPQL), pour que
     * le converter personnalisé s'applique correctement.
     *
     * @param idCategorie l'identifiant de la catégorie recherchée
     */
    @Query("""
            SELECT f FROM Formation f
            WHERE f.statut = :statut
              AND f.categorie.idCategorie = :idCategorie
            """)
    List<Formation> trouverParCategorie(@Param("idCategorie") Long idCategorie,
                                         @Param("statut") StatutFormation statut);

    /**
     * Surcharge pratique : filtre par catégorie en se limitant aux
     * formations approuvées (cas d'usage réel pour la recherche
     * apprenant). C'est cette version que le contrôleur appellera.
     */
    default List<Formation> trouverParCategorie(Long idCategorie) {
        return trouverParCategorie(idCategorie, StatutFormation.approuve);
    }

    /**
     * Toutes les formations d'un centre donné, peu importe leur statut.
     * Utile pour le futur tableau de bord formateur (lister brouillons,
     * en attente, approuvées, rejetées).
     */
    List<Formation> findByCentre_IdCentre(Long idCentre);

    /**
     * Recherche plein texte sur titre + description, en français.
     * Exploite l'index GIN idxFormationsFts créé en V7.
     * Ne renvoie que les formations approuvées (recherche apprenant).
     *
     * @param motsCles texte libre saisi par l'apprenant
     */
    @Query(value = """
            SELECT * FROM eofana.formations f
            WHERE f."statut" = 'approuve'
              AND to_tsvector('french', f."titre" || ' ' || COALESCE(f."description", ''))
                  @@ to_tsquery('french', :motsCles)
            """, nativeQuery = true)
    List<Formation> rechercherParMotsCles(@Param("motsCles") String motsCles);
}
