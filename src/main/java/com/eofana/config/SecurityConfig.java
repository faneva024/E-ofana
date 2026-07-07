package com.eofana.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * Déclare le bean BCryptPasswordEncoder utilisé dans tout le projet
 * pour hasher et vérifier les mots de passe (voir UtilisateurService).
 *
 * BCrypt génère un sel aléatoire différent à chaque appel de encode(),
 * donc deux hash du même mot de passe ne seront jamais identiques —
 * c'est volontaire et c'est ce qui protège contre les attaques par
 * table de correspondance (rainbow tables).
 *
 * La force par défaut est 10 rounds, cohérente avec le commentaire
 * de la colonne motDePasse en base ("Hash BCrypt minimum 10 rounds").
 */
@Configuration
public class SecurityConfig {

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(10);
    }
}
