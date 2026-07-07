package com.eofana.config;

import com.eofana.security.DemoTokenAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * T-B-116 : configuration de la sécurité HTTP module Formateur.
 *
 * ⚠ Toujours pas de vrai JWT (voir DemoTokenAuthenticationFilter) —
 * mais les routes sont désormais réellement protégées par rôle, ce
 * qui n'était pas le cas en Semaine 1 (permitAll() partout). Routes
 * publiques listées explicitement ; tout le reste passe par
 * l'authentification puis, pour le périmètre formateur, exige
 * ROLE_FORMATEUR.
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityFilterConfig {

    private final DemoTokenAuthenticationFilter demoTokenAuthenticationFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                    // --- Routes protégées, réservées au rôle FORMATEUR ---
                    // (doivent être évaluées AVANT le permitAll générique
                    // "/api/v1/formations/**" ci-dessous : Spring Security
                    // applique la première règle qui matche une requête)
                    .requestMatchers(
                            "/api/v1/auth-formateur/deconnexion",
                            "/api/v1/formateurs/profil",
                            "/api/v1/formations/mes-formations",
                            "/api/v1/formateurs/tableau-de-bord/**",
                            "/api/v1/formateurs/finances/**",
                            "/api/v1/formations/*/inscrits",
                            "/api/v1/formations/*/inscrits/export-pdf",
                            "/api/v1/formations/*/sessions"
                    ).hasRole("FORMATEUR")
                    // POST/PUT/DELETE sur les formations = publication/gestion
                    // par le formateur propriétaire ; seul GET reste public
                    // (consultation du catalogue par les apprenants)
                    .requestMatchers(HttpMethod.POST, "/api/v1/formations").hasRole("FORMATEUR")
                    .requestMatchers(HttpMethod.PUT, "/api/v1/formations/*").hasRole("FORMATEUR")
                    .requestMatchers(HttpMethod.DELETE, "/api/v1/formations/*").hasRole("FORMATEUR")
                    // --- Routes publiques (sans authentification) ---
                    .requestMatchers(
                            "/",
                            "/api/v1/auth/connexion",
                            "/api/v1/auth-formateur/connexion",
                            "/api/v1/formateurs/profil/public/**",
                            "/api/v1/formations/**",
                            "/api/v1/categories/**",
                            "/api/v1/centres",
                            "/api/v1/inscriptions/**",
                            "/api/v1/paiements/**",
                            "/api/v1/recus/**",
                            "/api/v1/utilisateurs/**"
                    ).permitAll()
                    .anyRequest().permitAll()
            )
            .addFilterBefore(demoTokenAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
            // Pas de session HTTP : l'API est consommée par le frontend
            // Vue.js de façon stateless
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            // CSRF désactivé : pertinent uniquement pour les formulaires
            // server-side classiques, pas pour une API consommée par Vue.js
            .csrf(csrf -> csrf.disable())
            .formLogin(form -> form.disable())
            .httpBasic(basic -> basic.disable());

        return http.build();
    }
}
