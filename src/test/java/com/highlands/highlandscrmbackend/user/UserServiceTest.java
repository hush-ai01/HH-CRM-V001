package com.highlands.highlandscrmbackend.user;

import com.highlands.highlandscrmbackend.common.exception.ResourceNotFoundException;
import com.highlands.highlandscrmbackend.common.exception.UserAlreadyExistsException;
import com.highlands.highlandscrmbackend.company.Company;
import com.highlands.highlandscrmbackend.company.CompanyRepository;
import com.highlands.highlandscrmbackend.role.Role;
import com.highlands.highlandscrmbackend.role.RoleRepository;
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

        /*
         * Role lookup must be tenant-aware.
         */
        when(roleRepository.findByIdAndCompanyId(
                roleId,
                companyId
        )).thenReturn(Optional.of(role));

        when(passwordEncoder.encode(request.password()))
                .thenReturn("hashed-password");

        /*
         * UserResponse.from() requires the saved User
         * to have a Company relationship.
         */
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

        /*
         * The old non-tenant-aware lookup must never be used.
         */
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

        /*
         * TenantContext must determine the company.
         */
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

        /*
         * The tenant-aware repository lookup returns nothing.
         */
        when(roleRepository.findByIdAndCompanyId(
                roleId,
                companyId
        )).thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> userService.createUser(request)
        );

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

//        UUID anotherCompanyId = UUID.randomUUID();

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

        /*
         * A role belonging to another company must not be returned
         * by findByIdAndCompanyId(roleId, companyId).
         *
         * From the UserService perspective this is simply:
         * "role does not exist for this tenant".
         */
        when(roleRepository.findByIdAndCompanyId(
                roleId,
                companyId
        )).thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> userService.createUser(request)
        );

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

        /*
         * Verify the current tenant was passed to the role query.
         */
        verify(roleRepository)
                .findByIdAndCompanyId(
                        roleId,
                        tenantCompanyId
                );

        /*
         * Ensure the old non-tenant-aware query is never used.
         */
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

        /*
         * The repository MUST be queried using the current tenant.
         */
        verify(userRepository)
                .findAllByCompanyId(companyId);
    }

    @Test
    void shouldRejectGetUsersWhenTenantContextIsMissing() {

        TenantContext.clear();

        assertThrows(
                IllegalStateException.class,
                () -> userService.getUsersByCompany()
        );

        verifyNoInteractions(userRepository);
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

        verify(userRepository)
                .findByIdAndCompanyId(
                        userId,
                        companyId
                );
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

        verifyNoInteractions(userRepository);
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

        verify(userRepository)
                .findAllByCompanyId(tenantCompanyId);

        /*
         * Any query using another tenant ID would violate isolation.
         */
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

        verify(userRepository)
                .findByIdAndCompanyId(
                        userId,
                        tenantCompanyId
                );
    }
}