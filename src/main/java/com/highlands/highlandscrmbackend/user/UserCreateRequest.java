package com.highlands.highlandscrmbackend.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.Set;
import java.util.UUID;

public record UserCreateRequest(

        @NotNull(message = "Company ID is required")
        UUID companyId,

        @NotBlank(message = "Email is required")
        @Email(message = "Email must be valid")
        @Size(max = 255, message = "Email must not exceed 255 characters")
        String email,

        @NotBlank(message = "Password is required")
        @Size(min = 8, max = 100,
                message = "Password must be between 8 and 100 characters")
        String password,

        @NotBlank(message = "First name is required")
        @Size(max = 100,
                message = "First name must not exceed 100 characters")
        String firstName,

        @NotBlank(message = "Last name is required")
        @Size(max = 100,
                message = "Last name must not exceed 100 characters")
        String lastName,

        Set<UUID> roleIds
) {
}