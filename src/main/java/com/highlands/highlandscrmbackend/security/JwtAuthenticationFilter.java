package com.highlands.highlandscrmbackend.security;

import com.highlands.highlandscrmbackend.auth.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    public JwtAuthenticationFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        try {
            String authorizationHeader =
                    request.getHeader("Authorization");

            /*
             * No Authorization header.
             *
             * We don't authenticate the request here.
             * Spring Security will decide later whether
             * the endpoint requires authentication.
             */
            if (authorizationHeader == null
                    || !authorizationHeader.startsWith("Bearer ")) {

                filterChain.doFilter(request, response);
                return;
            }

            /*
             * Extract JWT from:
             *
             * Authorization: Bearer <token>
             */
            String token = authorizationHeader.substring(7);

            /*
             * If the JWT is invalid or expired, don't create
             * an authentication object.
             *
             * Spring Security will subsequently reject the
             * request if the endpoint is protected.
             */
            if (!jwtService.isTokenValid(token)) {
                filterChain.doFilter(request, response);
                return;
            }

            try {
                /*
                 * Extract identity information from the JWT.
                 */
                UUID userId =
                        jwtService.extractUserId(token);

                UUID companyId =
                        jwtService.extractCompanyId(token);

                String email =
                        jwtService.extractEmail(token);

                Set<String> roles =
                        jwtService.extractRoles(token);

                /*
                 * Convert application roles into Spring Security
                 * authorities.
                 *
                 * Example:
                 *
                 * ADMIN
                 *
                 * becomes:
                 *
                 * ROLE_ADMIN
                 */
                var authorities = roles.stream()
                        .map(role ->
                                new SimpleGrantedAuthority(
                                        "ROLE_" + role
                                )
                        )
                        .collect(Collectors.toSet());

                /*
                 * Create our application-specific authenticated
                 * principal.
                 */
                JwtAuthenticationPrincipal principal =
                        new JwtAuthenticationPrincipal(
                                userId,
                                companyId,
                                email
                        );

                /*
                 * Create the Spring Security Authentication object.
                 */
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                principal,
                                null,
                                authorities
                        );

                /*
                 * Store the authenticated user in Spring Security's
                 * SecurityContext.
                 */
                SecurityContextHolder
                        .getContext()
                        .setAuthentication(authentication);

                /*
                 * Establish the tenant/company for this request.
                 *
                 * IMPORTANT:
                 * The company ID comes from the trusted JWT,
                 * NOT from a frontend request parameter.
                 */
                TenantContext.setCompanyId(companyId);

            } catch (RuntimeException exception) {

                /*
                 * Something went wrong while extracting or processing
                 * the JWT.
                 *
                 * Do not leave a partially authenticated request
                 * in the SecurityContext.
                 */
                SecurityContextHolder.clearContext();
                TenantContext.clear();
            }

            /*
             * Continue the request through the remaining filters
             * and eventually to the controller.
             */
            filterChain.doFilter(request, response);

        } finally {

            /*
             * VERY IMPORTANT:
             *
             * Servlet container threads are reused.
             *
             * If we don't clear TenantContext, a subsequent request
             * handled by the same thread could accidentally inherit
             * the previous user's company ID.
             */
            TenantContext.clear();
        }
    }
}