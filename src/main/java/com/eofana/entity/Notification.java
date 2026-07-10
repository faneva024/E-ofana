package com.eofana.entity;

import com.eofana.enums.CanalNotification;
import com.eofana.enums.StatutEnvoi;
import com.eofana.enums.TypeNotification;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;

/**
 * Entité JPA mappée sur la table "notifications" (V9, module Admin).
 *
 * @see com.eofana.service.NotificationService
 */
@Entity
@Table(name = "\"notifications\"")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "\"idNotification\"")
    private Long idNotification;

    @NotNull(message = "La notification doit être rattachée à un destinataire")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "\"idUser\"", nullable = false)
    private Utilisateur destinataire;

    @Column(name = "\"titre\"", length = 200)
    private String titre;

    @Column(name = "\"message\"", columnDefinition = "TEXT")
    private String message;

    @NotNull
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "\"typeNotif\"", nullable = false, columnDefinition = "\"typeNotification\"")
    @Builder.Default
    private TypeNotification typeNotif = TypeNotification.systeme;

    @NotNull
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "\"canal\"", nullable = false, columnDefinition = "\"canalNotification\"")
    @Builder.Default
    private CanalNotification canal = CanalNotification.email;

    @NotNull
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "\"statutEmail\"", nullable = false, columnDefinition = "\"statutEnvoi\"")
    @Builder.Default
    private StatutEnvoi statutEmail = StatutEnvoi.enAttente;

    @NotNull
    @Column(name = "\"estLu\"", nullable = false)
    @Builder.Default
    private Boolean estLu = false;

    @Column(name = "\"sentAt\"")
    private OffsetDateTime sentAt;

    @Column(name = "\"createdAt\"", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @PrePersist
    public void prePersist() {
        if (this.createdAt == null) {
            this.createdAt = OffsetDateTime.now();
        }
    }
}
