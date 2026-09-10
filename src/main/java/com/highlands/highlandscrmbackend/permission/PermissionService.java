package com.highlands.highlandscrmbackend.permission;

import com.highlands.highlandscrmbackend.common.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class PermissionService {

    private final PermissionRepository permissionRepository;

    public PermissionService(PermissionRepository permissionRepository) {
        this.permissionRepository = permissionRepository;
    }

    @Transactional(readOnly = true)
    public List<PermissionResponse> getAllPermissions() {
        return permissionRepository.findAll()
                .stream()
                .map(PermissionResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public PermissionResponse getPermissionById(UUID id) {
        Permission permission = permissionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Permission with id '" + id + "' not found"
                ));

        return PermissionResponse.from(permission);
    }

    @Transactional(readOnly = true)
    public PermissionResponse getPermissionByCode(String code) {
        Permission permission = permissionRepository.findByCode(code)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Permission with code '" + code + "' not found"
                ));

        return PermissionResponse.from(permission);
    }
}