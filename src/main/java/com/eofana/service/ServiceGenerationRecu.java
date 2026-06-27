package com.eofana.service;

import com.eofana.depot.InscriptionDepot;
import com.eofana.entite.Inscription;
import org.springframework.stereotype.Service;

/**
 * ServiceGenerationRecu — T-B-011 (FENOSOA)
 * Stub déclaré ici pour permettre la compilation de ControleurInscription.
 * L'implémentation complète (génération PDF iText/OpenPDF) est assignée à FENOSOA.
 *
 * Rôle : générer le reçu PDF d'une inscription à partir de son idInscription.
 * Référence BDD v3.0 : lit les champs de la table "inscriptions" +
 * les tables liées (utilisateurs, sessionsFormation, formations, centres).
 */
@Service
public class ServiceGenerationRecu {

    private final InscriptionDepot inscriptionDepot;

    public ServiceGenerationRecu(InscriptionDepot inscriptionDepot) {
        this.inscriptionDepot = inscriptionDepot;
    }

    /**
     * Générer le PDF de reçu pour une inscription.
     * @param idInscription identifiant de l'inscription (table "inscriptions")
     * @return tableau d'octets du PDF généré
     */
    public byte[] genererRecu(Long idInscription) {
        Inscription inscription = inscriptionDepot.findById(idInscription)
            .orElseThrow(() -> new RuntimeException("Inscription introuvable : " + idInscription));

        // Implémentation complète à faire par FENOSOA (T-B-011)
        // Contenu du reçu selon BDD v3.0 :
        //   - numeroRecu           : inscription.getNumeroRecu()
        //   - apprenant            : inscription.getUtilisateur().getNomComplet()
        //   - formation            : inscription.getSession().getFormation().getTitre()
        //   - session              : inscription.getSession().getDateDebut()
        //   - lieu                 : inscription.getSession().getFormation().getLieu()
        //   - montantPaye          : inscription.getMontantPaye()
        //   - remise               : inscription.getRemise()
        //   - commission           : inscription.getCommission()
        //   - operateur            : inscription.getOperateur()
        //   - transactionId        : inscription.getTransactionId()
        //   - dateInscription      : inscription.getCreatedAt()

        return new byte[0]; // À remplacer par la génération PDF réelle
    }
}
