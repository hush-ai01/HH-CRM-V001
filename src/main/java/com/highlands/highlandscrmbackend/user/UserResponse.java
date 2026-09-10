package com.highlands.highlandscrmbackend.user;

import java.time.OffsetDateTime;
import java.util.Set;
import java.util.UUID;

public record UserResponse(
        UUID id,
        UUID companyId,
        String email,
        String firstName,
        String lastName,
        boolean active,
        Set<UUID> roleIds,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {

    public static UserResponse from(User user) {

        Set<UUID> roleIds = user.getRoles()
                .stream()
                .map(role -> role.getId())
                .collect(java.util.stream.Collectors.toSet());

        return new UserResponse(
                user.getId(),
                user.getCompany().getId(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.isActive(),
                roleIds,
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }
}