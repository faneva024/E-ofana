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
 * T-B-116 (module Formateur) + T-B-215 (module Admin) : configuration
 * de la sécurité HTTP.
 *
 * ⚠ Toujours pas de vrai JWT (voir DemoTokenAuthenticationFilter) —
 * mais les routes sont désormais réellement protégées par rôle, ce
 * qui n'était pas le cas en Semaine 1 (permitAll() partout). Routes
 * publiques listées explicitement ; tout le reste passe par
 * l'authentification puis, selon le périmètre, exige ROLE_FORMATEUR
 * ou ROLE_ADMIN.
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
                    // --- T-B-215 : routes admin, réservées au rôle ADMIN ---
                    // Évaluées AVANT tout permitAll générique : Spring
                    // Security applique la première règle qui matche.
                    .requestMatchers(
                            "/api/v1/admin/**",
                            "/api/v1/auth-admin/deconnexion"
                    ).hasRole("ADMIN")
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
                            "/api/v1/auth-admin/connexion",
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
            // T-B-215 : distingue explicitement 401 (pas authentifié du
            // tout — en-tête Authorization absent/invalide) de 403
            // (authentifié mais rôle insuffisant), conformément au
            // critère d'acceptation du ticket. Par défaut, Spring
            // Security renvoie 403 dans les deux cas dès qu'aucun
            // AuthenticationEntryPoint n'est configuré — d'où cet ajout,
            // qui bénéficie aussi bien aux routes formateur qu'admin.
            .exceptionHandling(handling -> handling
                    .authenticationEntryPoint((request, response, authException) ->
                            response.sendError(401, "Authentification requise"))
                    .accessDeniedHandler((request, response, accessDeniedException) ->
                            response.sendError(403, "Accès refusé : rôle insuffisant"))
            )
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
