package com.eofana.repository;

import com.eofana.entity.Centre;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Dépôt Spring Data JPA pour l'entité Centre.
 * Minimal pour ce sprint — sera enrichi quand le module Formateur
 * (gestion de profil centre, etc.) sera développé.
 */
public interface CentreRepository extends JpaRepository<Centre, Long> {

    /**
     * Retrouve le centre d'un utilisateur (relation OneToOne).
     * Utile pour aller de "l'utilisateur connecté" à "son centre"
     * une fois l'authentification en place.
     */
    Optional<Centre> findByUtilisateur_IdUser(Long idUser);
}
