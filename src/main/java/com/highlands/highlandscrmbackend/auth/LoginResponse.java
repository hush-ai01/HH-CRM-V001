package com.highlands.highlandscrmbackend.auth;

import java.util.UUID;

public record LoginResponse(
        String accessToken,
        String tokenType,
        long expiresIn,
        UUID userId,
        UUID companyId,
        String email,
        String firstName,
        String lastName
) {
}