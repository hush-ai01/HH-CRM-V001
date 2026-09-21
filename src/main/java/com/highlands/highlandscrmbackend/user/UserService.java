package com.highlands.highlandscrmbackend.user;

import com.highlands.highlandscrmbackend.common.exception.ResourceNotFoundException;
import com.highlands.highlandscrmbackend.common.exception.UserAlreadyExistsException;
import com.highlands.highlandscrmbackend.company.Company;
import com.highlands.highlandscrmbackend.company.CompanyRepository;
import com.highlands.highlandscrmbackend.role.Role;
import com.highlands.highlandscrmbackend.role.RoleRepository;
import com.highlands.highlandscrmbackend.security.AuthorizationService;
import com.highlands.highlandscrmbackend.security.TenantContext;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final CompanyRepository companyRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthorizationService authorizationService;

    public UserService(
            UserRepository userRepository,
            CompanyRepository companyRepository,
            RoleRepository roleRepository,
            PasswordEncoder passwordEncoder,
            AuthorizationService authorizationService
    ) {
        this.userRepository = userRepository;
        this.companyRepository = companyRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.authorizationService = authorizationService;
    }

    // -------------------------------------------------------------------------
    // CREATE USER
    // -------------------------------------------------------------------------

    public UserResponse createUser(UserCreateRequest request) {

        UUID companyId = TenantContext.requireCompanyId();

        authorizationService.requirePermission("USER_CREATE");

        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Company with id '" + companyId + "' not found"
                ));

        if (userRepository.existsByCompanyIdAndEmail(
                companyId,
                request.email()
        )) {
            throw new UserAlreadyExistsException(
                    "User with email '" + request.email()
                            + "' already exists for this company"
            );
        }

        Set<Role> roles = resolveRoles(request.roleIds());

        String passwordHash =
                passwordEncoder.encode(request.password());

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

    // -------------------------------------------------------------------------
    // GET USERS
    // -------------------------------------------------------------------------

    @Transactional(readOnly = true)
    public List<UserResponse> getUsersByCompany() {

        UUID companyId = TenantContext.requireCompanyId();

        authorizationService.requirePermission("USER_READ");

        return userRepository.findAllByCompanyId(companyId)
                .stream()
                .map(UserResponse::from)
                .toList();
    }

    // -------------------------------------------------------------------------
    // GET USER BY ID
    // -------------------------------------------------------------------------

    @Transactional(readOnly = true)
    public UserResponse getUserById(UUID id) {

        UUID companyId = TenantContext.requireCompanyId();

        authorizationService.requirePermission("USER_READ");

        User user = userRepository.findByIdAndCompanyId(
                        id,
                        companyId
                )
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User with id '" + id + "' not found"
                ));

        return UserResponse.from(user);
    }

    // -------------------------------------------------------------------------
    // RESOLVE ROLES
    // -------------------------------------------------------------------------

    private Set<Role> resolveRoles(Set<UUID> roleIds) {

        UUID companyId = TenantContext.requireCompanyId();

        return roleIds.stream()
                .map(roleId ->
                        roleRepository.findByIdAndCompanyId(
                                roleId,
                                companyId
                        ).orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Role with id '" + roleId
                                                + "' not found"
                                )
                        )
                )
                .collect(Collectors.toSet());
    }

    // -------------------------------------------------------------------------
    // UPDATE USER
    // -------------------------------------------------------------------------

    public UserResponse updateUser(
            UUID id,
            UserUpdateRequest request
    ) {

        UUID companyId = TenantContext.requireCompanyId();

        authorizationService.requirePermission("USER_UPDATE");

        User user = userRepository.findByIdAndCompanyId(
                        id,
                        companyId
                )
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User with id '" + id + "' not found"
                ));

        /*
         * Only perform the duplicate-email lookup when the requested
         * email is actually different from the user's current email.
         */
        if (request.email() != null
                && !request.email().equalsIgnoreCase(user.getEmail())) {

            if (userRepository.existsByCompanyIdAndEmail(
                    companyId,
                    request.email()
            )) {
                throw new UserAlreadyExistsException(
                        "User with email '" + request.email()
                                + "' already exists for this company"
                );
            }

            user.setEmail(request.email());
        }

        if (request.firstName() != null) {
            user.setFirstName(request.firstName());
        }

        if (request.lastName() != null) {
            user.setLastName(request.lastName());
        }

        User savedUser = userRepository.save(user);

        return UserResponse.from(savedUser);
    }

    // -------------------------------------------------------------------------
// UPDATE USER STATUS
// -------------------------------------------------------------------------

    public UserResponse updateUserStatus(
            UUID id,
            UserStatusUpdateRequest request
    ) {

        UUID companyId = TenantContext.requireCompanyId();

        authorizationService.requirePermission("USER_DEACTIVATE");

        User user = userRepository.findByIdAndCompanyId(
                        id,
                        companyId
                )
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User with id '" + id + "' not found"
                ));

        user.setActive(request.active());

        User savedUser = userRepository.save(user);

        return UserResponse.from(savedUser);
    }
}