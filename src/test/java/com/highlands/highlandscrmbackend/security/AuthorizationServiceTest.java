package com.highlands.highlandscrmbackend.security;

import com.highlands.highlandscrmbackend.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthorizationServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private CurrentUserService currentUserService;

    private AuthorizationService authorizationService;

    private UUID userId;
    private UUID companyId;

    @BeforeEach
    void setUp() {
        authorizationService = new AuthorizationService(
                userRepository,
                currentUserService
        );

        userId = UUID.randomUUID();
        companyId = UUID.randomUUID();

        when(currentUserService.getCurrentUserId())
                .thenReturn(userId);

        when(currentUserService.getCurrentCompanyId())
                .thenReturn(companyId);
    }

    @Test
    void shouldReturnTrueWhenUserHasPermission() {

        when(userRepository.hasPermission(
                userId,
                companyId,
                "CLIENT_READ"
        )).thenReturn(true);

        boolean result =
                authorizationService.hasPermission("CLIENT_READ");

        assertTrue(result);

        verify(userRepository)
                .hasPermission(
                        userId,
                        companyId,
                        "CLIENT_READ"
                );
    }

    @Test
    void shouldReturnFalseWhenUserDoesNotHavePermission() {

        when(userRepository.hasPermission(
                userId,
                companyId,
                "CLIENT_READ"
        )).thenReturn(false);

        boolean result =
                authorizationService.hasPermission("CLIENT_READ");

        assertFalse(result);

        verify(userRepository)
                .hasPermission(
                        userId,
                        companyId,
                        "CLIENT_READ"
                );
    }

    @Test
    void shouldAllowWhenRequiredPermissionExists() {

        when(userRepository.hasPermission(
                userId,
                companyId,
                "CLIENT_CREATE"
        )).thenReturn(true);

        assertDoesNotThrow(() ->
                authorizationService.requirePermission(
                        "CLIENT_CREATE"
                )
        );

        verify(userRepository)
                .hasPermission(
                        userId,
                        companyId,
                        "CLIENT_CREATE"
                );
    }

    @Test
    void shouldThrowForbiddenWhenRequiredPermissionIsMissing() {

        when(userRepository.hasPermission(
                userId,
                companyId,
                "CLIENT_UPDATE"
        )).thenReturn(false);

        ForbiddenException exception =
                assertThrows(
                        ForbiddenException.class,
                        () -> authorizationService.requirePermission(
                                "CLIENT_UPDATE"
                        )
                );

        assertEquals(
                "You do not have permission to perform this action",
                exception.getMessage()
        );

        verify(userRepository)
                .hasPermission(
                        userId,
                        companyId,
                        "CLIENT_UPDATE"
                );
    }

    @Test
    void shouldCheckPermissionUsingCurrentUserAndCompany() {

        when(userRepository.hasPermission(
                userId,
                companyId,
                "CLIENT_DELETE"
        )).thenReturn(true);

        authorizationService.hasPermission("CLIENT_DELETE");

        verify(currentUserService)
                .getCurrentUserId();

        verify(currentUserService)
                .getCurrentCompanyId();

        verify(userRepository)
                .hasPermission(
                        userId,
                        companyId,
                        "CLIENT_DELETE"
                );
    }

    @Test
    void shouldNotCheckAnotherUserOrCompany() {

        when(userRepository.hasPermission(
                userId,
                companyId,
                "CLIENT_READ"
        )).thenReturn(true);

        authorizationService.hasPermission("CLIENT_READ");

        verify(userRepository, times(1))
                .hasPermission(
                        eq(userId),
                        eq(companyId),
                        eq("CLIENT_READ")
                );

        verifyNoMoreInteractions(userRepository);
    }
}