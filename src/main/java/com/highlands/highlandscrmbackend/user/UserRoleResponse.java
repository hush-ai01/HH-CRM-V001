package com.highlands.highlandscrmbackend.user;

import com.highlands.highlandscrmbackend.role.Role;

import java.time.OffsetDateTime;
import java.util.UUID;

public record UserRoleResponse(
        UUID id,
        UUID companyId,
        String name,
        String description,
        boolean active,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {

    public static UserRoleResponse from(Role role) {
        return new UserRoleResponse(
                role.getId(),
                role.getCompany().getId(),
                role.getName(),
                role.getDescription(),
                role.isActive(),
                role.getCreatedAt(),
                role.getUpdatedAt()
        );
    }
}