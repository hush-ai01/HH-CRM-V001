package com.highlands.highlandscrmbackend.auth;

import java.util.UUID;

public record LoginResponse(
        UUID userId,
        UUID companyId,
        String email,
        String firstName,
        String lastName
) {
}