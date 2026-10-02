package com.highlands.highlandscrmbackend.grade.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record CreateGradeRequest(

        @NotNull(message = "Commodity ID is required")
        UUID commodityId,

        @NotBlank(message = "Grade name is required")
        @Size(max = 100, message = "Grade name must not exceed 100 characters")
        String name,

        @NotBlank(message = "Grade code is required")
        @Size(max = 50, message = "Grade code must not exceed 50 characters")
        String code,

        @Size(max = 500, message = "Grade description must not exceed 500 characters")
        String description
) {
}