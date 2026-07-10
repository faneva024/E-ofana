package com.eofana.service;

import com.eofana.entity.Notification;
import com.eofana.entity.Utilisateur;
import com.eofana.enums.CanalNotification;
import com.eofana.enums.StatutEnvoi;
import com.eofana.enums.TypeNotification;
import com.eofana.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.time.OffsetDateTime;
import java.util.Map;

/**
 * T-B-201/V9 : point d'entrée unique du module Admin pour toute
 * notification (email/SMS), utilisé par T-B-213 (identifiants
 * formateur), T-B-214 (décisions de modération de formation) et
 * T-B-206 (décisions sur les demandes de modification de profil).
 *
 * Chaque appel : (1) enregistre une ligne dans "notifications" — trace
 * d'audit et point de reprise pour un futur cron de relance — puis
 * (2) tente un envoi immédiat best-effort via le micro-service Node.js
 * (services/service-email), en suivant le pattern déjà utilisé par
 * AutomaticTransferService (module Formateur, T-B-113). Un échec
 * d'envoi met juste à jour statutEmail = erreur ; il ne remonte
 * jamais d'exception à l'appelant (cf. critère d'acceptation de
 * T-B-207 : "log en cas d'échec, pas de blocage de la transaction BD").
 */
@Service
@RequiredArgsConstructor
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    private final NotificationRepository notificationRepository;
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${eofana.service-email.url:http://localhost:3001}")
    private String serviceEmailUrl;

    /**
     * Enregistre puis tente l'envoi immédiat d'une notification.
     * Ne lève jamais d'exception : un échec d'envoi email est
     * uniquement reflété dans notification.statutEmail (= erreur) et
     * loggé, jamais propagé à l'appelant.
     */
    @Transactional
    public Notification creerEtEnvoyer(Utilisateur destinataire, TypeNotification type,
                                        String titre, String message, Map<String, Object> donneesEmail) {
        Notification notification = Notification.builder()
                .destinataire(destinataire)
                .titre(titre)
                .message(message)
                .typeNotif(type)
                .canal(CanalNotification.email)
                .statutEmail(StatutEnvoi.enAttente)
                .build();
        notification = notificationRepository.save(notification);

        if (destinataire.getEmail() == null || destinataire.getEmail().isBlank()) {
            log.warn("Utilisateur {} sans email : notification {} non envoyée", destinataire.getIdUser(), type);
            notification.setStatutEmail(StatutEnvoi.erreur);
            return notificationRepository.save(notification);
        }

        try {
            restTemplate.postForEntity(serviceEmailUrl + "/api/notifications/envoyer", donneesEmail, Void.class);
            notification.setStatutEmail(StatutEnvoi.envoye);
            notification.setSentAt(OffsetDateTime.now());
        } catch (RestClientException e) {
            log.error("Échec de l'envoi de la notification {} (type={}) à l'utilisateur {} ({})",
                    notification.getIdNotification(), type, destinataire.getIdUser(), destinataire.getEmail(), e);
            notification.setStatutEmail(StatutEnvoi.erreur);
        }

        return notificationRepository.save(notification);
    }
}
