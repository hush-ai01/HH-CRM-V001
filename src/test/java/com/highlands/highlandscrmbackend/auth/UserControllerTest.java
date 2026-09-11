package com.highlands.highlandscrmbackend.auth;

import com.highlands.highlandscrmbackend.common.exception.GlobalExceptionHandler;
import com.highlands.highlandscrmbackend.common.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private AuthService authService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {

        AuthController authController =
                new AuthController(authService);

        mockMvc = MockMvcBuilders
                .standaloneSetup(authController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void shouldLoginSuccessfully() throws Exception {

        UUID companyId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        LoginResponse response = new LoginResponse(
                userId,
                companyId,
                "admin@highlands.co.za",
                "Highlands",
                "Admin"
        );

        when(authService.login(any(LoginRequest.class)))
                .thenReturn(response);

        mockMvc.perform(
                        post("/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "companyId": "%s",
                                          "email": "admin@highlands.co.za",
                                          "password": "Password123"
                                        }
                                        """.formatted(companyId))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(userId.toString()))
                .andExpect(jsonPath("$.companyId").value(companyId.toString()))
                .andExpect(jsonPath("$.email")
                        .value("admin@highlands.co.za"))
                .andExpect(jsonPath("$.firstName")
                        .value("Highlands"))
                .andExpect(jsonPath("$.lastName")
                        .value("Admin"));

        verify(authService).login(any(LoginRequest.class));
    }

    @Test
    void shouldRejectInvalidRequest() throws Exception {

        UUID companyId = UUID.randomUUID();

        mockMvc.perform(
                        post("/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "companyId": "%s",
                                          "email": "",
                                          "password": ""
                                        }
                                        """.formatted(companyId))
                )
                .andExpect(status().isBadRequest());

        verify(authService, never())
                .login(any(LoginRequest.class));
    }

    @Test
    void shouldRejectInvalidCredentials() throws Exception {

        UUID companyId = UUID.randomUUID();

        when(authService.login(any(LoginRequest.class)))
                .thenThrow(
                        new InvalidCredentialsException(
                                "Invalid email or password"
                        )
                );

        mockMvc.perform(
                        post("/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "companyId": "%s",
                                          "email": "admin@highlands.co.za",
                                          "password": "WrongPassword"
                                        }
                                        """.formatted(companyId))
                )
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldRejectMissingCompany() throws Exception {

        UUID companyId = UUID.randomUUID();

        when(authService.login(any(LoginRequest.class)))
                .thenThrow(
                        new ResourceNotFoundException(
                                "Company with id '" + companyId + "' not found"
                        )
                );

        mockMvc.perform(
                        post("/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "companyId": "%s",
                                          "email": "admin@highlands.co.za",
                                          "password": "Password123"
                                        }
                                        """.formatted(companyId))
                )
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldRejectMalformedCompanyId() throws Exception {

        mockMvc.perform(
                        post("/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "companyId": "not-a-uuid",
                                          "email": "admin@highlands.co.za",
                                          "password": "Password123"
                                        }
                                        """)
                )
                .andExpect(status().isBadRequest());

        verify(authService, never())
                .login(any(LoginRequest.class));
    }
}