package com.highlands.highlandscrmbackend.fleet.dto;

import com.highlands.highlandscrmbackend.fleet.DriverIdentificationType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpdateDriverRequest(

        @NotBlank
        @Size(max = 100)
        String firstName,

        @NotBlank
        @Size(max = 100)
        String lastName,

        @NotNull
        DriverIdentificationType identificationType,

        @NotBlank
        @Size(max = 50)
        String identificationNumber,

        @NotBlank
        @Size(max = 30)
        String phone
) {
}