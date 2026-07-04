package com.eofana.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * Filtre d'authentification JWT étendu (Semaine 2 - SANDA - T-B-115).
 * * Intercepte chaque requête, extrait le token, le valide et injecte l'utilisateur
 * authentifié dans le SecurityContext avec l'autorité Spring Security appropriée
 * (ROLE_APPRENANT ou ROLE_FORMATEUR) basée sur les claims du jeton.
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    public JwtAuthenticationFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        // 1. Vérifier la présence et le format du Header Authorization
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7);

        try {
            // 2. Valider le token et s'assurer que le thread n'est pas déjà authentifié
            if (jwtService.isTokenValid(token) && SecurityContextHolder.getContext().getAuthentication() == null) {
                
                String email = jwtService.extractUsername(token);
                Long idUser = jwtService.extractIdUser(token);
                String role = jwtService.extractRole(token); // Extrait "FORMATEUR" ou "apprenant"

                if (email != null && role != null) {
                    // 3. Normalisation et attribution de l'autorité Spring Security (ROLE_FORMATEUR / ROLE_APPRENANT)
                    var authorities = List.of(new SimpleGrantedAuthority("ROLE_" + role.toUpperCase()));

                    // 4. Création du Principal à l'aide de l'objet Record minimal
                    AuthenticatedUser principal = new AuthenticatedUser(idUser, email, role.toUpperCase());
                    
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            principal, 
                            null, 
                            authorities
                    );

                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    // 5. Injection de l'authentification dans le SecurityContextHolder de Spring
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }
        } catch (Exception e) {
            // En cas d'anomalie ou de token altéré, sécurisation en vidant le contexte
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Principal minimal placé dans le SecurityContext : les
     * contrôleurs peuvent le récupérer via
     * `@AuthenticationPrincipal AuthenticatedUser user` pour connaître
     * l'utilisateur connecté sans requête supplémentaire.
     */
    public record AuthenticatedUser(Long idUser, String email, String role) {
    }
}