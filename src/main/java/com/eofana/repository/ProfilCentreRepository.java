package com.eofana.repository;

import com.eofana.entity.ProfilCentre;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Dépôt ProfilCentre — table "profilCentre" (V13)
 * Tâche T-B-101 — ROLPH (Semaine 2)
 */
@Repository
public interface ProfilCentreRepository extends JpaRepository<ProfilCentre, Long> {

    /** Profil d'un formateur par son idUser */
    Optional<ProfilCentre> findByFormateur_IdUser(Long idUser);

    /** Vérifier si un formateur a déjà un profil */
    boolean existsByFormateur_IdUser(Long idUser);

    /** Tous les profils approuvés (pour le scheduler des virements) */
    List<ProfilCentre> findByStatutProfil(String statutProfil);
}
