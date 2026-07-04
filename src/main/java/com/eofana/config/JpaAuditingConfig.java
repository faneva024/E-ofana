package com.eofana.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * Active le module Auditing de Spring Data JPA.
 *
 * Sans cette configuration, les annotations @CreatedDate et
 * @LastModifiedDate posées sur Utilisateur.createdAt / updatedAt
 * ne seraient jamais renseignées : elles resteraient à null.
 *
 * @EnableJpaAuditing branche AuditingEntityListener (déclaré sur
 * l'entité via @EntityListeners) au cycle de vie JPA, pour qu'il
 * remplisse ces deux champs automatiquement à l'insertion et à
 * la mise à jour de chaque entité auditée.
 */
@Configuration
@EnableJpaAuditing
public class JpaAuditingConfig {
}