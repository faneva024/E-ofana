package com.eofana.repository;

import com.eofana.entity.Inscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface InscriptionRepository extends JpaRepository<Inscription, Long> {
    List<Inscription> findByUtilisateurIdUser(Long idUtilisateur);
    List<Inscription> findByFormationIdFormation(Long idFormation);
    boolean existsByUtilisateurIdUserAndFormationIdFormation(Long idUtilisateur, Long idFormation);
    Optional<Inscription> findByIdInscriptionAndUtilisateurIdUser(Long idInscription, Long idUtilisateur);
}
