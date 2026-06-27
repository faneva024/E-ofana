package com.eofana.securite;

import com.eofana.depot.UtilisateurDepot;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * FiltreAuthentificationJwt — T-B-012 (SANDA)
 * Branche : back → part-Rolph
 *
 * Ce filtre est déclaré ici pour permettre la compilation de ConfigurationSecurite.
 * L'implémentation complète est assignée à SANDA (T-B-012).
 *
 * Rôle : intercepter chaque requête HTTP, extraire et valider le token JWT
 * depuis le header Authorization, puis injecter l'identité dans le SecurityContext.
 *
 * Référence BDD v3.0 : utilise la table "utilisateurs" via UtilisateurDepot,
 * colonne "email" comme identifiant principal.
 */
@Component
public class FiltreAuthentificationJwt extends OncePerRequestFilter {

    private final UserDetailsService userDetailsService;

    @Value("${eofana.jwt.secret:eofana-secret-key-dev}")
    private String jwtSecret;

    public FiltreAuthentificationJwt(UserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest  request,
                                    HttpServletResponse response,
                                    FilterChain         filterChain)
            throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");

        // Pas de token → on laisse passer (les routes publiques sont autorisées)
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            String token = authHeader.substring(7);

            // Extraire l'email depuis le token JWT
            Claims claims = Jwts.parserBuilder()
                .setSigningKey(jwtSecret.getBytes())
                .build()
                .parseClaimsJws(token)
                .getBody();

            String email = claims.getSubject();

            if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = userDetailsService.loadUserByUsername(email);

                UsernamePasswordAuthenticationToken authToken =
                    new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities()
                    );
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        } catch (Exception e) {
            // Token invalide ou expiré → réponse 401
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("{\"erreur\":\"Token invalide ou expiré.\"}");
            return;
        }

        filterChain.doFilter(request, response);
    }
}
