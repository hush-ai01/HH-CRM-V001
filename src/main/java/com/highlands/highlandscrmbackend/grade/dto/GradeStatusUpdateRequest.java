package com.highlands.highlandscrmbackend.grade.dto;

import jakarta.validation.constraints.NotNull;

public record GradeStatusUpdateRequest(

        @NotNull(message = "Active status is required")
        Boolean active
) {
}