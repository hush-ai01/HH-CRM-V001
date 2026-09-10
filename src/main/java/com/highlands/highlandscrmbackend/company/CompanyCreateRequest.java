package com.highlands.highlandscrmbackend.company;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CompanyCreateRequest(

        @NotBlank(message = "Company name is required")
        @Size(max = 255, message = "Company name must not exceed 255 characters")
        String name,

        @NotBlank(message = "Company code is required")
        @Size(max = 100, message = "Company code must not exceed 100 characters")
        String code
) {
}