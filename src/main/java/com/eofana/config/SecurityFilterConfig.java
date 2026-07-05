package com.eofana.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

/**
 * SecurityFilterConfig — T-B-116 (mise à jour Semaine 2)
 * Branche : Back → part-Rolph
 *
 * ⚠️ CE FICHIER REMPLACE SecurityFilterConfig.java existant.
 *
 * Modifications apportées par ROLPH (Semaine 2) :
 *   1. @EnableScheduling ajouté → active ServiceVirementAutomatique (T-B-113)
 *   2. Les routes restent ouvertes (anyRequest().permitAll()) car le JWT
 *      complet (T-B-006, SANDA) n'est pas encore implémenté dans ce sprint.
 *      La protection sera activée une fois FiltreAuthentificationJwt finalisé.
 *
 * Aucune autre modification — structure originale conservée.
 */
@Configuration
@EnableWebSecurity
@EnableScheduling
public class SecurityFilterConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // Toutes les routes ouvertes (temporaire — sera sécurisé avec JWT, T-B-006 SANDA)
            .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
            // Stateless — pas de session HTTP (préparation JWT)
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            // CSRF désactivé (API REST consommée par Vue.js)
            .csrf(csrf -> csrf.disable())
            .formLogin(form -> form.disable())
            .httpBasic(basic -> basic.disable());

        return http.build();
    }
}
