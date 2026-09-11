package com.highlands.highlandscrmbackend.auth;

import com.highlands.highlandscrmbackend.security.JwtProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import java.util.Set;

class JwtServiceTest {

    private JwtService jwtService;

    private UUID userId;
    private UUID companyId;

    private Set<String> roles;

    @BeforeEach
    void setUp() {
        JwtProperties properties = new JwtProperties();

        properties.setSecret(
                "highlands-crm-local-secret-key-2026-very-secure"
        );
        properties.setExpirationMs(30 * 60 * 1000L);
        properties.setIssuer("highlands-crm");

        jwtService = new JwtService(properties);

        roles = Set.of("ADMIN", "MANAGER");

        userId = UUID.randomUUID();
        companyId = UUID.randomUUID();
    }

    @Test
    void shouldGenerateValidToken() {

        String token = jwtService.generateToken(
                userId,
                companyId,
                "john@highlands.com",
                roles
        );

        assertNotNull(token);
        assertFalse(token.isBlank());
    }

    @Test
    void shouldExtractUserIdFromToken() {

        String token = jwtService.generateToken(
                userId,
                companyId,
                "john@highlands.com",
                roles
        );

        UUID extractedUserId = jwtService.extractUserId(token);

        assertEquals(userId, extractedUserId);
    }

    @Test
    void shouldExtractCompanyIdFromToken() {

        String token = jwtService.generateToken(
                userId,
                companyId,
                "john@highlands.com",
                roles
        );

        UUID extractedCompanyId = jwtService.extractCompanyId(token);

        assertEquals(companyId, extractedCompanyId);
    }

    @Test
    void shouldExtractEmailFromToken() {

        String token = jwtService.generateToken(
                userId,
                companyId,
                "john@highlands.com",
                roles
        );

        String extractedEmail = jwtService.extractEmail(token);

        assertEquals("john@highlands.com", extractedEmail);
    }

    @Test
    void shouldValidateValidToken() {

        String token = jwtService.generateToken(
                userId,
                companyId,
                "john@highlands.com",
                roles
        );

        assertTrue(jwtService.isTokenValid(token));
    }

    @Test
    void shouldRejectInvalidToken() {

        assertFalse(
                jwtService.isTokenValid("this-is-not-a-valid-jwt")
        );
    }

    @Test
    void shouldExtractRolesFromToken() {
        String token = jwtService.generateToken(
                userId,
                companyId,
                "john@highlands.com",
                roles
        );

        Set<String> extractedRoles = jwtService.extractRoles(token);

        assertEquals(roles, extractedRoles);
    }

    @Test
    void shouldRejectExpiredToken() throws InterruptedException {
        JwtProperties properties = new JwtProperties();
        properties.setSecret("highlands-crm-local-secret-key-2026-very-secure");
        properties.setExpirationMs(1);
        properties.setIssuer("highlands-crm");

        JwtService shortLivedJwtService = new JwtService(properties);

        String token = shortLivedJwtService.generateToken(
                userId,
                companyId,
                "john@highlands.com",
                roles
        );

        Thread.sleep(10);

        assertFalse(shortLivedJwtService.isTokenValid(token));
    }

    @Test
    void shouldRejectTokenSignedWithDifferentSecret() {
        JwtProperties differentProperties = new JwtProperties();
        differentProperties.setSecret(
                "another-completely-different-secret-key-2026"
        );
        differentProperties.setExpirationMs(30 * 60 * 1000L);
        differentProperties.setIssuer("highlands-crm");

        JwtService differentJwtService = new JwtService(differentProperties);

        String token = differentJwtService.generateToken(
                userId,
                companyId,
                "john@highlands.com",
                roles
        );

        assertFalse(jwtService.isTokenValid(token));
    }
}