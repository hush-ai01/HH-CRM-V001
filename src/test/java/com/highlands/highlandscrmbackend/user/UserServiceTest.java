package com.highlands.highlandscrmbackend.user;

import com.highlands.highlandscrmbackend.common.exception.ResourceNotFoundException;
import com.highlands.highlandscrmbackend.common.exception.UserAlreadyExistsException;
import com.highlands.highlandscrmbackend.company.Company;
import com.highlands.highlandscrmbackend.company.CompanyRepository;
import com.highlands.highlandscrmbackend.role.Role;
import com.highlands.highlandscrmbackend.role.RoleRepository;
import com.highlands.highlandscrmbackend.security.AuthorizationService;
import com.highlands.highlandscrmbackend.security.ForbiddenException;
import com.highlands.highlandscrmbackend.security.TenantContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private CompanyRepository companyRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthorizationService authorizationService;

    @InjectMocks
    private UserService userService;

    private UUID companyId;
    private UUID userId;
    private UUID roleId;

    @BeforeEach
    void setUp() {
        TenantContext.clear();

        companyId = UUID.randomUUID();
        userId = UUID.randomUUID();
        roleId = UUID.randomUUID();
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    // -------------------------------------------------------------------------
    // CREATE USER
    // -------------------------------------------------------------------------

    @Test
    void shouldCreateUserSuccessfully() {

        TenantContext.setCompanyId(companyId);

        UserCreateRequest request = new UserCreateRequest(
                companyId,
                "john.doe@example.com",
                "Password123!",
                "John",
                "Doe",
                Set.of(roleId)
        );

        Company company = mock(Company.class);
        Role role = mock(Role.class);
        User savedUser = mock(User.class);

        when(company.getId())
                .thenReturn(companyId);

        when(companyRepository.findById(companyId))
                .thenReturn(Optional.of(company));

        when(userRepository.existsByCompanyIdAndEmail(
                companyId,
                request.email()
        )).thenReturn(false);

        when(roleRepository.findByIdAndCompanyId(
                roleId,
                companyId
        )).thenReturn(Optional.of(role));

        when(passwordEncoder.encode(request.password()))
                .thenReturn("hashed-password");

        when(savedUser.getId())
                .thenReturn(userId);

        when(savedUser.getCompany())
                .thenReturn(company);

        when(savedUser.getEmail())
                .thenReturn(request.email());

        when(savedUser.getFirstName())
                .thenReturn(request.firstName());

        when(savedUser.getLastName())
                .thenReturn(request.lastName());

        when(savedUser.isActive())
                .thenReturn(true);

        when(savedUser.getRoles())
                .thenReturn(Set.of(role));

        when(userRepository.save(any(User.class)))
                .thenReturn(savedUser);

        UserResponse response =
                userService.createUser(request);

        assertNotNull(response);

        verify(authorizationService)
                .requirePermission("USER_CREATE");

        verify(companyRepository)
                .findById(companyId);

        verify(userRepository)
                .existsByCompanyIdAndEmail(
                        companyId,
                        request.email()
                );

        verify(roleRepository)
                .findByIdAndCompanyId(
                        roleId,
                        companyId
                );

        verify(roleRepository, never())
                .findById(roleId);

        verify(passwordEncoder)
                .encode(request.password());

        verify(userRepository)
                .save(any(User.class));
    }

    @Test
    void shouldUseTenantContextInsteadOfRequestCompanyId() {

        UUID tenantCompanyId = UUID.randomUUID();
        UUID requestCompanyId = UUID.randomUUID();

        TenantContext.setCompanyId(tenantCompanyId);

        UserCreateRequest request = new UserCreateRequest(
                requestCompanyId,
                "john.doe@example.com",
                "Password123!",
                "John",
                "Doe",
                Set.of()
        );

        Company company = mock(Company.class);
        User savedUser = mock(User.class);

        when(company.getId())
                .thenReturn(tenantCompanyId);

        when(companyRepository.findById(tenantCompanyId))
                .thenReturn(Optional.of(company));

        when(userRepository.existsByCompanyIdAndEmail(
                tenantCompanyId,
                request.email()
        )).thenReturn(false);

        when(passwordEncoder.encode(request.password()))
                .thenReturn("hashed-password");

        when(savedUser.getId())
                .thenReturn(userId);

        when(savedUser.getCompany())
                .thenReturn(company);

        when(savedUser.getEmail())
                .thenReturn(request.email());

        when(savedUser.getFirstName())
                .thenReturn(request.firstName());

        when(savedUser.getLastName())
                .thenReturn(request.lastName());

        when(savedUser.isActive())
                .thenReturn(true);

        when(savedUser.getRoles())
                .thenReturn(Set.of());

        when(userRepository.save(any(User.class)))
                .thenReturn(savedUser);

        UserResponse response =
                userService.createUser(request);

        assertNotNull(response);

        verify(authorizationService)
                .requirePermission("USER_CREATE");

        verify(companyRepository)
                .findById(tenantCompanyId);

        verify(companyRepository, never())
                .findById(requestCompanyId);

        verify(userRepository)
                .existsByCompanyIdAndEmail(
                        tenantCompanyId,
                        request.email()
                );

        verify(userRepository, never())
                .existsByCompanyIdAndEmail(
                        requestCompanyId,
                        request.email()
                );

        verify(userRepository)
                .save(any(User.class));
    }

    @Test
    void shouldRejectCreateUserWhenTenantContextIsMissing() {

        TenantContext.clear();

        UserCreateRequest request = new UserCreateRequest(
                companyId,
                "john.doe@example.com",
                "Password123!",
                "John",
                "Doe",
                Set.of()
        );

        assertThrows(
                IllegalStateException.class,
                () -> userService.createUser(request)
        );

        verifyNoInteractions(
                authorizationService,
                companyRepository,
                userRepository,
                roleRepository,
                passwordEncoder
        );
    }

    @Test
    void shouldRejectCreateUserWhenUserLacksCreatePermission() {

        TenantContext.setCompanyId(companyId);

        UserCreateRequest request = new UserCreateRequest(
                companyId,
                "john.doe@example.com",
                "Password123!",
                "John",
                "Doe",
                Set.of()
        );

        doThrow(new ForbiddenException(
                "You do not have permission to perform this action"
        )).when(authorizationService)
                .requirePermission("USER_CREATE");

        assertThrows(
                ForbiddenException.class,
                () -> userService.createUser(request)
        );

        verify(authorizationService)
                .requirePermission("USER_CREATE");

        verifyNoInteractions(
                companyRepository,
                userRepository,
                roleRepository,
                passwordEncoder
        );
    }

    @Test
    void shouldRejectCreateUserWhenCompanyDoesNotExist() {

        TenantContext.setCompanyId(companyId);

        UserCreateRequest request = new UserCreateRequest(
                companyId,
                "john.doe@example.com",
                "Password123!",
                "John",
                "Doe",
                Set.of()
        );

        when(companyRepository.findById(companyId))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> userService.createUser(request)
        );

        verify(authorizationService)
                .requirePermission("USER_CREATE");

        verify(companyRepository)
                .findById(companyId);

        verifyNoInteractions(
                passwordEncoder,
                roleRepository
        );

        verify(userRepository, never())
                .save(any(User.class));
    }

    @Test
    void shouldRejectDuplicateUser() {

        TenantContext.setCompanyId(companyId);

        UserCreateRequest request = new UserCreateRequest(
                companyId,
                "john.doe@example.com",
                "Password123!",
                "John",
                "Doe",
                Set.of()
        );

        Company company = mock(Company.class);

        when(companyRepository.findById(companyId))
                .thenReturn(Optional.of(company));

        when(userRepository.existsByCompanyIdAndEmail(
                companyId,
                request.email()
        )).thenReturn(true);

        assertThrows(
                UserAlreadyExistsException.class,
                () -> userService.createUser(request)
        );

        verify(authorizationService)
                .requirePermission("USER_CREATE");

        verify(userRepository)
                .existsByCompanyIdAndEmail(
                        companyId,
                        request.email()
                );

        verify(userRepository, never())
                .save(any(User.class));

        verifyNoInteractions(
                passwordEncoder,
                roleRepository
        );
    }

    @Test
    void shouldRejectUserWhenRoleDoesNotExist() {

        TenantContext.setCompanyId(companyId);

        UserCreateRequest request = new UserCreateRequest(
                companyId,
                "john.doe@example.com",
                "Password123!",
                "John",
                "Doe",
                Set.of(roleId)
        );

        Company company = mock(Company.class);

        when(companyRepository.findById(companyId))
                .thenReturn(Optional.of(company));

        when(userRepository.existsByCompanyIdAndEmail(
                companyId,
                request.email()
        )).thenReturn(false);

        when(roleRepository.findByIdAndCompanyId(
                roleId,
                companyId
        )).thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> userService.createUser(request)
        );

        verify(authorizationService)
                .requirePermission("USER_CREATE");

        verify(roleRepository)
                .findByIdAndCompanyId(
                        roleId,
                        companyId
                );

        verify(roleRepository, never())
                .findById(roleId);

        verify(userRepository, never())
                .save(any(User.class));

        verifyNoInteractions(passwordEncoder);
    }

    @Test
    void shouldRejectRoleFromAnotherCompany() {

        TenantContext.setCompanyId(companyId);

        UserCreateRequest request = new UserCreateRequest(
                companyId,
                "john.doe@example.com",
                "Password123!",
                "John",
                "Doe",
                Set.of(roleId)
        );

        Company company = mock(Company.class);

        when(companyRepository.findById(companyId))
                .thenReturn(Optional.of(company));

        when(userRepository.existsByCompanyIdAndEmail(
                companyId,
                request.email()
        )).thenReturn(false);

        when(roleRepository.findByIdAndCompanyId(
                roleId,
                companyId
        )).thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> userService.createUser(request)
        );

        verify(authorizationService)
                .requirePermission("USER_CREATE");

        verify(roleRepository)
                .findByIdAndCompanyId(
                        roleId,
                        companyId
                );

        verify(roleRepository, never())
                .findById(roleId);

        verify(userRepository, never())
                .save(any(User.class));

        verifyNoInteractions(passwordEncoder);
    }

    @Test
    void shouldResolveRoleUsingCurrentTenant() {

        UUID tenantCompanyId = UUID.randomUUID();

        TenantContext.setCompanyId(tenantCompanyId);

        UserCreateRequest request = new UserCreateRequest(
                tenantCompanyId,
                "john.doe@example.com",
                "Password123!",
                "John",
                "Doe",
                Set.of(roleId)
        );

        Company company = mock(Company.class);
        Role role = mock(Role.class);
        User savedUser = mock(User.class);

        when(company.getId())
                .thenReturn(tenantCompanyId);

        when(companyRepository.findById(tenantCompanyId))
                .thenReturn(Optional.of(company));

        when(userRepository.existsByCompanyIdAndEmail(
                tenantCompanyId,
                request.email()
        )).thenReturn(false);

        when(roleRepository.findByIdAndCompanyId(
                roleId,
                tenantCompanyId
        )).thenReturn(Optional.of(role));

        when(passwordEncoder.encode(request.password()))
                .thenReturn("hashed-password");

        when(savedUser.getId())
                .thenReturn(userId);

        when(savedUser.getCompany())
                .thenReturn(company);

        when(savedUser.getEmail())
                .thenReturn(request.email());

        when(savedUser.getFirstName())
                .thenReturn(request.firstName());

        when(savedUser.getLastName())
                .thenReturn(request.lastName());

        when(savedUser.isActive())
                .thenReturn(true);

        when(savedUser.getRoles())
                .thenReturn(Set.of(role));

        when(userRepository.save(any(User.class)))
                .thenReturn(savedUser);

        UserResponse response =
                userService.createUser(request);

        assertNotNull(response);

        verify(authorizationService)
                .requirePermission("USER_CREATE");

        verify(roleRepository)
                .findByIdAndCompanyId(
                        roleId,
                        tenantCompanyId
                );

        verify(roleRepository, never())
                .findById(roleId);
    }

    // -------------------------------------------------------------------------
    // GET USERS BY TENANT
    // -------------------------------------------------------------------------

    @Test
    void shouldGetUsersForCurrentTenant() {

        TenantContext.setCompanyId(companyId);

        Company company = mock(Company.class);
        User user = mock(User.class);

        when(company.getId())
                .thenReturn(companyId);

        when(user.getId())
                .thenReturn(userId);

        when(user.getCompany())
                .thenReturn(company);

        when(userRepository.findAllByCompanyId(companyId))
                .thenReturn(List.of(user));

        List<UserResponse> response =
                userService.getUsersByCompany();

        assertNotNull(response);
        assertEquals(1, response.size());

        verify(authorizationService)
                .requirePermission("USER_READ");

        verify(userRepository)
                .findAllByCompanyId(companyId);
    }

    @Test
    void shouldRejectGetUsersWhenUserLacksReadPermission() {

        TenantContext.setCompanyId(companyId);

        doThrow(new ForbiddenException(
                "You do not have permission to perform this action"
        )).when(authorizationService)
                .requirePermission("USER_READ");

        assertThrows(
                ForbiddenException.class,
                () -> userService.getUsersByCompany()
        );

        verify(authorizationService)
                .requirePermission("USER_READ");

        verifyNoInteractions(userRepository);
    }

    @Test
    void shouldRejectGetUsersWhenTenantContextIsMissing() {

        TenantContext.clear();

        assertThrows(
                IllegalStateException.class,
                () -> userService.getUsersByCompany()
        );

        verifyNoInteractions(
                authorizationService,
                userRepository
        );
    }

    // -------------------------------------------------------------------------
    // GET USER BY ID
    // -------------------------------------------------------------------------

    @Test
    void shouldGetUserByIdForCurrentTenant() {

        TenantContext.setCompanyId(companyId);

        Company company = mock(Company.class);
        User user = mock(User.class);

        when(company.getId())
                .thenReturn(companyId);

        when(user.getId())
                .thenReturn(userId);

        when(user.getCompany())
                .thenReturn(company);

        when(userRepository.findByIdAndCompanyId(
                userId,
                companyId
        )).thenReturn(Optional.of(user));

        UserResponse response =
                userService.getUserById(userId);

        assertNotNull(response);

        verify(authorizationService)
                .requirePermission("USER_READ");

        verify(userRepository)
                .findByIdAndCompanyId(
                        userId,
                        companyId
                );
    }

    @Test
    void shouldRejectGetUserByIdWhenUserLacksReadPermission() {

        TenantContext.setCompanyId(companyId);

        doThrow(new ForbiddenException(
                "You do not have permission to perform this action"
        )).when(authorizationService)
                .requirePermission("USER_READ");

        assertThrows(
                ForbiddenException.class,
                () -> userService.getUserById(userId)
        );

        verify(authorizationService)
                .requirePermission("USER_READ");

        verifyNoInteractions(userRepository);
    }

    @Test
    void shouldNotReturnUserFromAnotherTenant() {

        TenantContext.setCompanyId(companyId);

        when(userRepository.findByIdAndCompanyId(
                userId,
                companyId
        )).thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> userService.getUserById(userId)
        );

        verify(authorizationService)
                .requirePermission("USER_READ");

        verify(userRepository)
                .findByIdAndCompanyId(
                        userId,
                        companyId
                );
    }

    @Test
    void shouldRejectGetUserByIdWhenTenantContextIsMissing() {

        TenantContext.clear();

        assertThrows(
                IllegalStateException.class,
                () -> userService.getUserById(userId)
        );

        verifyNoInteractions(
                authorizationService,
                userRepository
        );
    }

    // -------------------------------------------------------------------------
    // UPDATE USER
    // -------------------------------------------------------------------------

    @Test
    void shouldUpdateUserSuccessfully() {

        TenantContext.setCompanyId(companyId);

        UserUpdateRequest request = new UserUpdateRequest(
                "john.updated@example.com",
                "John",
                "Smith"
        );

        User user = mock(User.class);
        User savedUser = mock(User.class);
        Company company = mock(Company.class);

        when(userRepository.findByIdAndCompanyId(
                userId,
                companyId
        )).thenReturn(Optional.of(user));

        when(user.getEmail())
                .thenReturn("john@example.com");

        when(userRepository.existsByCompanyIdAndEmail(
                companyId,
                request.email()
        )).thenReturn(false);

        when(userRepository.save(user))
                .thenReturn(savedUser);

        when(savedUser.getId())
                .thenReturn(userId);

        when(savedUser.getCompany())
                .thenReturn(company);

        when(savedUser.getEmail())
                .thenReturn(request.email());

        when(savedUser.getFirstName())
                .thenReturn(request.firstName());

        when(savedUser.getLastName())
                .thenReturn(request.lastName());

        when(savedUser.isActive())
                .thenReturn(true);

        when(savedUser.getRoles())
                .thenReturn(Set.of());

        UserResponse response =
                userService.updateUser(
                        userId,
                        request
                );

        assertNotNull(response);

        verify(authorizationService)
                .requirePermission("USER_UPDATE");

        verify(userRepository)
                .findByIdAndCompanyId(
                        userId,
                        companyId
                );

        verify(userRepository)
                .existsByCompanyIdAndEmail(
                        companyId,
                        request.email()
                );

        verify(user)
                .setEmail(request.email());

        verify(user)
                .setFirstName(request.firstName());

        verify(user)
                .setLastName(request.lastName());

        verify(userRepository)
                .save(user);
    }

    @Test
    void shouldPartiallyUpdateUser() {

        TenantContext.setCompanyId(companyId);

        UserUpdateRequest request = new UserUpdateRequest(
                null,
                "John Updated",
                null
        );

        User user = mock(User.class);
        User savedUser = mock(User.class);
        Company company = mock(Company.class);

        when(userRepository.findByIdAndCompanyId(
                userId,
                companyId
        )).thenReturn(Optional.of(user));

        when(userRepository.save(user))
                .thenReturn(savedUser);

        when(savedUser.getId())
                .thenReturn(userId);

        when(savedUser.getCompany())
                .thenReturn(company);

        when(savedUser.getFirstName())
                .thenReturn(request.firstName());

        when(savedUser.getRoles())
                .thenReturn(Set.of());

        UserResponse response =
                userService.updateUser(
                        userId,
                        request
                );

        assertNotNull(response);

        verify(authorizationService)
                .requirePermission("USER_UPDATE");

        verify(user)
                .setFirstName("John Updated");

        verify(user, never())
                .setEmail(anyString());

        verify(user, never())
                .setLastName(anyString());

        verify(userRepository)
                .save(user);

        verify(userRepository, never())
                .existsByCompanyIdAndEmail(
                        any(UUID.class),
                        anyString()
                );
    }

    @Test
    void shouldRejectUpdateWhenUserDoesNotExist() {

        TenantContext.setCompanyId(companyId);

        UserUpdateRequest request = new UserUpdateRequest(
                "john.updated@example.com",
                "John",
                "Smith"
        );

        when(userRepository.findByIdAndCompanyId(
                userId,
                companyId
        )).thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> userService.updateUser(
                        userId,
                        request
                )
        );

        verify(authorizationService)
                .requirePermission("USER_UPDATE");

        verify(userRepository)
                .findByIdAndCompanyId(
                        userId,
                        companyId
                );

        verify(userRepository, never())
                .save(any(User.class));

        verify(userRepository, never())
                .existsByCompanyIdAndEmail(
                        any(UUID.class),
                        anyString()
                );
    }

    @Test
    void shouldRejectUpdateWhenEmailAlreadyExists() {

        TenantContext.setCompanyId(companyId);

        UserUpdateRequest request = new UserUpdateRequest(
                "existing@example.com",
                "John",
                "Smith"
        );

        User user = mock(User.class);

        when(userRepository.findByIdAndCompanyId(
                userId,
                companyId
        )).thenReturn(Optional.of(user));

        when(user.getEmail())
                .thenReturn("john@example.com");

        when(userRepository.existsByCompanyIdAndEmail(
                companyId,
                request.email()
        )).thenReturn(true);

        assertThrows(
                UserAlreadyExistsException.class,
                () -> userService.updateUser(
                        userId,
                        request
                )
        );

        verify(authorizationService)
                .requirePermission("USER_UPDATE");

        verify(userRepository)
                .existsByCompanyIdAndEmail(
                        companyId,
                        request.email()
                );

        verify(userRepository, never())
                .save(any(User.class));

        verify(user, never())
                .setEmail(anyString());

        verify(user, never())
                .setFirstName(anyString());

        verify(user, never())
                .setLastName(anyString());
    }

    @Test
    void shouldAllowUpdateWhenEmailIsUnchanged() {

        TenantContext.setCompanyId(companyId);

        String existingEmail = "john@example.com";

        UserUpdateRequest request = new UserUpdateRequest(
                existingEmail,
                "John Updated",
                "Smith"
        );

        User user = mock(User.class);
        User savedUser = mock(User.class);
        Company company = mock(Company.class);

        when(userRepository.findByIdAndCompanyId(
                userId,
                companyId
        )).thenReturn(Optional.of(user));

        when(user.getEmail())
                .thenReturn(existingEmail);

        when(userRepository.save(user))
                .thenReturn(savedUser);

        when(savedUser.getId())
                .thenReturn(userId);

        when(savedUser.getCompany())
                .thenReturn(company);

        when(savedUser.getEmail())
                .thenReturn(existingEmail);

        when(savedUser.getFirstName())
                .thenReturn(request.firstName());

        when(savedUser.getLastName())
                .thenReturn(request.lastName());

        when(savedUser.isActive())
                .thenReturn(true);

        when(savedUser.getRoles())
                .thenReturn(Set.of());

        UserResponse response =
                userService.updateUser(
                        userId,
                        request
                );

        assertNotNull(response);

        verify(authorizationService)
                .requirePermission("USER_UPDATE");

        verify(user)
                .setFirstName("John Updated");

        verify(user)
                .setLastName("Smith");

        verify(user, never())
                .setEmail(anyString());

        verify(userRepository, never())
                .existsByCompanyIdAndEmail(
                        any(UUID.class),
                        anyString()
                );

        verify(userRepository)
                .save(user);
    }

    @Test
    void shouldRejectUpdateWhenPermissionIsMissing() {

        TenantContext.setCompanyId(companyId);

        UserUpdateRequest request = new UserUpdateRequest(
                "john.updated@example.com",
                "John",
                "Smith"
        );

        doThrow(new ForbiddenException(
                "You do not have permission to perform this action"
        )).when(authorizationService)
                .requirePermission("USER_UPDATE");

        assertThrows(
                ForbiddenException.class,
                () -> userService.updateUser(
                        userId,
                        request
                )
        );

        verify(authorizationService)
                .requirePermission("USER_UPDATE");

        verifyNoInteractions(
                userRepository,
                companyRepository,
                roleRepository,
                passwordEncoder
        );
    }

    @Test
    void shouldOnlyUpdateUserWithinCurrentTenant() {

        UUID tenantCompanyId = UUID.randomUUID();
        UUID anotherCompanyId = UUID.randomUUID();

        TenantContext.setCompanyId(tenantCompanyId);

        UserUpdateRequest request = new UserUpdateRequest(
                "john.updated@example.com",
                "John",
                "Smith"
        );

        when(userRepository.findByIdAndCompanyId(
                userId,
                tenantCompanyId
        )).thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> userService.updateUser(
                        userId,
                        request
                )
        );

        verify(authorizationService)
                .requirePermission("USER_UPDATE");

        verify(userRepository)
                .findByIdAndCompanyId(
                        userId,
                        tenantCompanyId
                );

        verify(userRepository, never())
                .findByIdAndCompanyId(
                        userId,
                        anotherCompanyId
                );

        verify(userRepository, never())
                .save(any(User.class));
    }

    @Test
    void shouldRejectUpdateWhenTenantContextIsMissing() {

        TenantContext.clear();

        UserUpdateRequest request = new UserUpdateRequest(
                "john.updated@example.com",
                "John",
                "Smith"
        );

        assertThrows(
                IllegalStateException.class,
                () -> userService.updateUser(
                        userId,
                        request
                )
        );

        verifyNoInteractions(
                authorizationService,
                userRepository,
                companyRepository,
                roleRepository,
                passwordEncoder
        );
    }

    // -------------------------------------------------------------------------
    // UPDATE USER STATUS
    // -------------------------------------------------------------------------

    @Test
    void shouldDeactivateUserSuccessfully() {

        TenantContext.setCompanyId(companyId);

        UserStatusUpdateRequest request =
                new UserStatusUpdateRequest(false);

        User user = mock(User.class);
        User savedUser = mock(User.class);
        Company company = mock(Company.class);
        Role role = mock(Role.class);

        when(userRepository.findByIdAndCompanyId(
                userId,
                companyId
        )).thenReturn(Optional.of(user));

        when(userRepository.save(user))
                .thenReturn(savedUser);

        when(savedUser.getId())
                .thenReturn(userId);

        when(savedUser.getCompany())
                .thenReturn(company);

        when(company.getId())
                .thenReturn(companyId);

        when(savedUser.getEmail())
                .thenReturn("john@example.com");

        when(savedUser.getFirstName())
                .thenReturn("John");

        when(savedUser.getLastName())
                .thenReturn("Doe");

        when(savedUser.isActive())
                .thenReturn(false);

        when(savedUser.getRoles())
                .thenReturn(Set.of(role));

        UserResponse response =
                userService.updateUserStatus(
                        userId,
                        request
                );

        assertNotNull(response);

        verify(authorizationService)
                .requirePermission("USER_DEACTIVATE");

        verify(userRepository)
                .findByIdAndCompanyId(
                        userId,
                        companyId
                );

        verify(user)
                .setActive(false);

        verify(userRepository)
                .save(user);

        verifyNoMoreInteractions(userRepository);
    }

    @Test
    void shouldReactivateUserSuccessfully() {

        TenantContext.setCompanyId(companyId);

        UserStatusUpdateRequest request =
                new UserStatusUpdateRequest(true);

        User user = mock(User.class);
        User savedUser = mock(User.class);
        Company company = mock(Company.class);
        Role role = mock(Role.class);

        when(userRepository.findByIdAndCompanyId(
                userId,
                companyId
        )).thenReturn(Optional.of(user));

        when(userRepository.save(user))
                .thenReturn(savedUser);

        when(savedUser.getId())
                .thenReturn(userId);

        when(savedUser.getCompany())
                .thenReturn(company);

        when(company.getId())
                .thenReturn(companyId);

        when(savedUser.getEmail())
                .thenReturn("john@example.com");

        when(savedUser.getFirstName())
                .thenReturn("John");

        when(savedUser.getLastName())
                .thenReturn("Doe");

        when(savedUser.isActive())
                .thenReturn(true);

        when(savedUser.getRoles())
                .thenReturn(Set.of(role));

        UserResponse response =
                userService.updateUserStatus(
                        userId,
                        request
                );

        assertNotNull(response);

        verify(authorizationService)
                .requirePermission("USER_DEACTIVATE");

        verify(userRepository)
                .findByIdAndCompanyId(
                        userId,
                        companyId
                );

        verify(user)
                .setActive(true);

        verify(userRepository)
                .save(user);

        verifyNoMoreInteractions(userRepository);
    }

    @Test
    void shouldRejectUpdateStatusWhenPermissionIsMissing() {

        TenantContext.setCompanyId(companyId);

        UserStatusUpdateRequest request =
                new UserStatusUpdateRequest(false);

        doThrow(new ForbiddenException(
                "You do not have permission to perform this action"
        )).when(authorizationService)
                .requirePermission("USER_DEACTIVATE");

        assertThrows(
                ForbiddenException.class,
                () -> userService.updateUserStatus(
                        userId,
                        request
                )
        );

        verify(authorizationService)
                .requirePermission("USER_DEACTIVATE");

        verifyNoInteractions(userRepository);
    }

    @Test
    void shouldRejectUpdateStatusWhenTenantContextIsMissing() {

        TenantContext.clear();

        UserStatusUpdateRequest request =
                new UserStatusUpdateRequest(false);

        assertThrows(
                IllegalStateException.class,
                () -> userService.updateUserStatus(
                        userId,
                        request
                )
        );

        verifyNoInteractions(
                authorizationService,
                userRepository
        );
    }

    @Test
    void shouldRejectUpdateStatusWhenUserDoesNotExist() {

        TenantContext.setCompanyId(companyId);

        UserStatusUpdateRequest request =
                new UserStatusUpdateRequest(false);

        when(userRepository.findByIdAndCompanyId(
                userId,
                companyId
        )).thenReturn(Optional.empty());

        ResourceNotFoundException exception =
                assertThrows(
                        ResourceNotFoundException.class,
                        () -> userService.updateUserStatus(
                                userId,
                                request
                        )
                );

        assertEquals(
                "User with id '" + userId + "' not found",
                exception.getMessage()
        );

        verify(authorizationService)
                .requirePermission("USER_DEACTIVATE");

        verify(userRepository)
                .findByIdAndCompanyId(
                        userId,
                        companyId
                );

        verify(userRepository, never())
                .save(any(User.class));
    }

    @Test
    void shouldNotUpdateUserFromAnotherTenant() {

        TenantContext.setCompanyId(companyId);

        UUID anotherUserId = UUID.randomUUID();

        UserStatusUpdateRequest request =
                new UserStatusUpdateRequest(false);

        when(userRepository.findByIdAndCompanyId(
                anotherUserId,
                companyId
        )).thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> userService.updateUserStatus(
                        anotherUserId,
                        request
                )
        );

        verify(authorizationService)
                .requirePermission("USER_DEACTIVATE");

        verify(userRepository)
                .findByIdAndCompanyId(
                        anotherUserId,
                        companyId
                );

        verify(userRepository, never())
                .save(any(User.class));
    }

    // -------------------------------------------------------------------------
    // ADDITIONAL TENANT ISOLATION CHECKS
    // -------------------------------------------------------------------------

    @Test
    void shouldOnlyQueryUsersUsingCurrentTenant() {

        UUID tenantCompanyId = UUID.randomUUID();

        TenantContext.setCompanyId(tenantCompanyId);

        Company company = mock(Company.class);
        User user = mock(User.class);

        when(company.getId())
                .thenReturn(tenantCompanyId);

        when(user.getCompany())
                .thenReturn(company);

        when(userRepository.findAllByCompanyId(tenantCompanyId))
                .thenReturn(List.of(user));

        userService.getUsersByCompany();

        verify(authorizationService)
                .requirePermission("USER_READ");

        verify(userRepository)
                .findAllByCompanyId(tenantCompanyId);

        verify(userRepository, never())
                .findAllByCompanyId(
                        argThat(id ->
                                !tenantCompanyId.equals(id)
                        )
                );
    }

    @Test
    void shouldOnlyQueryUserByIdUsingCurrentTenant() {

        UUID tenantCompanyId = UUID.randomUUID();

        TenantContext.setCompanyId(tenantCompanyId);

        when(userRepository.findByIdAndCompanyId(
                userId,
                tenantCompanyId
        )).thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> userService.getUserById(userId)
        );

        verify(authorizationService)
                .requirePermission("USER_READ");

        verify(userRepository)
                .findByIdAndCompanyId(
                        userId,
                        tenantCompanyId
                );
    }
}