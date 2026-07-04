package com.eofana.security;

import com.eofana.entity.Utilisateur;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Service de génération et de validation des tokens JWT (T-B-015).
 */
@Service
public class JwtService {

    private final SecretKey secretKey;
    private final long expirationMs;

    public JwtService(
            @Value("${eofana.jwt.secret}") String secret,
            @Value("${eofana.jwt.expiration-ms:86400000}") long expirationMs
    ) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationMs = expirationMs;
    }

    /**
     * Surchargée (Semaine 1) : Génère un token simple sans claims additionnels.
     */
    public String generateToken(Utilisateur utilisateur) {
        return generateToken(new HashMap<>(), utilisateur);
    }

    /**
     * Surchargée - SANDA (Semaine 2 - T-B-115) : Génère un token avec des claims additionnels.
     * Permet d'injecter "typeUtilisateur" pour différencier dynamiquement l'espace Formateur.
     */
    public String generateToken(Map<String, Object> extraClaims, Utilisateur utilisateur) {
        Date now = new Date();
        Date expiration = new Date(now.getTime() + expirationMs);

        return Jwts.builder()
                .setClaims(extraClaims) // Injecte les claims supplémentaires (comme typeUtilisateur)
                .setSubject(utilisateur.getEmail()) // L'email reste l'identifiant principal (Subject)
                .claim("idUser", utilisateur.getIdUser())
                .claim("role", utilisateur.getRole().name())
                .setIssuedAt(now)
                .setExpiration(expiration)
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }

    public String extractUsername(String token) {
        return parseClaims(token).getSubject();
    }

    public Long extractIdUser(String token) {
        return parseClaims(token).get("idUser", Long.class);
    }

    public String extractRole(String token) {
        return parseClaims(token).get("role", String.class);
    }

    public boolean isTokenValid(String token) {
        try {
            Claims claims = parseClaims(token);
            return claims.getExpiration().after(new Date());
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    private Claims parseClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}