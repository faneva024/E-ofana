package com.eofana.repository;

import com.eofana.entite.Inscription;
import com.eofana.entite.SessionFormation;
import com.eofana.entite.StatutInscription;
import com.eofana.entite.Utilisateur;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InscriptionRepository extends JpaRepository<Inscription, Long> {

    List<Inscription> findByUtilisateur(Utilisateur utilisateur);

    List<Inscription> findBySessionFormation(SessionFormation sessionFormation);

    List<Inscription> findByStatut(StatutInscription statut);

    boolean existsByUtilisateurAndSessionFormation(
            Utilisateur utilisateur,
            SessionFormation sessionFormation
    );
}