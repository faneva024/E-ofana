package com.eofana.service;

import com.eofana.entity.Utilisateur;
import com.eofana.enums.TypeNotif;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Stub temporaire : le vrai module notifications (table + entité +
 * envoi email/push) n'existe pas encore dans ce projet. En attendant,
 * on se contente de logger, pour que CourseValidationService (T-B-114)
 * ne soit pas bloqué par une dépendance non construite.
 *
 * À remplacer par une vraie implémentation (persistance en base +
 * envoi) dès que le module notifications sera ticketé.
 */
@Service
@Slf4j
public class NotificationService {

    public void envoyer(Utilisateur destinataire, TypeNotif type, String message) {
        log.info("[NOTIFICATION - {}] destinataire={} ({}) — {}",
                type, destinataire.getIdUser(), destinataire.getEmail(), message);
    }
}