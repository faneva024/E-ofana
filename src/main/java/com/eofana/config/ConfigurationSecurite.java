package com.eofana.config;

import com.eofana.service.ServiceUtilisateur;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class ConfigurationSecurite {

    private final FiltreAuthentificationJwt filtreJwt;
    private final ServiceUtilisateur serviceUtilisateur;

    public ConfigurationSecurite(FiltreAuthentificationJwt filtreJwt, ServiceUtilisateur serviceUtilisateur) {
        this.filtreJwt = filtreJwt;
        this.serviceUtilisateur = serviceUtilisateur;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .authorizeHttpRequests(auth -> auth
                // Routes publiques
                .requestMatchers(HttpMethod.GET, "/").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/v1/auth/inscription").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/v1/auth/connexion").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/v1/formations").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/v1/formations/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/v1/formations/rechercher").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/v1/formations/categories").permitAll()
                // Routes protégées
                .anyRequest().authenticated()
            )
            .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authenticationProvider(authenticationProvider())
            .addFilterBefore(filtreJwt, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(serviceUtilisateur);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
