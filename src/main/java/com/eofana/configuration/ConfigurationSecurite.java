package com.eofana.configuration;

import com.eofana.securite.FiltreAuthentificationJwt;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * ConfigurationSecurite — T-B-013
 * Branche : back → part-Rolph
 *
 * Définit toutes les règles d'accès de l'API E-OFANA :
 *   - Routes publiques  : inscription, connexion, consultation formations
 *   - Routes protégées  : tout ce qui nécessite un token JWT valide
 *
 * Référence BDD v3.0 :
 *   - Authentification basée sur la table "utilisateurs", colonne "email"
 *   - Rôles : apprenant | formateur | moderateur | commercial | admin
 *
 * API stateless — aucune session HTTP côté serveur (JWT uniquement).
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class ConfigurationSecurite {

    private final FiltreAuthentificationJwt filtreJwt;

    public ConfigurationSecurite(FiltreAuthentificationJwt filtreJwt) {
        this.filtreJwt = filtreJwt;
    }

    // ── Encodeur de mots de passe BCrypt (10 rounds) ─────────────────────────
    @Bean
    public PasswordEncoder encodeurMotDePasse() {
        return new BCryptPasswordEncoder(10);
    }

    // ── AuthenticationManager exposé comme Bean ───────────────────────────────
    // Injecté dans ControleurAuthentification pour valider email + motDePasse
    @Bean
    public AuthenticationManager gestionnaireAuthentification(
            AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    // ── Chaîne de filtres de sécurité principale ──────────────────────────────
    @Bean
    public SecurityFilterChain chaineFiltres(HttpSecurity http) throws Exception {

        http
            // API REST stateless → pas besoin de CSRF
            .csrf(csrf -> csrf.disable())

            // CORS pour Vue.js (port différent en développement)
            .cors(cors -> cors.configurationSource(sourceCors()))

            // Aucune session côté serveur — JWT uniquement
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )

            // ── Règles d'autorisation par route ──────────────────────────────
            .authorizeHttpRequests(auth -> auth

                // ┌──────────────────────────────────────────────────────────┐
                // │  ROUTES PUBLIQUES — sans authentification                 │
                // └──────────────────────────────────────────────────────────┘

                // Authentification
                .requestMatchers(HttpMethod.POST, "/api/v1/auth/inscription").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/v1/auth/connexion").permitAll()

                // Catalogue formations (lecture publique)
                .requestMatchers(HttpMethod.GET, "/api/v1/formations").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/v1/formations/{id}").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/v1/formations/rechercher").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/v1/formations/categories").permitAll()

                // Sessions d'une formation (lecture publique pour afficher les dates)
                .requestMatchers(HttpMethod.GET, "/api/v1/formations/{id}/sessions").permitAll()

                // ┌──────────────────────────────────────────────────────────┐
                // │  ROUTES PROTÉGÉES — JWT obligatoire                       │
                // └──────────────────────────────────────────────────────────┘

                // Déconnexion
                .requestMatchers(HttpMethod.POST, "/api/v1/auth/deconnexion").authenticated()

                // Inscriptions et paiements (T-B-008 — ROLPH)
                .requestMatchers(HttpMethod.POST, "/api/v1/inscriptions/inscrire").authenticated()
                .requestMatchers(HttpMethod.POST, "/api/v1/inscriptions/reserver").authenticated()
                .requestMatchers(HttpMethod.GET,  "/api/v1/inscriptions/mes-inscriptions").authenticated()
                .requestMatchers(HttpMethod.GET,  "/api/v1/inscriptions/*/recu").authenticated()
                .requestMatchers(HttpMethod.POST, "/api/v1/inscriptions/*/confirmer-paiement").authenticated()

                // Espace personnel apprenant (T-B-009 — FENOSOA)
                .requestMatchers(HttpMethod.GET,  "/api/v1/apprenants/profil").authenticated()
                .requestMatchers(HttpMethod.PUT,  "/api/v1/apprenants/profil").authenticated()
                .requestMatchers(HttpMethod.GET,  "/api/v1/apprenants/inscriptions").authenticated()
                .requestMatchers(HttpMethod.GET,  "/api/v1/apprenants/recus").authenticated()

                // Toute route non déclarée → refusée
                .anyRequest().denyAll()
            )

            // Injecter le filtre JWT avant le filtre Spring standard
            .addFilterBefore(filtreJwt, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // ── Configuration CORS ────────────────────────────────────────────────────
    // Autorise les requêtes depuis le front Vue.js
    @Bean
    public CorsConfigurationSource sourceCors() {
        CorsConfiguration config = new CorsConfiguration();

        config.setAllowedOrigins(List.of(
            "http://localhost:5173",  // Vue.js Vite (dev)
            "http://localhost:3000",  // Alternative dev
            "https://eofana.mg"       // Production
        ));

        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("Authorization", "Content-Type", "Accept"));
        config.setExposedHeaders(List.of("Authorization"));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", config);
        return source;
    }
}
