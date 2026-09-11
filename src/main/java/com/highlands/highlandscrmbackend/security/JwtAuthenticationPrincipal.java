package com.highlands.highlandscrmbackend.security;

import java.util.UUID;

public record JwtAuthenticationPrincipal(
        UUID userId,
        UUID companyId,
        String email
) {
}