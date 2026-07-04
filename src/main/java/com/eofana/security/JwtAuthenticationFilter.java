package com.eofana.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * Filtre d'authentification JWT (T-B-015).
 *
 * Intercepte chaque requête, extrait le token de l'en-tête
 * Authorization: Bearer {token}, le valide, et place l'utilisateur
 * authentifié dans le SecurityContext avec son rôle comme autorité
 * Spring Security (ex : ROLE_APPRENANT), ce qui permettra plus tard
 * de distinguer apprenant / formateur avec @PreAuthorize.
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

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7);

        if (jwtService.isTokenValid(token) && SecurityContextHolder.getContext().getAuthentication() == null) {
            String email = jwtService.extractUsername(token);
            Long idUser = jwtService.extractIdUser(token);
            String role = jwtService.extractRole(token);

            var authorities = List.of(new SimpleGrantedAuthority("ROLE_" + role.toUpperCase()));

            var authToken = new UsernamePasswordAuthenticationToken(
                    new AuthenticatedUser(idUser, email, role), null, authorities);

            SecurityContextHolder.getContext().setAuthentication(authToken);
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
