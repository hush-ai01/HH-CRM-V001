package com.highlands.highlandscrmbackend.role;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record RoleCreateRequest(

        @NotNull(message = "Company ID is required")
        UUID companyId,

        @NotBlank(message = "Role name is required")
        @Size(max = 100, message = "Role name must not exceed 100 characters")
        String name,

        @Size(max = 255, message = "Role description must not exceed 255 characters")
        String description
) {
}