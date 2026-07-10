package com.eofana.security;

import com.eofana.entity.Utilisateur;
import com.eofana.repository.UtilisateurRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * T-B-115 (module Formateur) + T-B-215 (module Admin) : reconnaît les
 * tokens émis par /api/v1/auth/connexion, /api/v1/auth-formateur/connexion
 * ET /api/v1/auth-admin/connexion (T-B-204), et alimente le
 * SecurityContext Spring avec l'autorité correspondant au rôle réel
 * de l'utilisateur (ROLE_FORMATEUR, ROLE_APPRENANT, ROLE_ADMIN, ...).
 *
 * ⚠ Ce projet n'a PAS de JwtAuthenticationFilter existant à étendre
 * (aucune infrastructure JWT n'a été posée en Semaine 1 — voir
 * SecurityFilterConfig, qui laissait explicitement toutes les routes
 * ouvertes "en attendant JWT"). Ce filtre est donc un pont temporaire
 * qui reconnaît le format "TOKEN-DEMO-{idUser}" /
 * "TOKEN-DEMO-FORMATEUR-{idUser}" / "TOKEN-DEMO-ADMIN-{idUser}" déjà
 * émis par AuthController/AdminAuthController — PAS un vrai JWT
 * signé. Un token de démo n'a aucune valeur de sécurité réelle (pas
 * de signature, pas d'expiration) : à remplacer par un vrai
 * JwtService dès que ce chantier sera repris.
 *
 * Note de conception T-B-215 : le mapping "ROLE_" + role.name() était
 * DÉJÀ générique sur RoleUtilisateur (aucun changement nécessaire là) —
 * seul le pattern regex, qui ne reconnaissait que le préfixe
 * "FORMATEUR-", devait être étendu pour accepter "ADMIN-" également.
 */
@Component
@RequiredArgsConstructor
public class DemoTokenAuthenticationFilter extends OncePerRequestFilter {

    private static final Pattern PATTERN_TOKEN =
            Pattern.compile("^TOKEN-DEMO-(?:FORMATEUR-|ADMIN-)?(\\d+)$");

    private final UtilisateurRepository utilisateurRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                     HttpServletResponse response,
                                     FilterChain filterChain) throws ServletException, IOException {

        String header = request.getHeader("Authorization");

        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring("Bearer ".length()).trim();
            Matcher matcher = PATTERN_TOKEN.matcher(token);

            if (matcher.matches()) {
                Long idUser = Long.parseLong(matcher.group(1));
                Optional<Utilisateur> utilisateurOpt = utilisateurRepository.findById(idUser);

                if (utilisateurOpt.isPresent() && Boolean.TRUE.equals(utilisateurOpt.get().getEstActif())) {
                    Utilisateur utilisateur = utilisateurOpt.get();
                    String autorite = "ROLE_" + utilisateur.getRole().name().toUpperCase();

                    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                            utilisateur, null, List.of(new SimpleGrantedAuthority(autorite)));

                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            }
        }

        filterChain.doFilter(request, response);
    }
}
