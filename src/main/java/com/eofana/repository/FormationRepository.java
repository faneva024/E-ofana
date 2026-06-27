package com.eofana.repository;

import com.eofana.entity.Formation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface FormationRepository extends JpaRepository<Formation, Long> {

    Page<Formation> findByStatut(String statut, Pageable pageable);

    Page<Formation> findByStatutAndCategorie(String statut, String categorie, Pageable pageable);

    @Query("SELECT DISTINCT f.categorie FROM Formation f WHERE f.statut = 'approuve' AND f.categorie IS NOT NULL")
    List<String> findAllCategories();

    @Query("SELECT f FROM Formation f WHERE f.statut = 'approuve' " +
           "AND (:categorie IS NULL OR f.categorie = :categorie) " +
           "AND (:lieu IS NULL OR LOWER(f.lieu) LIKE LOWER(CONCAT('%', :lieu, '%'))) " +
           "AND (:prixMin IS NULL OR f.prix >= :prixMin) " +
           "AND (:prixMax IS NULL OR f.prix <= :prixMax) " +
           "AND (:motsCles IS NULL OR LOWER(f.titre) LIKE LOWER(CONCAT('%', :motsCles, '%')) " +
           "     OR LOWER(f.description) LIKE LOWER(CONCAT('%', :motsCles, '%')))")
    Page<Formation> rechercherFormations(
            @Param("motsCles") String motsCles,
            @Param("categorie") String categorie,
            @Param("lieu") String lieu,
            @Param("prixMin") BigDecimal prixMin,
            @Param("prixMax") BigDecimal prixMax,
            Pageable pageable);
}
