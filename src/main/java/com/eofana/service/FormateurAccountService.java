package com.eofana.service;

import com.eofana.entity.Abonnement;
import com.eofana.entity.Centre;
import com.eofana.entity.Utilisateur;
import com.eofana.enums.AbonnementType;
import com.eofana.enums.OperateurMm;
import com.eofana.enums.RoleUtilisateur;
import com.eofana.enums.StatutCentre;
import com.eofana.enums.TypeNotification;
import com.eofana.repository.AbonnementRepository;
import com.eofana.repository.CentreRepository;
import com.eofana.repository.UtilisateurRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDate;
import java.util.Map;

/**
 * T-B-213 : création atomique d'un compte formateur par l'admin
 * (ex-fonction "commercial" de l'ancien rôle Modérateur/Commercial,
 * cf. §3.2 du cahier des charges).
 *
 * Une seule opération transactionnelle crée : le compte Utilisateur
 * (role = FORMATEUR, mot de passe temporaire généré et haché), le
 * profil Centre associé, et le premier enregistrement d'historique
 * dans "abonnements" — les trois ensemble ou aucun (critère
 * d'acceptation de T-B-207).
 */
@Service
@RequiredArgsConstructor
public class FormateurAccountService {

    /** Sans 0/O/1/l/I : évite les erreurs de recopie du mot de passe temporaire par le formateur. */
    private static final String CARACTERES_MOT_DE_PASSE =
            "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz23456789";
    private static final int LONGUEUR_MOT_DE_PASSE = 12;
    private static final SecureRandom RANDOM = new SecureRandom();

    private final UtilisateurRepository utilisateurRepository;
    private final CentreRepository centreRepository;
    private final AbonnementRepository abonnementRepository;
    private final UtilisateurService utilisateurService;
    private final NotificationService notificationService;

    @PersistenceContext
    private EntityManager entityManager;

    /**
     * Valide la complétude du formulaire de création, conformément
     * au tableau des champs obligatoires du §3.2 du cahier des
     * charges. Le compte Mobile Money et le site web restent
     * optionnels (un centre peut les compléter plus tard depuis son
     * profil, cf. T-B-107).
     *
     * @return null si tout est valide, sinon le premier message d'erreur rencontré
     */
    public String validateRequiredFields(CreateFormateurDto dto) {
        if (isBlank(dto.nomResponsable())) return "Le nom du responsable est obligatoire";
        if (isBlank(dto.prenomResponsable())) return "Le prénom du responsable est obligatoire";
        if (isBlank(dto.emailConnexion())) return "L'email de connexion est obligatoire";
        if (isBlank(dto.nomCentre())) return "Le nom du centre est obligatoire";
        if (isBlank(dto.ville())) return "La ville est obligatoire";
        if (dto.fourchettePrixMin() == null || dto.fourchettePrixMax() == null)
            return "La fourchette de prix est obligatoire";
        if (dto.fourchettePrixMin() < 0 || dto.fourchettePrixMax() < dto.fourchettePrixMin())
            return "La fourchette de prix est invalide";
        if (isBlank(dto.telephoneCentre()) && isBlank(dto.emailCentre()))
            return "Au moins un contact (téléphone ou email) du centre est obligatoire";
        if (dto.abonnement() == null) return "Le type d'abonnement est obligatoire";
        if (utilisateurRepository.existsByEmail(dto.emailConnexion()))
            return "Cet email est déjà utilisé par un autre compte";
        return null;
    }

