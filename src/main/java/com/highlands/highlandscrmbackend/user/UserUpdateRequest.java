package com.highlands.highlandscrmbackend.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

public record UserUpdateRequest(

        @Email(message = "Email must be valid")
        @Size(
                max = 255,
                message = "Email must not exceed 255 characters"
        )
        String email,

        @Size(
                max = 100,
                message = "First name must not exceed 100 characters"
        )
        String firstName,

        @Size(
                max = 100,
                message = "Last name must not exceed 100 characters"
        )
        String lastName
) {}

