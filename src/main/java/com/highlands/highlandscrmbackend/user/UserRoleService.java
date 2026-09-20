package com.highlands.highlandscrmbackend.user;

import com.highlands.highlandscrmbackend.common.exception.ResourceNotFoundException;
import com.highlands.highlandscrmbackend.role.Role;
import com.highlands.highlandscrmbackend.role.RoleRepository;
import com.highlands.highlandscrmbackend.security.TenantContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class UserRoleService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    public UserRoleService(
            UserRepository userRepository,
            RoleRepository roleRepository
    ) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
    }

    @Transactional(readOnly = true)
    public List<UserRoleResponse> getRoles(UUID userId) {

        UUID companyId = TenantContext.requireCompanyId();

        User user = userRepository.findByIdAndCompanyId(
                        userId,
                        companyId
                )
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User with id '" + userId + "' not found"
                ));

        return user.getRoles()
                .stream()
                .map(UserRoleResponse::from)
                .toList();
    }

    public List<UserRoleResponse> addRole(
            UUID userId,
            UUID roleId
    ) {

        UUID companyId = TenantContext.requireCompanyId();

        User user = userRepository.findByIdAndCompanyId(
                        userId,
                        companyId
                )
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User with id '" + userId + "' not found"
                ));

        Role role = roleRepository.findByIdAndCompanyId(
                        roleId,
                        companyId
                )
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Role with id '" + roleId + "' not found"
                ));

        user.addRole(role);

        User savedUser = userRepository.save(user);

        return savedUser.getRoles()
                .stream()
                .map(UserRoleResponse::from)
                .toList();
    }

    public List<UserRoleResponse> removeRole(
            UUID userId,
            UUID roleId
    ) {

        UUID companyId = TenantContext.requireCompanyId();

        User user = userRepository.findByIdAndCompanyId(
                        userId,
                        companyId
                )
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User with id '" + userId + "' not found"
                ));

        Role role = roleRepository.findByIdAndCompanyId(
                        roleId,
                        companyId
                )
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Role with id '" + roleId + "' not found"
                ));

        user.removeRole(role);

        User savedUser = userRepository.save(user);

        return savedUser.getRoles()
                .stream()
                .map(UserRoleResponse::from)
                .toList();
    }
}