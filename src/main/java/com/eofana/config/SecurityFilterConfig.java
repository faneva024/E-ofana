package com.eofana.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Configuration TEMPORAIRE de la sécurité HTTP.
 *
 * Dès que spring-boot-starter-security est sur le classpath, Spring
 * Security protège par défaut TOUTES les routes et affiche une page
 * de login auto-générée. Pour ce sprint (T-B-002 : entité + repository
 * Utilisateur, pas encore d'authentification HTTP), on désactive cette
 * protection automatique afin de pouvoir tester les futurs contrôleurs
 * du module Apprenant sans être bloqué par un login.
 *
 * ⚠ À REMPLACER dans un sprint ultérieur (authentification réelle :
 * JWT ou session) par une configuration qui protège effectivement
 * les routes selon le rôle de l'utilisateur connecté.
 */
@Configuration
@EnableWebSecurity
public class SecurityFilterConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // Toutes les routes sont ouvertes pour l'instant (sprint en cours)
            .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
            // Pas de session HTTP : l'API sera consommée par le frontend
            // Vue.js de façon stateless (préparation pour JWT plus tard)
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            // CSRF désactivé : pertinent uniquement pour les formulaires
            // server-side classiques, pas pour une API consommée par Vue.js
            .csrf(csrf -> csrf.disable())
            // Désactive aussi le formulaire de login HTML auto-généré
            .formLogin(form -> form.disable())
            .httpBasic(basic -> basic.disable());

        return http.build();
    }
}
