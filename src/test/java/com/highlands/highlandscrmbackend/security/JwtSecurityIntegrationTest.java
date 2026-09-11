package com.highlands.highlandscrmbackend.security;

import com.highlands.highlandscrmbackend.auth.JwtService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Set;
import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class JwtSecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JwtService jwtService;

    private String token;
    private UUID userId;
    private UUID companyId;
    private String email;
    private Set<String> roles;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();

        token = "valid-test-token";
        userId = UUID.randomUUID();
        companyId = UUID.randomUUID();
        email = "admin@highlandsholdings.co.za";
        roles = Set.of("ADMIN");
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldRejectProtectedEndpointWithoutToken() throws Exception {

        mockMvc.perform(
                        get("/permissions")
                )
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldRejectProtectedEndpointWithInvalidToken() throws Exception {

        when(jwtService.isTokenValid(token))
                .thenReturn(false);

        mockMvc.perform(
                        get("/permissions")
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                )
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldAllowProtectedEndpointWithValidToken() throws Exception {

        when(jwtService.isTokenValid(token))
                .thenReturn(true);

        when(jwtService.extractUserId(token))
                .thenReturn(userId);

        when(jwtService.extractCompanyId(token))
                .thenReturn(companyId);

        when(jwtService.extractEmail(token))
                .thenReturn(email);

        when(jwtService.extractRoles(token))
                .thenReturn(roles);

        mockMvc.perform(
                        get("/permissions")
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                )
                .andExpect(status().isOk());
    }
}

