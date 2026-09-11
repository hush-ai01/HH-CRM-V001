package com.highlands.highlandscrmbackend.auth;

import com.highlands.highlandscrmbackend.common.exception.ResourceNotFoundException;
import com.highlands.highlandscrmbackend.company.Company;
import com.highlands.highlandscrmbackend.company.CompanyRepository;
import com.highlands.highlandscrmbackend.user.User;
import com.highlands.highlandscrmbackend.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private CompanyRepository companyRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private Company company;

    @Mock
    private User user;

    private AuthService authService;

    private UUID companyId;
    private UUID userId;

    @BeforeEach
    void setUp() {
        authService = new AuthService(
                userRepository,
                companyRepository,
                passwordEncoder
        );

        companyId = UUID.randomUUID();
        userId = UUID.randomUUID();
    }

    @Test
    void shouldLoginSuccessfullyWithValidCredentials() {

        LoginRequest request = new LoginRequest(
                companyId,
                "admin@highlands.co.za",
                "Password123"
        );

        when(companyRepository.existsById(companyId))
                .thenReturn(true);

        when(userRepository.findByCompanyIdAndEmail(
                companyId,
                request.email()
        )).thenReturn(Optional.of(user));

        when(user.isActive())
                .thenReturn(true);

        when(passwordEncoder.matches(
                request.password(),
                user.getPasswordHash()
        )).thenReturn(true);

        when(user.getId())
                .thenReturn(userId);

        when(user.getCompany())
                .thenReturn(company);

        when(company.getId())
                .thenReturn(companyId);

        when(user.getEmail())
                .thenReturn(request.email());

        when(user.getFirstName())
                .thenReturn("Highlands");

        when(user.getLastName())
                .thenReturn("Admin");

        LoginResponse response = authService.login(request);

        assertNotNull(response);
        assertEquals(userId, response.userId());
        assertEquals(companyId, response.companyId());
        assertEquals("admin@highlands.co.za", response.email());
        assertEquals("Highlands", response.firstName());
        assertEquals("Admin", response.lastName());

        verify(passwordEncoder)
                .matches(request.password(), user.getPasswordHash());
    }

    @Test
    void shouldRejectInvalidPassword() {

        LoginRequest request = new LoginRequest(
                companyId,
                "admin@highlands.co.za",
                "WrongPassword"
        );

        when(companyRepository.existsById(companyId))
                .thenReturn(true);

        when(userRepository.findByCompanyIdAndEmail(
                companyId,
                request.email()
        )).thenReturn(Optional.of(user));

        when(user.isActive())
                .thenReturn(true);

        when(passwordEncoder.matches(
                request.password(),
                user.getPasswordHash()
        )).thenReturn(false);

        InvalidCredentialsException exception =
                assertThrows(
                        InvalidCredentialsException.class,
                        () -> authService.login(request)
                );

        assertEquals(
                "Invalid email or password",
                exception.getMessage()
        );
    }

    @Test
    void shouldRejectUnknownEmail() {

        LoginRequest request = new LoginRequest(
                companyId,
                "unknown@highlands.co.za",
                "Password123"
        );

        when(companyRepository.existsById(companyId))
                .thenReturn(true);

        when(userRepository.findByCompanyIdAndEmail(
                companyId,
                request.email()
        )).thenReturn(Optional.empty());

        InvalidCredentialsException exception =
                assertThrows(
                        InvalidCredentialsException.class,
                        () -> authService.login(request)
                );

        assertEquals(
                "Invalid email or password",
                exception.getMessage()
        );

        verify(passwordEncoder, never())
                .matches(anyString(), anyString());
    }

    @Test
    void shouldRejectInactiveUser() {

        LoginRequest request = new LoginRequest(
                companyId,
                "admin@highlands.co.za",
                "Password123"
        );

        when(companyRepository.existsById(companyId))
                .thenReturn(true);

        when(userRepository.findByCompanyIdAndEmail(
                companyId,
                request.email()
        )).thenReturn(Optional.of(user));

        when(user.isActive())
                .thenReturn(false);

        InvalidCredentialsException exception =
                assertThrows(
                        InvalidCredentialsException.class,
                        () -> authService.login(request)
                );

        assertEquals(
                "Invalid email or password",
                exception.getMessage()
        );

        verify(passwordEncoder, never())
                .matches(anyString(), anyString());
    }

    @Test
    void shouldRejectLoginWhenCompanyDoesNotExist() {

        LoginRequest request = new LoginRequest(
                companyId,
                "admin@highlands.co.za",
                "Password123"
        );

        when(companyRepository.existsById(companyId))
                .thenReturn(false);

        ResourceNotFoundException exception =
                assertThrows(
                        ResourceNotFoundException.class,
                        () -> authService.login(request)
                );

        assertEquals(
                "Company with id '" + companyId + "' not found",
                exception.getMessage()
        );

        verify(userRepository, never())
                .findByCompanyIdAndEmail(any(), anyString());
    }

    @Test
    void shouldNotAuthenticateUserFromAnotherCompany() {

        UUID anotherCompanyId = UUID.randomUUID();

        LoginRequest request = new LoginRequest(
                anotherCompanyId,
                "admin@highlands.co.za",
                "Password123"
        );

        when(companyRepository.existsById(anotherCompanyId))
                .thenReturn(true);

        when(userRepository.findByCompanyIdAndEmail(
                anotherCompanyId,
                request.email()
        )).thenReturn(Optional.empty());

        assertThrows(
                InvalidCredentialsException.class,
                () -> authService.login(request)
        );

        verify(userRepository)
                .findByCompanyIdAndEmail(
                        anotherCompanyId,
                        request.email()
                );

        verify(passwordEncoder, never())
                .matches(anyString(), anyString());
    }
}