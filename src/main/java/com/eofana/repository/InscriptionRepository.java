package com.eofana.repository;

import com.eofana.entity.Inscription;
import com.eofana.enums.StatutInscription;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Dépôt Spring Data JPA pour l'entité Inscription.
 */
public interface InscriptionRepository extends JpaRepository<Inscription, Long> {

    /** Historique complet d'un apprenant, du plus récent au plus ancien. */
    List<Inscription> findByUtilisateur_IdUserOrderByCreatedAtDesc(Long idUser);

    /** Un apprenant ne peut s'inscrire qu'une fois à la même session. */
    boolean existsByUtilisateur_IdUserAndSession_IdSession(Long idUser, Long idSession);

    Optional<Inscription> findByUtilisateur_IdUserAndSession_IdSession(Long idUser, Long idSession);

    /** Toutes les inscriptions (tous statuts) d'une session donnée — utile côté formateur. */
    List<Inscription> findBySession_IdSession(Long idSession);

    List<Inscription> findBySession_IdSessionAndStatut(Long idSession, StatutInscription statut);
}
