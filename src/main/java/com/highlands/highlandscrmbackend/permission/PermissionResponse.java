package com.highlands.highlandscrmbackend.permission;

import java.util.UUID;

public record PermissionResponse(
        UUID id,
        String code,
        String description
) {

    public static PermissionResponse from(Permission permission) {
        return new PermissionResponse(
                permission.getId(),
                permission.getCode(),
                permission.getDescription()
        );
    }
}