    /**
     * Création atomique : Utilisateur (FORMATEUR) + Centre + premier
     * Abonnement. Un échec de l'envoi d'email (sendCredentialsByEmail)
     * n'annule JAMAIS cette transaction : seule une erreur de
     * validation ou d'écriture en base provoque un rollback,
     * conformément aux critères d'acceptation de T-B-207.
     */
    @Transactional
    public Utilisateur createFormateurAccount(CreateFormateurDto dto) {
        String motDePasseTemporaire = generateTemporaryPassword();

        Utilisateur formateur = Utilisateur.builder()
                .nom(dto.nomResponsable())
                .prenom(dto.prenomResponsable())
                .email(dto.emailConnexion())
                .motDePasse(utilisateurService.hacherMotDePasse(motDePasseTemporaire))
                .role(RoleUtilisateur.formateur)
                .telephone(dto.telephoneResponsable())
                .estActif(true)
                .build();
        formateur = utilisateurRepository.save(formateur);

        Centre centre = Centre.builder()
                .utilisateur(formateur)
                .nom(dto.nomCentre())
                .logo(dto.logo())
                .description(dto.description())
                .services(dto.services())
                .ville(dto.ville())
                .adresse(dto.adresse())
                .fourchettePrixMin(dto.fourchettePrixMin())
                .fourchettePrixMax(dto.fourchettePrixMax())
                .telephone(dto.telephoneCentre())
                .email(dto.emailCentre())
                .siteWeb(dto.siteWeb())
                .abonnement(dto.abonnement())
                .mobileMoneyOperateur(dto.mobileMoneyOperateur())
                .mobileMoneyNumero(dto.mobileMoneyNumero())
                // Créé directement par l'admin (pas de demande à
                // modérer, contrairement à l'auto-inscription) : le
                // centre est actif dès sa création.
                .statut(StatutCentre.actif)
                .build();

        // saveAndFlush + refresh : "abonnement" déclenche le trigger
        // PostgreSQL trgSyncTauxCommission à l'INSERT (V8), qui recalcule
        // tauxCommission côté base. On relit l'entité après le flush
        // plutôt que de recalculer 7.00/15.00 côté Java, pour ne jamais
        // dupliquer cette règle métier (cf. note de conception de T-B-208).
        centre = centreRepository.saveAndFlush(centre);
        entityManager.refresh(centre);

        Abonnement premierAbonnement = Abonnement.builder()
                .centre(centre)
                .type(dto.abonnement())
                .tauxCommission(centre.getTauxCommission())
                .dateDebut(LocalDate.now())
                .build();
        abonnementRepository.save(premierAbonnement);

        sendCredentialsByEmail(formateur, motDePasseTemporaire);

        return formateur;
    }

    /**
     * Génère un mot de passe temporaire aléatoire et cryptographiquement
     * sûr (SecureRandom, pas Random) — jamais stocké en clair : seul
     * son hash BCrypt (via UtilisateurService#hacherMotDePasse) est
     * persisté, et la valeur en clair n'existe que le temps de cette
     * transaction pour être transmise par email.
     */
    public String generateTemporaryPassword() {
        StringBuilder motDePasse = new StringBuilder(LONGUEUR_MOT_DE_PASSE);
        for (int i = 0; i < LONGUEUR_MOT_DE_PASSE; i++) {
            motDePasse.append(CARACTERES_MOT_DE_PASSE.charAt(RANDOM.nextInt(CARACTERES_MOT_DE_PASSE.length())));
        }
        return motDePasse.toString();
    }

    /**
     * Envoie les identifiants de connexion au nouveau formateur via
     * NotificationService (table "notifications", typeNotif =
     * creationCompte, V9) — remplace l'appel RestTemplate direct
     * utilisé avant que cette table n'existe (cf. T-B-201) : la
     * notification est maintenant tracée en base avant tentative
     * d'envoi, et non plus seulement loggée en cas d'échec.
     *
     * Un échec d'envoi n'est jamais propagé (cf. NotificationService) :
     * conformément aux critères d'acceptation de T-B-207, il ne doit
     * jamais faire échouer la transaction de création du compte.
     */
    public void sendCredentialsByEmail(Utilisateur formateur, String motDePasseTemporaire) {
        String titre = "Bienvenue sur E-OFANA — vos identifiants de connexion";
        String message = "Un compte formateur a été créé pour vous par l'administration E-OFANA. "
                + "Identifiant : " + formateur.getEmail() + ". Mot de passe temporaire à changer à la première connexion.";

        notificationService.creerEtEnvoyer(formateur, TypeNotification.creationCompte, titre, message, Map.of(
                "destinataire", formateur.getEmail(),
                "typeNotif", "CREATION_COMPTE",
                "nom", formateur.getNom(),
                "prenom", formateur.getPrenom(),
                "emailConnexion", formateur.getEmail(),
                "motDePasseTemporaire", motDePasseTemporaire
        ));
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    /**
     * DTO de création complète d'un compte formateur (T-B-207/T-B-213,
     * §3.2 du cahier des charges) : identifiants de connexion du
     * responsable + profil complet du centre + abonnement initial.
     */
    public record CreateFormateurDto(
            String nomResponsable,
            String prenomResponsable,
            String emailConnexion,
            String telephoneResponsable,
            String nomCentre,
            String logo,
            String description,
            String services,
            String ville,
            String adresse,
            Integer fourchettePrixMin,
            Integer fourchettePrixMax,
            String telephoneCentre,
            String emailCentre,
            String siteWeb,
            AbonnementType abonnement,
            OperateurMm mobileMoneyOperateur,
            String mobileMoneyNumero
    ) {
    }
}
