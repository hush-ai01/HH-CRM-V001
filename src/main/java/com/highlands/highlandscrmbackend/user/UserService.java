package com.highlands.highlandscrmbackend.user;

import com.highlands.highlandscrmbackend.common.exception.ResourceNotFoundException;
import com.highlands.highlandscrmbackend.common.exception.UserAlreadyExistsException;
import com.highlands.highlandscrmbackend.company.Company;
import com.highlands.highlandscrmbackend.company.CompanyRepository;
import com.highlands.highlandscrmbackend.role.Role;
import com.highlands.highlandscrmbackend.role.RoleRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final CompanyRepository companyRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(
            UserRepository userRepository,
            CompanyRepository companyRepository,
            RoleRepository roleRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.userRepository = userRepository;
        this.companyRepository = companyRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public UserResponse createUser(UserCreateRequest request) {

        Company company = companyRepository.findById(request.companyId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Company with id '" + request.companyId() + "' not found"
                ));

        if (userRepository.existsByCompanyIdAndEmail(
                request.companyId(),
                request.email()
        )) {
            throw new UserAlreadyExistsException(
                    "User with email '" + request.email()
                            + "' already exists for this company"
            );
        }

        Set<Role> roles = resolveRoles(request.companyId(), request.roleIds());

        String passwordHash = passwordEncoder.encode(request.password());

        User user = new User(
                company,
                request.email(),
                passwordHash,
                request.firstName(),
                request.lastName()
        );

        roles.forEach(user::addRole);

        User savedUser = userRepository.save(user);

        return UserResponse.from(savedUser);
    }

    @Transactional(readOnly = true)
    public List<UserResponse> getUsersByCompany(UUID companyId) {

        if (!companyRepository.existsById(companyId)) {
            throw new ResourceNotFoundException(
                    "Company with id '" + companyId + "' not found"
            );
        }

        return userRepository.findAllByCompanyId(companyId)
                .stream()
                .map(UserResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public UserResponse getUserById(UUID id) {

        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User with id '" + id + "' not found"
                ));

        return UserResponse.from(user);
    }

    private Set<Role> resolveRoles(UUID companyId, Set<UUID> roleIds) {

        if (roleIds == null || roleIds.isEmpty()) {
            return new HashSet<>();
        }

        Set<Role> roles = new HashSet<>();

        for (UUID roleId : roleIds) {

            Role role = roleRepository.findById(roleId)
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Role with id '" + roleId + "' not found"
                    ));

            if (!role.getCompany().getId().equals(companyId)) {
                throw new ResourceNotFoundException(
                        "Role with id '" + roleId
                                + "' does not belong to company '"
                                + companyId + "'"
                );
            }

            roles.add(role);
        }

        return roles;
    }
}