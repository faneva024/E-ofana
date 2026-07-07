package com.eofana.repository;

import com.eofana.entity.Categorie;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Dépôt Spring Data JPA pour l'entité Categorie.
 * Minimal pour ce sprint — référentiel simple, peu de logique métier.
 */
public interface CategorieRepository extends JpaRepository<Categorie, Long> {

    Optional<Categorie> findByNom(String nom);
}
