package com.highlands.highlandscrmbackend.auth;

import com.highlands.highlandscrmbackend.security.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;
import java.util.Set;

@Service
public class JwtService {

    private final JwtProperties jwtProperties;
    private final SecretKey signingKey;

    public JwtService(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
        this.signingKey = Keys.hmacShaKeyFor(
                jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8)
        );
    }

    public String generateToken(
            UUID userId,
            UUID companyId,
            String email,
            Set<String> roles
    ) {
        Date issuedAt = new Date();
        Date expiration = new Date(
                issuedAt.getTime() + jwtProperties.getExpirationMs()
        );

        return Jwts.builder()
                .subject(userId.toString())
                .claim("companyId", companyId.toString())
                .claim("email", email)
                .claim("roles", roles)
                .issuer(jwtProperties.getIssuer())
                .issuedAt(issuedAt)
                .expiration(expiration)
                .signWith(signingKey)
                .compact();
    }

    public Set<String> extractRoles(String token) {
        Object rolesClaim = extractAllClaims(token).get("roles");

        if (rolesClaim instanceof java.util.List<?> roles) {
            return roles.stream()
                    .map(Object::toString)
                    .collect(java.util.stream.Collectors.toSet());
        }

        return Set.of();
    }

    public UUID extractUserId(String token) {
        return UUID.fromString(
                extractAllClaims(token).getSubject()
        );
    }

    public UUID extractCompanyId(String token) {
        return UUID.fromString(
                extractAllClaims(token).get("companyId", String.class)
        );
    }

    public String extractEmail(String token) {
        return extractAllClaims(token).get("email", String.class);
    }

    public boolean isTokenValid(String token) {
        try {
            Claims claims = extractAllClaims(token);

            return jwtProperties.getIssuer().equals(claims.getIssuer())
                    && claims.getExpiration().after(new Date());

        } catch (Exception exception) {
            return false;
        }
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}