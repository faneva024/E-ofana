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

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class ConfigurationSecurite {

    private final FiltreAuthentificationJwt filtreAuthentificationJwt;

    public ConfigurationSecurite(FiltreAuthentificationJwt filtreAuthentificationJwt) {
        this.filtreAuthentificationJwt = filtreAuthentificationJwt;
    }

    @Bean
    public PasswordEncoder encodeurMotDePasse() {
        return new BCryptPasswordEncoder(10);
    }

    @Bean
    public AuthenticationManager gestionnaireAuthentification(
            AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain chaineFiltresSecurite(HttpSecurity http) throws Exception {

        http
            // Désactiver CSRF (API REST stateless, pas de formulaires)
            .csrf(csrf -> csrf.disable())

            // Activer CORS (pour Vue.js sur un port différent en dev)
            .cors(cors -> cors.configurationSource(sourceCors()))

            // API stateless — aucune session HTTP côté serveur
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )

            // ── Règles d'autorisation ──────────────────────────────
            .authorizeHttpRequests(auth -> auth

                // Authentification
                .requestMatchers(HttpMethod.POST, "/api/v1/auth/inscription").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/v1/auth/connexion").permitAll()

                // Consultation des formations (lecture seule)
                .requestMatchers(HttpMethod.GET,  "/api/v1/formations").permitAll()
                .requestMatchers(HttpMethod.GET,  "/api/v1/formations/{id}").permitAll()
                .requestMatchers(HttpMethod.GET,  "/api/v1/formations/rechercher").permitAll()
                .requestMatchers(HttpMethod.GET,  "/api/v1/formations/categories").permitAll()

                // Déconnexion
                .requestMatchers(HttpMethod.POST, "/api/v1/auth/deconnexion").authenticated()

                // Inscriptions et paiements
                .requestMatchers(HttpMethod.POST, "/api/v1/inscriptions/inscrire").authenticated()
                .requestMatchers(HttpMethod.POST, "/api/v1/inscriptions/reserver").authenticated()
                .requestMatchers(HttpMethod.GET,  "/api/v1/inscriptions/mes-inscriptions").authenticated()
                .requestMatchers(HttpMethod.GET,  "/api/v1/inscriptions/*/recu").authenticated()
                .requestMatchers(HttpMethod.POST, "/api/v1/inscriptions/*/confirmer-paiement").authenticated()

                // Espace personnel apprenant
                .requestMatchers(HttpMethod.GET,  "/api/v1/apprenants/profil").authenticated()
                .requestMatchers(HttpMethod.PUT,  "/api/v1/apprenants/profil").authenticated()
                .requestMatchers(HttpMethod.GET,  "/api/v1/apprenants/inscriptions").authenticated()
                .requestMatchers(HttpMethod.GET,  "/api/v1/apprenants/recus").authenticated()

                // Toute autre route non déclarée → refusée par défaut
                .anyRequest().denyAll()
            )

            // Injecter le filtre JWT avant le filtre d'authentification standard
            .addFilterBefore(
                filtreAuthentificationJwt,
                UsernamePasswordAuthenticationFilter.class
            );

        return http.build();
    }

    @Bean
    public CorsConfigurationSource sourceCors() {
        CorsConfiguration config = new CorsConfiguration();

        // Origines autorisées (adapter pour la production)
        config.setAllowedOrigins(List.of(
                "http://localhost:5173",   
                "http://localhost:3000",  
                "https://eofana.mg"        
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
