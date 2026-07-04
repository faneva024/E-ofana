package com.eofana.config;

import com.eofana.security.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Configuration de la sécurité HTTP (T-B-016).
 *
 * Remplace la configuration précédente qui ouvrait TOUTES les routes
 * (`anyRequest().permitAll()`) sans aucune authentification, y
 * compris `/api/v1/inscriptions/**` et `/api/v1/apprenants/profil`
 * qui doivent être réservées à l'apprenant connecté.
 *
 * Routes publiques : authentification, catalogue de formations,
 * catégories, profils publics de centre. Tout le reste exige un
 * Bearer Token JWT valide (voir JwtAuthenticationFilter).
 */
@Configuration
@EnableWebSecurity
public class SecurityFilterConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityFilterConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                // Authentification : toujours publique
                .requestMatchers("/api/v1/auth/**").permitAll()
                // Catalogue de formations et catégories : lecture publique
                .requestMatchers(HttpMethod.GET, "/api/v1/formations/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/v1/categories/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/v1/centres/**").permitAll()
                .requestMatchers("/").permitAll()
                // Tout le reste nécessite un token JWT valide
                .anyRequest().authenticated()
            )
            // Pas de session HTTP : API stateless consommée par le
            // frontend Vue.js, l'état d'authentification vit dans le JWT
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            // CSRF désactivé : pertinent uniquement pour les formulaires
            // server-side classiques, pas pour une API stateless
            .csrf(csrf -> csrf.disable())
            .formLogin(form -> form.disable())
            .httpBasic(basic -> basic.disable())
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
