package com.highlands.highlandscrmbackend.security;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class CurrentUserServiceTest {

    private CurrentUserService currentUserService;

    @BeforeEach
    void setUp() {
        currentUserService = new CurrentUserService();
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void isManagementUser_shouldReturnTrueForManagementRole() {

        JwtAuthenticationPrincipal principal =
                new JwtAuthenticationPrincipal(
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        "manager@highlands.co.za"
                );

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        principal,
                        null,
                        List.of(
                                new SimpleGrantedAuthority(
                                        "ROLE_management"
                                )
                        )
                );

        SecurityContextHolder
                .getContext()
                .setAuthentication(authentication);

        assertTrue(
                currentUserService.isManagementUser()
        );
    }

    @Test
    void isManagementUser_shouldReturnFalseForNonManagementRole() {

        JwtAuthenticationPrincipal principal =
                new JwtAuthenticationPrincipal(
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        "sales@highlands.co.za"
                );

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        principal,
                        null,
                        List.of(
                                new SimpleGrantedAuthority(
                                        "ROLE_salesperson"
                                )
                        )
                );

        SecurityContextHolder
                .getContext()
                .setAuthentication(authentication);

        assertFalse(
                currentUserService.isManagementUser()
        );
    }

    @Test
    void isManagementUser_shouldReturnFalseWhenAuthenticationIsMissing() {

        assertFalse(
                currentUserService.isManagementUser()
        );
    }
}