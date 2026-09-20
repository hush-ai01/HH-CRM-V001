package com.highlands.highlandscrmbackend.user;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/users/{userId}/roles")
public class UserRoleController {

    private final UserRoleService userRoleService;

    public UserRoleController(
            UserRoleService userRoleService
    ) {
        this.userRoleService = userRoleService;
    }

    @GetMapping
    public ResponseEntity<List<UserRoleResponse>> getRoles(
            @PathVariable UUID userId
    ) {
        return ResponseEntity.ok(
                userRoleService.getRoles(userId)
        );
    }

    @PostMapping("/{roleId}")
    public ResponseEntity<List<UserRoleResponse>> addRole(
            @PathVariable UUID userId,
            @PathVariable UUID roleId
    ) {
        return ResponseEntity.ok(
                userRoleService.addRole(
                        userId,
                        roleId
                )
        );
    }

    @DeleteMapping("/{roleId}")
    public ResponseEntity<List<UserRoleResponse>> removeRole(
            @PathVariable UUID userId,
            @PathVariable UUID roleId
    ) {
        return ResponseEntity.ok(
                userRoleService.removeRole(
                        userId,
                        roleId
                )
        );
    }
}