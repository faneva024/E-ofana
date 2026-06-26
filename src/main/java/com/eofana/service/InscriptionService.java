package com.eofana.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.eofana.entite.Inscription;
import com.eofana.entite.StatutInscription;
import com.eofana.entite.TypeInscription;
import com.eofana.repository.InscriptionRepository;

@Service
@RequiredArgsConstructor

public class InscriptionService {
    private final InscriptionRepository inscriptionRepository;

    public Inscription creeInscription(Inscription inscription){
        inscription.setTypeInscription(TypeInscription.inscription);
        inscription.setStatut(StatutInscription.enAttente);

        return inscriptionRepository.save(inscription);
    }

    public Inscription creerReservation(Inscription inscription) {

        inscription.setTypeInscription(TypeInscription.reservation);
        inscription.setStatut(StatutInscription.enAttente);

        return inscriptionRepository.save(inscription);
    }

    public List<Inscription> trouverInscriptionsApprenant(Utilisateur utilisateur) {
        return inscriptionRepository.findByUtilisateur(utilisateur);
    }

    public List<Inscription> trouverInscriptionsFormation(SessionFormation sessionFormation) {
        return inscriptionRepository.findBySessionFormation(sessionFormation);
    }
}