package com.eofana.repository;

import com.eofana.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Dépôt Spring Data JPA pour l'entité Notification (V9, module Admin).
 */
public interface NotificationRepository extends JpaRepository<Notification, Long> {
}
