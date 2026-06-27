package com.eofana.config;

import com.eofana.service.ServiceJwt;
import com.eofana.service.ServiceUtilisateur;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class FiltreAuthentificationJwt extends OncePerRequestFilter {

    private final ServiceJwt serviceJwt;
    private final ServiceUtilisateur serviceUtilisateur;

    public FiltreAuthentificationJwt(ServiceJwt serviceJwt, ServiceUtilisateur serviceUtilisateur) {
        this.serviceJwt = serviceJwt;
        this.serviceUtilisateur = serviceUtilisateur;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");
        String email = null;
        String jwt = null;

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            jwt = authHeader.substring(7);
            try {
                email = serviceJwt.extraireEmail(jwt);
            } catch (Exception e) {
                logger.warn("Impossible d'extraire l'email depuis le JWT : " + e.getMessage());
            }
        }

        if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = serviceUtilisateur.loadUserByUsername(email);
            if (serviceJwt.validerToken(jwt, userDetails.getUsername())) {
                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }
        filterChain.doFilter(request, response);
    }
}
