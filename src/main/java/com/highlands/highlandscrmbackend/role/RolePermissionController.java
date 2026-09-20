package com.highlands.highlandscrmbackend.role;

import com.highlands.highlandscrmbackend.permission.PermissionResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/roles/{roleId}/permissions")
public class RolePermissionController {

    private final RolePermissionService rolePermissionService;

    public RolePermissionController(
            RolePermissionService rolePermissionService
    ) {
        this.rolePermissionService = rolePermissionService;
    }

    @GetMapping
    public ResponseEntity<List<PermissionResponse>> getPermissions(
            @PathVariable UUID roleId
    ) {
        return ResponseEntity.ok(
                rolePermissionService.getPermissions(roleId)
        );
    }

    @PostMapping("/{permissionId}")
    public ResponseEntity<List<PermissionResponse>> addPermission(
            @PathVariable UUID roleId,
            @PathVariable UUID permissionId
    ) {
        return ResponseEntity.ok(
                rolePermissionService.addPermission(
                        roleId,
                        permissionId
                )
        );
    }

    @DeleteMapping("/{permissionId}")
    public ResponseEntity<List<PermissionResponse>> removePermission(
            @PathVariable UUID roleId,
            @PathVariable UUID permissionId
    ) {
        return ResponseEntity.ok(
                rolePermissionService.removePermission(
                        roleId,
                        permissionId
                )
        );
    }
}