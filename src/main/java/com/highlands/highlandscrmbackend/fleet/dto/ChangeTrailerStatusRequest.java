package com.highlands.highlandscrmbackend.fleet.dto;

import jakarta.validation.constraints.NotBlank;

public record ChangeTrailerStatusRequest(
        @NotBlank String status
) {
}