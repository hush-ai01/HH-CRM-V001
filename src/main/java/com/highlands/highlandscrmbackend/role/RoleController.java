package com.highlands.highlandscrmbackend.role;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/roles")
public class RoleController {

    private final RoleService roleService;

    public RoleController(RoleService roleService) {
        this.roleService = roleService;
    }

    @PostMapping
    public ResponseEntity<RoleResponse> createRole(
            @Valid @RequestBody RoleCreateRequest request) {

        RoleResponse response = roleService.createRole(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping("/company/{companyId}")
    public ResponseEntity<List<RoleResponse>> getRolesByCompany(
            @PathVariable UUID companyId) {

        return ResponseEntity.ok(
                roleService.getRolesByCompany(companyId)
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<RoleResponse> getRoleById(
            @PathVariable UUID id) {

        return ResponseEntity.ok(
                roleService.getRoleById(id)
        );
    }
}