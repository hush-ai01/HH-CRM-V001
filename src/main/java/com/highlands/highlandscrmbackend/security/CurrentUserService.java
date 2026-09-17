package com.highlands.highlandscrmbackend.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class CurrentUserService {

    public JwtAuthenticationPrincipal getCurrentUser() {
        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        if (authentication == null
                || !(authentication.getPrincipal()
                instanceof JwtAuthenticationPrincipal principal)) {

            throw new IllegalStateException(
                    "No authenticated user is available"
            );
        }

        return principal;
    }

    public UUID getCurrentUserId() {
        return getCurrentUser().userId();
    }

    public UUID getCurrentCompanyId() {
        return getCurrentUser().companyId();
    }

    public String getCurrentUserEmail() {
        return getCurrentUser().email();
    }
}