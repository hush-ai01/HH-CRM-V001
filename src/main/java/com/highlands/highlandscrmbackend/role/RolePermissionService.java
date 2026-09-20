package com.highlands.highlandscrmbackend.role;

import com.highlands.highlandscrmbackend.common.exception.ResourceNotFoundException;
import com.highlands.highlandscrmbackend.permission.Permission;
import com.highlands.highlandscrmbackend.permission.PermissionRepository;
import com.highlands.highlandscrmbackend.permission.PermissionResponse;
import com.highlands.highlandscrmbackend.security.TenantContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class RolePermissionService {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;

    public RolePermissionService(
            RoleRepository roleRepository,
            PermissionRepository permissionRepository
    ) {
        this.roleRepository = roleRepository;
        this.permissionRepository = permissionRepository;
    }

    @Transactional(readOnly = true)
    public List<PermissionResponse> getPermissions(UUID roleId) {

        UUID companyId = TenantContext.requireCompanyId();

        Role role = roleRepository.findByIdAndCompanyId(
                        roleId,
                        companyId
                )
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Role with id '" + roleId + "' not found"
                ));

        return role.getPermissions()
                .stream()
                .map(PermissionResponse::from)
                .toList();
    }

    public List<PermissionResponse> addPermission(
            UUID roleId,
            UUID permissionId
    ) {

        UUID companyId = TenantContext.requireCompanyId();

        Role role = roleRepository.findByIdAndCompanyId(
                        roleId,
                        companyId
                )
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Role with id '" + roleId + "' not found"
                ));

        Permission permission = permissionRepository.findById(permissionId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Permission with id '" + permissionId + "' not found"
                ));

        role.addPermission(permission);

        Role savedRole = roleRepository.save(role);

        return savedRole.getPermissions()
                .stream()
                .map(PermissionResponse::from)
                .toList();
    }

    public List<PermissionResponse> removePermission(
            UUID roleId,
            UUID permissionId
    ) {

        UUID companyId = TenantContext.requireCompanyId();

        Role role = roleRepository.findByIdAndCompanyId(
                        roleId,
                        companyId
                )
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Role with id '" + roleId + "' not found"
                ));

        Permission permission = permissionRepository.findById(permissionId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Permission with id '" + permissionId + "' not found"
                ));

        role.removePermission(permission);

        Role savedRole = roleRepository.save(role);

        return savedRole.getPermissions()
                .stream()
                .map(PermissionResponse::from)
                .toList();
    }
}