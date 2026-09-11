package com.highlands.highlandscrmbackend.security;

import com.highlands.highlandscrmbackend.auth.JwtService;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock
    private JwtService jwtService;

    @Mock
    private FilterChain filterChain;

    private JwtAuthenticationFilter filter;

    private UUID userId;
    private UUID companyId;
    private String email;
    private Set<String> roles;

    @BeforeEach
    void setUp() {
        filter = new JwtAuthenticationFilter(jwtService);

        userId = UUID.randomUUID();
        companyId = UUID.randomUUID();
        email = "admin@highlandsholdings.co.za";
        roles = Set.of("ADMIN", "MANAGER");

        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldContinueChainWhenAuthorizationHeaderIsMissing() throws Exception {

        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(request, response);

        assertNull(
                SecurityContextHolder.getContext().getAuthentication()
        );
    }

    @Test
    void shouldContinueChainWhenAuthorizationHeaderDoesNotUseBearerScheme()
            throws Exception {

        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        request.addHeader(
                "Authorization",
                "Basic some-token"
        );

        filter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(request, response);

        assertNull(
                SecurityContextHolder.getContext().getAuthentication()
        );
    }

    @Test
    void shouldContinueChainWhenTokenIsInvalid() throws Exception {

        String token = "invalid-token";

        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        request.addHeader(
                "Authorization",
                "Bearer " + token
        );

        when(jwtService.isTokenValid(token))
                .thenReturn(false);

        filter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(request, response);

        assertNull(
                SecurityContextHolder.getContext().getAuthentication()
        );
    }

    @Test
    void shouldAuthenticateRequestWhenTokenIsValid() throws Exception {

        String token = "valid-token";

        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        request.addHeader(
                "Authorization",
                "Bearer " + token
        );

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

        filter.doFilter(request, response, filterChain);

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        assertTrue(authentication.isAuthenticated());

        verify(filterChain).doFilter(request, response);
    }

    @Test
    void shouldCreateAuthenticationPrincipalWithCorrectUserDetails()
            throws Exception {

        String token = "valid-token";

        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        request.addHeader(
                "Authorization",
                "Bearer " + token
        );

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

        filter.doFilter(request, response, filterChain);

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        JwtAuthenticationPrincipal principal =
                assertInstanceOf(
                        JwtAuthenticationPrincipal.class,
                        authentication.getPrincipal()
                );

        assertEquals(userId, principal.userId());
        assertEquals(companyId, principal.companyId());
        assertEquals(email, principal.email());
    }

    @Test
    void shouldConvertRolesIntoSpringSecurityAuthorities()
            throws Exception {

        String token = "valid-token";

        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        request.addHeader(
                "Authorization",
                "Bearer " + token
        );

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

        filter.doFilter(request, response, filterChain);

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        assertTrue(
                authentication.getAuthorities()
                        .stream()
                        .anyMatch(authority ->
                                authority.getAuthority().equals("ROLE_ADMIN")
                        )
        );

        assertTrue(
                authentication.getAuthorities()
                        .stream()
                        .anyMatch(authority ->
                                authority.getAuthority().equals("ROLE_MANAGER")
                        )
        );
    }

    @Test
    void shouldClearSecurityContextWhenTokenProcessingFails()
            throws Exception {

        String token = "malformed-token";

        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        request.addHeader(
                "Authorization",
                "Bearer " + token
        );

        when(jwtService.isTokenValid(token))
                .thenReturn(true);

        when(jwtService.extractUserId(token))
                .thenThrow(new IllegalArgumentException("Malformed token"));

        filter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(request, response);

        assertNull(
                SecurityContextHolder.getContext().getAuthentication()
        );
    }
}

